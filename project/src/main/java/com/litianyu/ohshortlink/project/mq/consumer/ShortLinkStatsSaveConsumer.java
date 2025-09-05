package com.litianyu.ohshortlink.project.mq.consumer;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.Week;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.litianyu.ohshortlink.project.common.convention.exception.ServiceException;
import com.litianyu.ohshortlink.project.dao.entity.LinkAccessLogsDO;
import com.litianyu.ohshortlink.project.dao.entity.LinkAccessStatsDO;
import com.litianyu.ohshortlink.project.dao.entity.LinkBrowserStatsDO;
import com.litianyu.ohshortlink.project.dao.entity.LinkDeviceStatsDO;
import com.litianyu.ohshortlink.project.dao.entity.LinkLocaleStatsDO;
import com.litianyu.ohshortlink.project.dao.entity.LinkNetworkStatsDO;
import com.litianyu.ohshortlink.project.dao.entity.LinkOsStatsDO;
import com.litianyu.ohshortlink.project.dao.entity.LinkStatsTodayDO;
import com.litianyu.ohshortlink.project.dao.entity.ShortLinkGotoDO;
import com.litianyu.ohshortlink.project.dao.mapper.LinkAccessLogsMapper;
import com.litianyu.ohshortlink.project.dao.mapper.LinkAccessStatsMapper;
import com.litianyu.ohshortlink.project.dao.mapper.LinkBrowserStatsMapper;
import com.litianyu.ohshortlink.project.dao.mapper.LinkDeviceStatsMapper;
import com.litianyu.ohshortlink.project.dao.mapper.LinkLocaleStatsMapper;
import com.litianyu.ohshortlink.project.dao.mapper.LinkNetworkStatsMapper;
import com.litianyu.ohshortlink.project.dao.mapper.LinkOsStatsMapper;
import com.litianyu.ohshortlink.project.dao.mapper.LinkStatsTodayMapper;
import com.litianyu.ohshortlink.project.dao.mapper.ShortLinkGotoMapper;
import com.litianyu.ohshortlink.project.dao.mapper.ShortLinkMapper;
import com.litianyu.ohshortlink.project.dto.biz.ShortLinkStatsRecordDTO;
import com.litianyu.ohshortlink.project.mq.idempotent.MessageQueueIdempotentHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

import static com.litianyu.ohshortlink.project.common.constant.RedisKeyConstant.LOCK_GID_UPDATE_KEY;

/**
 * 短链接监控状态保存消息队列消费者
 */
@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(
        topic = "${rocketmq.producer.topic}",
        consumerGroup = "${rocketmq.consumer.group}"
)
public class ShortLinkStatsSaveConsumer implements RocketMQListener<Map<String, String>> {

    private final ShortLinkMapper shortLinkMapper;
    private final ShortLinkGotoMapper shortLinkGotoMapper;
    private final RedissonClient redissonClient;
    private final LinkAccessStatsMapper linkAccessStatsMapper;
    private final LinkLocaleStatsMapper linkLocaleStatsMapper;
    private final LinkOsStatsMapper linkOsStatsMapper;
    private final LinkBrowserStatsMapper linkBrowserStatsMapper;
    private final LinkAccessLogsMapper linkAccessLogsMapper;
    private final LinkDeviceStatsMapper linkDeviceStatsMapper;
    private final LinkNetworkStatsMapper linkNetworkStatsMapper;
    private final LinkStatsTodayMapper linkStatsTodayMapper;

    private final MessageQueueIdempotentHandler messageQueueIdempotentHandler;


    @Override
    public void onMessage(Map<String, String> producerMap) {
        String keys = producerMap.get("keys");
        if (messageQueueIdempotentHandler.isMessageBeingConsumed(keys)) { // 消息已经被消费过
            if (messageQueueIdempotentHandler.isAccomplish(keys)) { // 且已经消费完成
                log.info("[消息消费日志] 消息已被消费且消费成功");
                return;
            }
            throw new ServiceException("消息未完成流程，需要消息队列重试"); // 被消费但未完成则需要重试
        }
        try {
            ShortLinkStatsRecordDTO statsRecord = JSON.parseObject(producerMap.get("statsRecord"), ShortLinkStatsRecordDTO.class);
            actualSaveShortLinkStats(statsRecord); // 短链接访问统计
        } catch (Throwable ex) {
            log.error("记录短链接监控消费异常", ex);
            try {
                messageQueueIdempotentHandler.delMessageProcessed(keys);
            } catch (Throwable remoteEx) {
                log.error("删除幂等标识错误", remoteEx);
            }
            throw ex;
        }
        messageQueueIdempotentHandler.setAccomplish(keys); // 成功消费，标记消息消费完成
        log.info("[消息消费统计监控] 消息消费结果：成功，消息keys：{}", keys);
    }

