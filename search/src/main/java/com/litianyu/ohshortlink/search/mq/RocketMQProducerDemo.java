package com.litianyu.ohshortlink.search.mq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RocketMQProducerDemo {

    private final RocketMQTemplate rocketMQTemplate;

    @Value("${rocketmq.producer.topic}")
    private String esSyncTopic;

    public void send(Map<String, String> producerMap) {
        String key = UUID.randomUUID().toString();
        producerMap.put("key", key);
        Message<Map<String, String>> buildMessage = MessageBuilder
                .withPayload(producerMap)
                .setHeader("KEYS", key)
                .build();
        SendResult sendResult = rocketMQTemplate.syncSend(esSyncTopic, buildMessage);// TODO: 不需要设置延时时间
        SendStatus sendStatus = sendResult.getSendStatus();
        log.info("消息发送，结果：{}", sendStatus.toString());
    }
}
