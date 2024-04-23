package com.litianyu.ohshortlink.project.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.text.StrBuilder;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.litianyu.ohshortlink.project.common.convention.exception.ClientException;
import com.litianyu.ohshortlink.project.common.convention.exception.ServiceException;
import com.litianyu.ohshortlink.project.common.enums.VailDateTypeEnum;
import com.litianyu.ohshortlink.project.dao.entity.ShortLinkDO;
import com.litianyu.ohshortlink.project.dao.entity.ShortLinkGotoDO;
import com.litianyu.ohshortlink.project.dao.mapper.ShortLinkGotoMapper;
import com.litianyu.ohshortlink.project.dao.mapper.ShortLinkMapper;
import com.litianyu.ohshortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.litianyu.ohshortlink.project.dto.req.ShortLinkPageReqDTO;
import com.litianyu.ohshortlink.project.dto.req.ShortLinkUpdateReqDTO;
import com.litianyu.ohshortlink.project.dto.resp.ShortLinkCreateRespDTO;
import com.litianyu.ohshortlink.project.dto.resp.ShortLinkGroupCountQueryRespDTO;
import com.litianyu.ohshortlink.project.dto.resp.ShortLinkPageRespDTO;
import com.litianyu.ohshortlink.project.service.ShortLinkService;
import com.litianyu.ohshortlink.project.toolkit.HashUtil;
import com.litianyu.ohshortlink.project.toolkit.LinkUtil;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.litianyu.ohshortlink.project.common.constant.RedisKeyConstant.*;

