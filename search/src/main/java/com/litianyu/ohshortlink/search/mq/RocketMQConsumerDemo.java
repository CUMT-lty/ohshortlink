package com.litianyu.ohshortlink.search.mq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(
        topic = "${rocketmq.producer.topic}",
        consumerGroup = "${rocketmq.consumer.group}"
)
public class RocketMQConsumerDemo implements RocketMQListener<Map<String, String>> {

    @Override
    public void onMessage(Map<String, String> producerMap) {
        log.info("消息消费");
        for (Map.Entry<String, String> entry : producerMap.entrySet()) {
            log.info("[onMessage] key:{}, value:{}", entry.getKey(), entry.getValue());
        }
        log.info("========");
    }
}