    public void actualSaveShortLinkStats(ShortLinkStatsRecordDTO statsRecord) {
        String fullShortUrl = statsRecord.getFullShortUrl();
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock(String.format(LOCK_GID_UPDATE_KEY, fullShortUrl)); // 获取读写锁
        RLock rLock = readWriteLock.readLock();
        rLock.tryLock(); // 如果获取不到写锁，统计操作仍然需要进行（写操作耗时很长，不能让一个写操作阻塞大量的读操作），把任务放入延迟队列
        try {
            LambdaQueryWrapper<ShortLinkGotoDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkGotoDO.class)
                    .eq(ShortLinkGotoDO::getFullShortUrl, fullShortUrl);
            ShortLinkGotoDO shortLinkGotoDO = shortLinkGotoMapper.selectOne(queryWrapper);
            String  gid = shortLinkGotoDO.getGid();
            Date currentDate = statsRecord.getCurrentDate();
            int hour = DateUtil.hour(currentDate, true);
            Week week = DateUtil.dayOfWeekEnum(currentDate);
            int weekValue = week.getIso8601Value();
            LinkAccessStatsDO linkAccessStatsDO = LinkAccessStatsDO.builder()
                    .pv(1)
                    .uv(statsRecord.getUvFirstFlag() ? 1 : 0)
                    .uip(statsRecord.getUipFirstFlag() ? 1 : 0)
                    .hour(hour)
                    .weekday(weekValue)
                    .fullShortUrl(fullShortUrl)
                    .date(currentDate)
                    .build();
            linkAccessStatsMapper.shortLinkStats(linkAccessStatsDO);

            // 地区相关信息之前从高德开放平台获取，现在下掉，全部写成默认信息
            String actualProvince = "未知";
            String actualCity = "未知";
            String province = "未知";
            boolean unknownFlag = StrUtil.equals(province, "[]");
            LinkLocaleStatsDO linkLocaleStatsDO = LinkLocaleStatsDO.builder()
                    .province(actualProvince = unknownFlag ? actualProvince : province)
                    .city(actualCity)
                    .adcode("未知")
                    .cnt(1)
                    .fullShortUrl(fullShortUrl)
                    .country("中国")
                    .date(currentDate)
                    .build();
            linkLocaleStatsMapper.shortLinkLocaleState(linkLocaleStatsDO);

            LinkOsStatsDO linkOsStatsDO = LinkOsStatsDO.builder()
                    .os(statsRecord.getOs())
                    .cnt(1)
                    .fullShortUrl(fullShortUrl)
                    .date(currentDate)
                    .build();
            linkOsStatsMapper.shortLinkOsState(linkOsStatsDO);
            LinkBrowserStatsDO linkBrowserStatsDO = LinkBrowserStatsDO.builder()
                    .browser(statsRecord.getBrowser())
                    .cnt(1)
                    .fullShortUrl(fullShortUrl)
                    .date(currentDate)
                    .build();
            linkBrowserStatsMapper.shortLinkBrowserState(linkBrowserStatsDO);
            LinkDeviceStatsDO linkDeviceStatsDO = LinkDeviceStatsDO.builder()
                    .device(statsRecord.getDevice())
                    .cnt(1)
                    .fullShortUrl(fullShortUrl)
                    .date(currentDate)
                    .build();
            linkDeviceStatsMapper.shortLinkDeviceState(linkDeviceStatsDO);
            LinkNetworkStatsDO linkNetworkStatsDO = LinkNetworkStatsDO.builder()
                    .network(statsRecord.getNetwork())
                    .cnt(1)
                    .fullShortUrl(fullShortUrl)
                    .date(currentDate)
                    .build();
            linkNetworkStatsMapper.shortLinkNetworkState(linkNetworkStatsDO);
            LinkAccessLogsDO linkAccessLogsDO = LinkAccessLogsDO.builder()
                    .user(statsRecord.getUv())
                    .ip(statsRecord.getRemoteAddr())
                    .browser(statsRecord.getBrowser())
                    .os(statsRecord.getOs())
                    .network(statsRecord.getNetwork())
                    .device(statsRecord.getDevice())
                    .locale(StrUtil.join("-", "中国", actualProvince, actualCity))
                    .fullShortUrl(fullShortUrl)
                    .build();
            linkAccessLogsMapper.insert(linkAccessLogsDO);
            shortLinkMapper.incrementStats(gid, fullShortUrl, 1, statsRecord.getUvFirstFlag() ? 1 : 0, statsRecord.getUipFirstFlag() ? 1 : 0);
            LinkStatsTodayDO linkStatsTodayDO = LinkStatsTodayDO.builder()
                    .todayPv(1)
                    .todayUv(statsRecord.getUvFirstFlag() ? 1 : 0)
                    .todayUip(statsRecord.getUipFirstFlag() ? 1 : 0)
                    .fullShortUrl(fullShortUrl)
                    .date(currentDate)
                    .build();
            linkStatsTodayMapper.shortLinkTodayState(linkStatsTodayDO);
        } catch (Throwable ex) {
            log.error("短链接访问量统计异常", ex);
        } finally {
            rLock.unlock();
        }
    }
}