/**
 * 短链接接口实现层
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShortLinkServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDO> implements ShortLinkService {

    private final RBloomFilter<String> shortUriCreateCachePenetrationBloomFilter;
    private final StringRedisTemplate stringRedisTemplate;
    private final RedissonClient redissonClient;
    private final ShortLinkGotoMapper shortLinkGotoMapper; // 引入短链接跳转持久层 mapper

    @Value("${short-link.domain.default}")
    private String createShortLinkDefaultDomain;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO requestParam) {
        // 短链接写在4个地方：link表、goto表、redis、布隆过滤器
        String shortLinkSuffix = generateSuffix(requestParam); // 拿到 6 位短链接后缀
        String fullShortUrl = StrBuilder.create(createShortLinkDefaultDomain) // 注意，是(域名+6位短链接)唯一标识一条短链接，而不仅仅是后缀（即允许后缀重复但域名不同的短链接）
                .append("/")
                .append(shortLinkSuffix)
                .toString();
        ShortLinkDO shortLinkDO = ShortLinkDO.builder()
                .domain(createShortLinkDefaultDomain)
                .originUrl(requestParam.getOriginUrl())
                .gid(requestParam.getGid())
                .createdType(requestParam.getCreatedType())
                .validDateType(requestParam.getValidDateType())
                .validDate(requestParam.getValidDate())
                .describe(requestParam.getDescribe())
                .shortUri(shortLinkSuffix)
                .enableStatus(0)
                .totalPv(0)
                .totalUv(0)
                .totalUip(0)
                .delTime(0L)
                .fullShortUrl(fullShortUrl)
//                .favicon(getFavicon(requestParam.getOriginUrl())) // 网站图标
                .build();
        ShortLinkGotoDO linkGotoDO = ShortLinkGotoDO.builder() // 短链接跳转
                .fullShortUrl(fullShortUrl)
                .gid(requestParam.getGid())
                .build();
        // 这里都是先写数据库再写 redis
        try { // 先尝试向数据库插入
            baseMapper.insert(shortLinkDO);
            shortLinkGotoMapper.insert(linkGotoDO);
        } catch (DuplicateKeyException ex) { // 如果出现异常，说明短链接重复
            if (!shortUriCreateCachePenetrationBloomFilter.contains(fullShortUrl)) { // 如果布隆过滤器中不存在，那么就向布隆过滤器中添加
                shortUriCreateCachePenetrationBloomFilter.add(fullShortUrl); // 把 fullShortUrl 添加到布隆过滤器中
            }
            throw new ServiceException(String.format("短链接：%s 生成重复", fullShortUrl)); // 最后抛异常
        }
        // 解释一下为什么会出现上面这种，数据在数据库中却不在缓存中的场景：
        // 因为添加短链接的时候是 mysql 和 redis 双写，那么有可能会出现第二阶段提交失败的情况（也就是 mysql 插入成功后，redis 写入失败）
        // 那么此时这个操作本质就是 mysql 做数据去重兜底 + redis和mysql一致性重建的过程

        // 如果写入数据库成功（说明短链接没有重复）
        // 缓存预热
        stringRedisTemplate.opsForValue().set( // 短链接跳转信息写入 redis
                String.format(GOTO_SHORT_LINK_KEY, fullShortUrl), // key:完整短链接
                requestParam.getOriginUrl(),                      // value:完整长链接
                LinkUtil.getLinkCacheValidTime(requestParam.getValidDate()), TimeUnit.MILLISECONDS // 按照短链接有效期设置过期时间
        );
        shortUriCreateCachePenetrationBloomFilter.add(fullShortUrl); // 将 fullShortUrl 添加到布隆过滤器中
        // TODO：删除短链接后，布隆过滤器如何删除？
        return ShortLinkCreateRespDTO.builder() // 返回 dto
                .fullShortUrl("http://" + shortLinkDO.getFullShortUrl())
                .originUrl(requestParam.getOriginUrl())
                .gid(requestParam.getGid())
                .build();
    }

    /**
     * 生成 6 位短链接后缀
     *
     * @param requestParam ShortLinkCreateReqDTO 请求参数
     * @return 返回生成的 6 位短链接
     */
    private String generateSuffix(ShortLinkCreateReqDTO requestParam) {
        int customGenerateCount = 0; // 重试次数
        String shorUri;
        while (true) { // 因为从长链接哈希成短链接，一定会存在冲突，那么就需要引入重试
            if (customGenerateCount > 10) { // 设置最大重试次数，防止循环次数过多对存储组件造成压力
                throw new ServiceException("短链接频繁生成，请稍后再试"); // 如果重试次数过多，抛出异常
            }
            String originUrl = requestParam.getOriginUrl(); // 原始长链接
            originUrl += UUID.randomUUID().toString(); // 拼接一个 uuid（可以减少哈希冲突）
            shorUri = HashUtil.hashToBase62(originUrl); // (原始长链接+uuid)一起去做哈希，拿到 6 位短链接后缀
            // 使用 redis 布隆过滤器判断短链接是否重复，如果重复会进入重试
            if (!shortUriCreateCachePenetrationBloomFilter.contains(createShortLinkDefaultDomain + "/" + shorUri)) {
                break;
            }
            customGenerateCount++; // 重试次数 + 1
        }
        return shorUri;
    }

    @Transactional(rollbackFor = Exception.class) // 事务管理，因为这个方法中涉及的数据库修改操作不止一个，需要有事务回滚措施
    @Override
    public void updateShortLink(ShortLinkUpdateReqDTO requestParam) { // TODO：这个逻辑后续需要优化，接口也没有调通
//        verificationWhitelist(requestParam.getOriginUrl());
        LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                .eq(ShortLinkDO::getGid, requestParam.getOriginGid())
                .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                .eq(ShortLinkDO::getDelFlag, 0)
                .eq(ShortLinkDO::getEnableStatus, 0);
        ShortLinkDO hasShortLinkDO = baseMapper.selectOne(queryWrapper); // 先获取修改之前的短链接
        if (hasShortLinkDO == null) { // 如果原来的短链接不存在，直接抛异常
            throw new ClientException("短链接记录不存在");
        }
        // 如果原来的短链接存在，则修改短链接
        ShortLinkDO shortLinkDO = ShortLinkDO.builder() // 构造新的短链接数据
                .domain(hasShortLinkDO.getDomain())
                .shortUri(hasShortLinkDO.getShortUri())
                .favicon(hasShortLinkDO.getFavicon())
                .createdType(hasShortLinkDO.getCreatedType())
                .gid(requestParam.getGid())
                .originUrl(requestParam.getOriginUrl())
                .describe(requestParam.getDescribe())
                .validDateType(requestParam.getValidDateType())
                .validDate(requestParam.getValidDate())
                .build();
        if (Objects.equals(hasShortLinkDO.getGid(), requestParam.getGid())) { // 如果没有给短链接切换分组
            LambdaUpdateWrapper<ShortLinkDO> updateWrapper = Wrappers.lambdaUpdate(ShortLinkDO.class)
                    .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                    .eq(ShortLinkDO::getGid, requestParam.getGid())
                    .eq(ShortLinkDO::getDelFlag, 0)
                    .eq(ShortLinkDO::getEnableStatus, 0)
                    .set(Objects.equals(requestParam.getValidDateType(), VailDateTypeEnum.PERMANENT.getType()), ShortLinkDO::getValidDate, null); // 如果短链接是永久有效，有效期应该为空
            baseMapper.update(shortLinkDO, updateWrapper); // 直接更新短链接
        } else { // 如果给短链接切换分组
            LambdaUpdateWrapper<ShortLinkDO> linkUpdateWrapper = Wrappers.lambdaUpdate(ShortLinkDO.class)
                    .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                    .eq(ShortLinkDO::getGid, hasShortLinkDO.getGid())
                    .eq(ShortLinkDO::getDelFlag, 0)
                    .eq(ShortLinkDO::getDelTime, 0L)
                    .eq(ShortLinkDO::getEnableStatus, 0)
                    .set(Objects.equals(requestParam.getValidDateType(), VailDateTypeEnum.PERMANENT.getType()), ShortLinkDO::getValidDate, null);
            baseMapper.delete(linkUpdateWrapper); // 先删原数据
            baseMapper.insert(shortLinkDO); // 再添加新数据

        }
    }

    @Override
    public IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkPageReqDTO requestParam) {
        LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                .eq(ShortLinkDO::getGid, requestParam.getGid())
                .eq(ShortLinkDO::getEnableStatus, 0) // 只显示启用的短链接
                .eq(ShortLinkDO::getDelFlag, 0)
                .orderByDesc(ShortLinkDO::getCreateTime);
        IPage<ShortLinkDO> resultPage = baseMapper.selectPage(requestParam, queryWrapper);
        return resultPage.convert(each -> {
            ShortLinkPageRespDTO result = BeanUtil.toBean(each, ShortLinkPageRespDTO.class);
            result.setDomain("http://" + result.getDomain());
            return result;
        });
    }

    @Override
    public List<ShortLinkGroupCountQueryRespDTO> listGroupShortLinkCount(List<String> requestParam) {
        QueryWrapper<ShortLinkDO> queryWrapper = Wrappers.query(new ShortLinkDO())
                .select("gid as gid, count(*) as shortLinkCount") // 这里字段名要改对
                .in("gid", requestParam)
                .eq("enable_status", 0)
                .eq("del_flag", 0)
                .eq("del_time", 0L)
                .groupBy("gid");
        List<Map<String, Object>> shortLinkDOList = baseMapper.selectMaps(queryWrapper);
        return BeanUtil.copyToList(shortLinkDOList, ShortLinkGroupCountQueryRespDTO.class);
    }

    @SneakyThrows // lombok 提供的处理异常的注解
    @Override
    public void restoreUrl(String shortUri, ServletRequest request, ServletResponse response) {
        String serverName = request.getServerName();
        String fullShortUrl = serverName + ":8001/" + shortUri; // TODO:这里后续需要修改
        // 先尝试从 redis 中获取原始长链接
        String originalLink = stringRedisTemplate.opsForValue().get(String.format(GOTO_SHORT_LINK_KEY, fullShortUrl));
        if (StrUtil.isNotBlank(originalLink)) { // 如果 redis 中能查到原始短链接
            // TODO：这里目前是不能转发的，还需要域名以及nginx，可以先写入本地 host 文件
            ((HttpServletResponse) response).sendRedirect(originalLink); // 直接重定向
            return;
        }
        // 如果 redis 中查不到原始短链接，需要查数据库（可能出现缓存问题）
        // 查询布隆过滤器，确认短链接是否存在 --> 解决缓存穿透问题
        boolean contains = shortUriCreateCachePenetrationBloomFilter.contains(fullShortUrl);
        if (!contains) { // 如果不存在
//            ((HttpServletResponse) response).sendRedirect("/page/notfound");
            return;
        }
        // TODO：检查空对象是否存在
        String gotoIsNullShortLink = stringRedisTemplate.opsForValue().get(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl));
        if (StrUtil.isNotBlank(gotoIsNullShortLink)) { // 如果空对象存在
//            ((HttpServletResponse) response).sendRedirect("/page/notfound");
            return;
        }
        // 短链接存在（但有可能误判）
        // 使用分布式锁 --> 解决缓存击穿问题
        RLock lock = redissonClient.getLock(String.format(LOCK_GOTO_SHORT_LINK_KEY, fullShortUrl)); // 创建分布式锁对象
        lock.lock(); // 获取分布式锁
        try {
            // TODO：这里为什么要进行双判
            originalLink = stringRedisTemplate.opsForValue().get(String.format(GOTO_SHORT_LINK_KEY, fullShortUrl));
            if (StrUtil.isNotBlank(originalLink)) {
                ((HttpServletResponse) response).sendRedirect(originalLink);
                return;
            }
            // 查 mysql
            LambdaQueryWrapper<ShortLinkGotoDO> linkGotoQueryWrapper = Wrappers.lambdaQuery(ShortLinkGotoDO.class)
                    .eq(ShortLinkGotoDO::getFullShortUrl, fullShortUrl);
            ShortLinkGotoDO shortLinkGotoDO = shortLinkGotoMapper.selectOne(linkGotoQueryWrapper);
            if (shortLinkGotoDO == null) { // 如果 mysql 中不存在该条数据，说明非法请求打到了 mysql 上
                // 在 redis 中存一个对应的空对象
                stringRedisTemplate.opsForValue().set(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl), "-", 30, TimeUnit.MINUTES);
//                ((HttpServletResponse) response).sendRedirect("/page/notfound");
                return;
            }
            LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                    .eq(ShortLinkDO::getGid, shortLinkGotoDO.getGid())
                    .eq(ShortLinkDO::getFullShortUrl, fullShortUrl)
                    .eq(ShortLinkDO::getDelFlag, 0)
                    .eq(ShortLinkDO::getEnableStatus, 0);
            ShortLinkDO shortLinkDO = baseMapper.selectOne(queryWrapper);
            if (shortLinkDO == null || (shortLinkDO.getValidDate() != null && shortLinkDO.getValidDate().before(new Date()))) { // 如果短链接不存在或者短链接已过期 TODO：redis 中短链接的过期时间怎么和mysql同步
                // 同理，缓存一个空对象
                stringRedisTemplate.opsForValue().set(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl), "-", 30, TimeUnit.MINUTES);
//                ((HttpServletResponse) response).sendRedirect("/page/notfound");
                return;
            }
            // 成功获取短链接，放到缓存中
            stringRedisTemplate.opsForValue().set( // 将短链接放到缓存中并重新设置缓存有效期
                    String.format(GOTO_SHORT_LINK_KEY, fullShortUrl),
                    shortLinkDO.getOriginUrl(),
                    LinkUtil.getLinkCacheValidTime(shortLinkDO.getValidDate()), TimeUnit.MILLISECONDS
            );
            ((HttpServletResponse) response).sendRedirect(shortLinkDO.getOriginUrl());// 重定向
        } finally {
            lock.unlock(); // 释放分布式锁
        }
    }
}
