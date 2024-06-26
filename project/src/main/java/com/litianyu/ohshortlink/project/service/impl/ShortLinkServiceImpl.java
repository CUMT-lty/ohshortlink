package com.litianyu.ohshortlink.project.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.Week;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.text.StrBuilder;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.litianyu.ohshortlink.project.common.convention.exception.ClientException;
import com.litianyu.ohshortlink.project.common.convention.exception.ServiceException;
import com.litianyu.ohshortlink.project.common.enums.VailDateTypeEnum;
import com.litianyu.ohshortlink.project.dao.entity.*;
import com.litianyu.ohshortlink.project.dao.mapper.*;
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
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static com.litianyu.ohshortlink.project.common.constant.RedisKeyConstant.*;
import static com.litianyu.ohshortlink.project.common.constant.ShortLinkConstant.AMAP_REMOTE_URL;

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
    private final LinkAccessStatsMapper linkAccessStatsMapper;
    private final LinkLocaleStatsMapper linkLocaleStatsMapper;
    private final LinkOsStatsMapper linkOsStatsMapper;
    private final LinkBrowserStatsMapper linkBrowserStatsMapper;
    private final LinkAccessLogsMapper linkAccessLogsMapper;
    private final LinkDeviceStatsMapper linkDeviceStatsMapper;
    private final LinkNetworkStatsMapper linkNetworkStatsMapper;
    private final LinkStatsTodayMapper linkStatsTodayMapper;

    @Value("${short-link.domain.default}")
    private String createShortLinkDefaultDomain;

    @Value("${short-link.stats.locale.amap-key}")
    private String statsLocaleAmapKey;

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
                .favicon(getFavicon(requestParam.getOriginUrl())) // 网站图标 TODO：后续可以优化为异步实现
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
    public IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkPageReqDTO requestParam) { // TODO：后续需要重构
//        LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
//                .eq(ShortLinkDO::getGid, requestParam.getGid())
//                .eq(ShortLinkDO::getEnableStatus, 0) // 只显示启用的短链接
//                .eq(ShortLinkDO::getDelFlag, 0)
//                .orderByDesc(ShortLinkDO::getCreateTime);
        IPage<ShortLinkDO> resultPage = baseMapper.pageLink(requestParam);
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
            shortLinkStats(fullShortUrl, null, request, response); // 成功跳转之前需要做相关统计
            // TODO：这里目前是不能转发的，还需要域名以及nginx，可以先写入本地 host 文件
            ((HttpServletResponse) response).sendRedirect(originalLink); // 直接重定向
            return;
        }
        // 如果 redis 中查不到原始短链接，需要查数据库（可能出现缓存问题）
        // 查询布隆过滤器，确认短链接是否存在 --> 解决缓存穿透问题
        boolean contains = shortUriCreateCachePenetrationBloomFilter.contains(fullShortUrl);
        if (!contains) { // 如果不存在
            ((HttpServletResponse) response).sendRedirect("/page/notfound");
            return;
        }
        // TODO：检查空对象是否存在
        String gotoIsNullShortLink = stringRedisTemplate.opsForValue().get(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl));
        if (StrUtil.isNotBlank(gotoIsNullShortLink)) { // 如果空对象存在
            ((HttpServletResponse) response).sendRedirect("/page/notfound"); // 跳转到不存在页面
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
                shortLinkStats(fullShortUrl, null, request, response); // 成功跳转之前需要做相关统计
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
                ((HttpServletResponse) response).sendRedirect("/page/notfound"); // 跳转到不存在页面
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
                ((HttpServletResponse) response).sendRedirect("/page/notfound"); // 跳转到不存在页面
                return;
            }
            // 成功获取短链接，放到缓存中
            stringRedisTemplate.opsForValue().set( // 将短链接放到缓存中并重新设置缓存有效期
                    String.format(GOTO_SHORT_LINK_KEY, fullShortUrl),
                    shortLinkDO.getOriginUrl(),
                    LinkUtil.getLinkCacheValidTime(shortLinkDO.getValidDate()), TimeUnit.MILLISECONDS
            );
            shortLinkStats(fullShortUrl, shortLinkDO.getGid(), request, response); // 成功跳转之前需要做相关统计
            ((HttpServletResponse) response).sendRedirect(shortLinkDO.getOriginUrl());// 重定向
        } finally {
            lock.unlock(); // 释放分布式锁
        }
    }

    // TODO: 监控代码这部分现在多少带点并发问题，后续通过异步的方式解决，这里先记个todo
    private void shortLinkStats(String fullShortUrl, String gid, ServletRequest request, ServletResponse response) {
        AtomicBoolean uvFirstFlag = new AtomicBoolean(); // 该用户是否是第一次访问
        Cookie[] cookies = ((HttpServletRequest) request).getCookies();
        try {
            AtomicReference<String> uv = new AtomicReference<>();
            Runnable addResponseCookieTask = () -> { // TODO：这里为什么要这么写
                uv.set(UUID.fastUUID().toString());
                Cookie uvCookie = new Cookie("uv", uv.get()); // uuid 作为 cookie，标识用户的身份
                uvCookie.setMaxAge(60 * 60 * 24 * 30); // 有效时间为1个月
                uvCookie.setPath(StrUtil.sub(fullShortUrl, fullShortUrl.indexOf("/"), fullShortUrl.length())); // 设置cookie的作用路径为仅当前短链接有效
                ((HttpServletResponse) response).addCookie(uvCookie); // 添加cookie
                uvFirstFlag.set(Boolean.TRUE); // 标识第一次访问
                stringRedisTemplate.opsForSet().add("short-link:stats:uv:" + fullShortUrl, uv.get());
            };
            if (ArrayUtil.isNotEmpty(cookies)) { // 如果不是第一次访问（带着cookie）
                Arrays.stream(cookies)
                        .filter(each -> Objects.equals(each.getName(), "uv"))
                        .findFirst()
                        .map(Cookie::getValue)
                        .ifPresentOrElse(each -> {
                            uv.set(each);
                            Long uvAdded = stringRedisTemplate.opsForSet().add("short-link:stats:uv:" + fullShortUrl, each); // 存入 redis
                            uvFirstFlag.set(uvAdded != null && uvAdded > 0L); // 设置是否是第一次访问
                        }, addResponseCookieTask);
            } else { // 如果没有设置cookie，说明是用户第一次访问
                addResponseCookieTask.run();
            }
            String remoteAddr = LinkUtil.getActualIp(((HttpServletRequest) request)); // 获取 ip
            Long uipAdded = stringRedisTemplate.opsForSet().add("short-link:stats:uip:" + fullShortUrl, remoteAddr);
            boolean uipFirstFlag = uipAdded != null && uipAdded > 0L; // 该ip是否是第一次访问
            if (StrUtil.isBlank(gid)) {
                LambdaQueryWrapper<ShortLinkGotoDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkGotoDO.class)
                        .eq(ShortLinkGotoDO::getFullShortUrl, fullShortUrl);
                ShortLinkGotoDO shortLinkGotoDO = shortLinkGotoMapper.selectOne(queryWrapper); // 去 goto 表中查 gid
                gid = shortLinkGotoDO.getGid();
            }
            int hour = DateUtil.hour(new Date(), true); // 获取当前小时
            Week week = DateUtil.dayOfWeekEnum(new Date());
            int weekValue = week.getIso8601Value(); // 获取现在是当月的第几周
            LinkAccessStatsDO linkAccessStatsDO = LinkAccessStatsDO.builder()
                    .pv(1)
                    .uv(uvFirstFlag.get() ? 1 : 0)
                    .uip(uipFirstFlag ? 1 : 0)
                    .hour(hour)
                    .weekday(weekValue)
                    .fullShortUrl(fullShortUrl)
                    .gid(gid) // TODO：gid后续需要删掉
                    .date(new Date())
                    .build();
            linkAccessStatsMapper.shortLinkStats(linkAccessStatsDO);
            // 地区访问监控
            Map<String, Object> localeParamMap = new HashMap<>();
            localeParamMap.put("key", statsLocaleAmapKey); // 高德api的应用key
            localeParamMap.put("ip", remoteAddr);
            String localeResultStr = HttpUtil.get(AMAP_REMOTE_URL, localeParamMap); // 发起 http 请求 TODO：后续需要重构
            JSONObject localeResultObj = JSON.parseObject(localeResultStr); // 转为 json 对象
            String infoCode = localeResultObj.getString("infocode");
            String actualProvince;
            String actualCity;
            if (StrUtil.isNotBlank(infoCode) && StrUtil.equals(infoCode, "10000")) {
                String province = localeResultObj.getString("province");
                boolean unknownFlag = StrUtil.equals(province, "[]");
                LinkLocaleStatsDO linkLocaleStatsDO = LinkLocaleStatsDO.builder()
                        .province(actualProvince = unknownFlag ? "未知" : province)
                        .city(actualCity = unknownFlag ? "未知" : localeResultObj.getString("city"))
                        .adcode(unknownFlag ? "未知" : localeResultObj.getString("adcode"))
                        .cnt(1)
                        .fullShortUrl(fullShortUrl)
                        .country("中国")
                        .gid(gid) // TODO：gid 后续需要删
                        .date(new Date())
                        .build();
                linkLocaleStatsMapper.shortLinkLocaleState(linkLocaleStatsDO);
                // 操作系统访问监控
                String os = LinkUtil.getOs(((HttpServletRequest) request));
                LinkOsStatsDO linkOsStatsDO = LinkOsStatsDO.builder()
                        .os(os) // 获取操作系统
                        .cnt(1)
                        .gid(gid) // TODO：gid 后续需要删
                        .fullShortUrl(fullShortUrl)
                        .date(new Date())
                        .build();
                linkOsStatsMapper.shortLinkOsState(linkOsStatsDO);
                // 浏览器访问监控
                String browser = LinkUtil.getBrowser(((HttpServletRequest) request));
                LinkBrowserStatsDO linkBrowserStatsDO = LinkBrowserStatsDO.builder()
                        .browser(browser)
                        .cnt(1)
                        .gid(gid) // TODO：gid 后续需要删
                        .fullShortUrl(fullShortUrl)
                        .date(new Date())
                        .build();
                linkBrowserStatsMapper.shortLinkBrowserState(linkBrowserStatsDO);
                // 访问设备监控
                String device = LinkUtil.getDevice(((HttpServletRequest) request));
                LinkDeviceStatsDO linkDeviceStatsDO = LinkDeviceStatsDO.builder()
                        .device(device)
                        .cnt(1)
                        .gid(gid) // TODO：gid 后续需要删
                        .fullShortUrl(fullShortUrl)
                        .date(new Date())
                        .build();
                linkDeviceStatsMapper.shortLinkDeviceState(linkDeviceStatsDO);
                // 网络信息监控
                String network = LinkUtil.getNetwork((HttpServletRequest) request);
                LinkNetworkStatsDO linkNetworkStatsDO = LinkNetworkStatsDO.builder()
                        .network(network)
                        .cnt(1)
                        .gid(gid) // TODO：gid 后续需要删
                        .fullShortUrl(fullShortUrl)
                        .date(new Date())
                        .build();
                linkNetworkStatsMapper.shortLinkNetworkState(linkNetworkStatsDO);
                // 记录浏览日志（注意字段要填全）
                LinkAccessLogsDO linkAccessLogsDO = LinkAccessLogsDO.builder()
                        .user(uv.get()) // 用 cookie 标识用户身份
                        .ip(remoteAddr)
                        .browser(browser)
                        .os(os)
                        .network(network)
                        .device(device)
                        .locale(StrUtil.join("-", "中国", actualProvince, actualCity))
                        .gid(gid) // TODO：gid 后续需要删
                        .fullShortUrl(fullShortUrl)
                        .build();
                linkAccessLogsMapper.insert(linkAccessLogsDO);
                baseMapper.incrementStats(gid, fullShortUrl, 1, uvFirstFlag.get() ? 1 : 0, uipFirstFlag ? 1 : 0);
                LinkStatsTodayDO linkStatsTodayDO = LinkStatsTodayDO.builder()
                        .todayPv(1)
                        .todayUv(uvFirstFlag.get() ? 1 : 0)
                        .todayUip(uipFirstFlag ? 1 : 0)
                        .gid(gid) // TODO：gid 后续要删掉
                        .fullShortUrl(fullShortUrl)
                        .date(new Date())
                        .build();
                linkStatsTodayMapper.shortLinkTodayState(linkStatsTodayDO);
            }
        } catch (Throwable ex) {
            log.error("短链接访问量统计异常", ex);
        }
    }

    @SneakyThrows
    private String getFavicon(String url) {
        URL targetUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) targetUrl.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();
        int responseCode = connection.getResponseCode();
        if (HttpURLConnection.HTTP_OK == responseCode) {
            Document document = Jsoup.connect(url).get();
            Element faviconLink = document.select("link[rel~=(?i)^(shortcut )?icon]").first();
            if (faviconLink != null) {
                return faviconLink.attr("abs:href");
            }
        }
        return null;
    }
}
