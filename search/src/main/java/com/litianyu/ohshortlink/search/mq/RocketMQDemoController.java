package com.litianyu.ohshortlink.search.mq;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.HashMap;

@Controller
@RequiredArgsConstructor
public class RocketMQDemoController {

    private final RocketMQProducerDemo producer;

    @GetMapping("/mq")
    public void send() {
        HashMap<String, String> msg = new HashMap<>();
        msg.put("name", "litianyu");
        producer.send(msg);
    }
}
