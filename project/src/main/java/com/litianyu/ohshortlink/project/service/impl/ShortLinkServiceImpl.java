package com.litianyu.ohshortlink.project.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.text.StrBuilder;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.litianyu.ohshortlink.project.common.convention.exception.ServiceException;
import com.litianyu.ohshortlink.project.dao.entity.ShortLinkDO;
import com.litianyu.ohshortlink.project.dao.mapper.ShortLinkMapper;
import com.litianyu.ohshortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.litianyu.ohshortlink.project.dto.req.ShortLinkPageReqDTO;
import com.litianyu.ohshortlink.project.dto.resp.ShortLinkCreateRespDTO;
import com.litianyu.ohshortlink.project.dto.resp.ShortLinkGroupCountQueryRespDTO;
import com.litianyu.ohshortlink.project.dto.resp.ShortLinkPageRespDTO;
import com.litianyu.ohshortlink.project.service.ShortLinkService;
import com.litianyu.ohshortlink.project.toolkit.HashUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 短链接接口实现层
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShortLinkServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDO> implements ShortLinkService {

    private final RBloomFilter<String> shortUriCreateCachePenetrationBloomFilter;
    private final RedissonClient redissonClient;

    @Value("${short-link.domain.default}")
    private String createShortLinkDefaultDomain;

    // TODO：短链接缓存预热

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO requestParam) {
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
        // 这里都是先写数据库再写 redis
        try { // 先尝试向数据库插入
            baseMapper.insert(shortLinkDO);
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
        shortUriCreateCachePenetrationBloomFilter.add(fullShortUrl); // 将 fullShortUrl 添加到布隆过滤器中
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

    @Override
    public IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkPageReqDTO requestParam) {
        LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                .eq(ShortLinkDO::getGid, requestParam.getGid())
                .eq(ShortLinkDO::getEnableStatus, 0) // 只显示启用的短链接
                .eq(ShortLinkDO::getDelFlag, 0)
                .orderByDesc(ShortLinkDO::getCreateTime);
        IPage<ShortLinkDO> resultPage = baseMapper.selectPage(requestParam, queryWrapper);
        return resultPage.convert(each -> BeanUtil.toBean(each, ShortLinkPageRespDTO.class));
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
}
