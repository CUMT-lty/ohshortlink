package com.litianyu.ohshortlink.project.config;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 初始化限流配置
 */
@Component
public class SentinelRuleConfig implements InitializingBean {

    @Override
    public void afterPropertiesSet() throws Exception { // 在 spring 启动的时候执行
        List<FlowRule> rules = new ArrayList<>();
        FlowRule createOrderRule = new FlowRule();
        createOrderRule.setResource("create_short-link");
        createOrderRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        createOrderRule.setCount(1);
        rules.add(createOrderRule);
        FlowRuleManager.loadRules(rules);
    }
}
