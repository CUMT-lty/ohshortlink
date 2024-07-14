package com.litianyu.ohshortlink.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.UUID;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.litianyu.ohshortlink.admin.common.conversion.exception.ClientException;
import com.litianyu.ohshortlink.admin.dao.entity.UserDO;
import com.litianyu.ohshortlink.admin.dao.mapper.UserMapper;
import com.litianyu.ohshortlink.admin.dto.req.UserLoginReqDTO;
import com.litianyu.ohshortlink.admin.dto.req.UserRegisterReqDTO;
import com.litianyu.ohshortlink.admin.dto.req.UserUpdateReqDTO;
import com.litianyu.ohshortlink.admin.dto.resp.UserLoginRespDTO;
import com.litianyu.ohshortlink.admin.dto.resp.UserRespDTO;
import com.litianyu.ohshortlink.admin.service.GroupService;
import com.litianyu.ohshortlink.admin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.litianyu.ohshortlink.admin.common.constant.RedisCacheConstant.LOCK_USER_REGISTER_KEY;
import static com.litianyu.ohshortlink.admin.common.constant.RedisCacheConstant.USER_LOGIN_KEY;
import static com.litianyu.ohshortlink.admin.common.enums.UserErrorCodeEnum.*;


/**
 * 用户接口实现层
 */

