package com.litianyu.ohshortlink.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.litianyu.ohshortlink.admin.common.conversion.exception.ClientException;
import com.litianyu.ohshortlink.admin.dao.entity.UserDO;
import com.litianyu.ohshortlink.admin.dao.mapper.UserMapper;
import com.litianyu.ohshortlink.admin.dto.req.UserRegisterReqDTO;
import com.litianyu.ohshortlink.admin.dto.req.UserUpdateReqDTO;
import com.litianyu.ohshortlink.admin.dto.resp.UserRespDTO;
import com.litianyu.ohshortlink.admin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import static com.litianyu.ohshortlink.admin.common.constant.RedisCacheConstant.LOCK_USER_REGISTER_KEY;
import static com.litianyu.ohshortlink.admin.common.enums.UserErrorCodeEnum.*;


/**
 * 用户接口实现层
 */

@Service // 标记为一个 bean
@RequiredArgsConstructor  // Lombok 提供的注解，会生成一个包含常量，和标识了 NotNull 的变量的构造方法
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {

    private final RBloomFilter<String> userRegisterCachePenetrationBloomFilter; // redisson 提供的布隆过滤器
    private final RedissonClient redissonClient;

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
                throw new ClientException(USER_SAVE_ERROR);
            }
            userRegisterCachePenetrationBloomFilter.add(requestParam.getUsername()); // 写 redis 布隆过滤器
//        groupService.saveGroup(requestParam.getUsername(), "默认分组");
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
}
