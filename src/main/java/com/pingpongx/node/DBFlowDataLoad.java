package com.pingpongx.node;

import com.google.common.collect.Lists;
import com.pingpongx.risk.data.biz.service.node.core.FlowChain;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * node和流程编排数据加载
 *
 * @author songmj
 * @date 2024/4/22  4:21 PM
 */
@Slf4j
@Component
public class DBFlowDataLoad extends AbstractFlowDataLoad {

    @Override
    public void getData() {
        List<FlowChain> flowChains = Lists.newArrayList();
        FlowChain flowChain = new FlowChain();
        flowChain.setChainId("chain2");
        flowChain.setChainName("chain2");
        flowChain.setEl("xxx = '{\"inputBindParams\":{\"userId\":\"userId1\"},\"outputBindParams\":{\"name\":\"name1\"}}';" +
                "yyy = '{\"inputBindParams\":{\"userId\":\"userId2\"},\"outputBindParams\":{\"name\":\"name2\"}}';" +
                "THEN(WHEN(abNode.data(xxx),abNode.data(yyy)),cdNode)");
        flowChains.add(flowChain);
        this.setFlowChains(flowChains);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        this.getData();
        this.load();
    }
}