@Service // 标记为一个 bean
@RequiredArgsConstructor  // Lombok 提供的注解，会生成一个包含常量，和标识了 NotNull 的变量的构造方法
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {

    private final RBloomFilter<String> userRegisterCachePenetrationBloomFilter; // redisson 提供的布隆过滤器
    private final RedissonClient redissonClient;
    private final StringRedisTemplate stringRedisTemplate;
    private final GroupService groupService;

    @Override
    public UserRespDTO getUserByUsername(String username) {
        LambdaQueryWrapper<UserDO> queryWrapper = Wrappers.lambdaQuery(UserDO.class) // 规定好要查询的实体对象
                .eq(UserDO::getUsername, username);
        UserDO userDO = baseMapper.selectOne(queryWrapper); // ServiceImpl 中有 baseMapper
        UserRespDTO result = new UserRespDTO();
        BeanUtils.copyProperties(userDO, result); // springboot 提供的方法，将 dao 转为 dto
        return result;
    }

    @Override
    public Boolean hasUsername(String username) {
        return userRegisterCachePenetrationBloomFilter.contains(username); // 用户名不存在说明该用户名可用
    }

    @Override
    public void register(UserRegisterReqDTO requestParam) {
        if (hasUsername(requestParam.getUsername())) { // 如果用户名不可用（用户名已存在）
            throw new ClientException(USER_NAME_EXIST);
        }
        // 注意，虽然有 redis 缓存层来判断用户名是否已经存在，但是仍然需要给 mysql 中的 username 字段设置为 unique
        // 因为 redis 集群主从复制的时候是有可能出现数据丢失的
        // 如果 redis 主节点上的一部分脏数据还没来得及复制就丢失了，刚好此时有重复的数据插入，就会导致 mysql 中有重复的 username
        // 所以需要给 mysql 中的数据也做一个兜底
        RLock lock = redissonClient.getLock(LOCK_USER_REGISTER_KEY + requestParam.getUsername()); // redisson 分布式锁对象
        // redisson 有看门狗机制，会周期性地对锁进行心跳续期，保证在业务执行期间锁不会被自动释放
        if (!lock.tryLock()) { // 尝试上锁 TODO：这里后续可以添加一个合适的重试机制的
            throw new ClientException(USER_NAME_EXIST);
        }
        try { // TODO: 这里 redis 和数据库双写的一致性怎么保证
            int inserted = baseMapper.insert(BeanUtil.toBean(requestParam, UserDO.class)); // 插入
            if (inserted < 1) { // 插入失败
                //  TODO：这里其实是出现了 mysql 和 redis 中数据不一致的情况了，这里是不是应该把用户名加入到 redis 布隆过滤器中，重建一致性，也防止后续总有这样的请求打到数据库
                throw new ClientException(USER_SAVE_ERROR);
            }
            userRegisterCachePenetrationBloomFilter.add(requestParam.getUsername()); // 写 redis 布隆过滤器
            groupService.saveGroup(requestParam.getUsername(), "默认分组"); // 每个用户注册的时候都会有一个默认的短链接分组
            // 如果第二阶段失败：再次注册该用户名，mysql 给用户名字段设置了唯一，所以 mysql 的 insert 操作会失败，可以保证 mysql 中的数据没问题
            // TODO：但是会导致布隆过滤器中一直没有对应的值，但实际上该值已经存在
        } catch (DuplicateKeyException ex) {
            throw new ClientException(USER_EXIST);
        } finally {
            lock.unlock(); // 解锁
        }
    }

    @Override
    public void update(UserUpdateReqDTO requestParam) {
        // TODO：验证当前用户是否为登陆用户（只有已经登陆的用户可以修改自己的信息）（放在网关中做）
        LambdaUpdateWrapper<UserDO> updateWrapper = Wrappers.lambdaUpdate(UserDO.class)
                .eq(UserDO::getUsername, requestParam.getUsername());
        baseMapper.update(BeanUtil.toBean(requestParam, UserDO.class), updateWrapper);
    }

    @Override
    public UserLoginRespDTO login(UserLoginReqDTO requestParam) { // TODO：用户登陆这里后续可以考虑使用 jwt 来改造
        LambdaQueryWrapper<UserDO> queryWrapper = Wrappers.lambdaQuery(UserDO.class) // 在数据库中查找(用户名,密码)是否存在
                .eq(UserDO::getUsername, requestParam.getUsername())
                .eq(UserDO::getPassword, requestParam.getPassword())
                .eq(UserDO::getDelFlag, 0); // 删除标志也要判断
        UserDO userDO = baseMapper.selectOne(queryWrapper);
        if (userDO == null) { // 如果(用户名,密码)不存在
            throw new ClientException("用户不存在");
        }
        /**
         * 以 Hash 结构去存储用户的登陆状态，
         * Key：login_用户名（注意这里是使用用户名去作为 key 的，因为 username 是唯一，这样可以防止用户重复登陆）
         * Value：
         *  Key：token标识
         *  Val：JSON 字符串（用户信息）
         */
        Map<Object, Object> hasLoginMap = stringRedisTemplate.opsForHash().entries(USER_LOGIN_KEY + requestParam.getUsername());
        if (CollUtil.isNotEmpty(hasLoginMap)) { // 如果用户已经登陆
            stringRedisTemplate.expire(USER_LOGIN_KEY + requestParam.getUsername(), 30L, TimeUnit.MINUTES); // 更新过期时间
            String token = hasLoginMap.keySet().stream() // 如果已登陆返回原 token
                    .findFirst()
                    .map(Object::toString)
                    .orElseThrow(() -> new ClientException("用户登录错误"));
            return new UserLoginRespDTO(token); // 返回原 token
        }
        // 如果用户未登陆，生成新的 token 返回
        String uuid = UUID.randomUUID().toString(); // hutool 提供的工具，生成一个 uuid 作为 token 返回
        stringRedisTemplate.opsForHash().put(USER_LOGIN_KEY + requestParam.getUsername(), uuid, JSON.toJSONString(userDO));
        stringRedisTemplate.expire(USER_LOGIN_KEY + requestParam.getUsername(), 30L, TimeUnit.MINUTES); // 设置过期时间
        return new UserLoginRespDTO(uuid);
    }

    @Override
    public Boolean checkLogin(String username, String token) {
        return stringRedisTemplate.opsForHash().get(USER_LOGIN_KEY + username, token) != null;
    }

    @Override
    public void logout(String username, String token) {
        if (checkLogin(username, token)) { // 先验证是否登陆，只有已登陆的用户才能退出
            stringRedisTemplate.delete(USER_LOGIN_KEY + username);
            return;
        }
        throw new ClientException("用户Token不存在或用户未登录");
    }
}
