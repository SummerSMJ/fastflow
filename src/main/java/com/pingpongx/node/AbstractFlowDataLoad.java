package com.pingpongx.node;

import com.pingpongx.risk.data.biz.service.node.core.FlowChain;
import com.yomahub.liteflow.builder.el.LiteFlowChainELBuilder;
import com.yomahub.liteflow.flow.FlowBus;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author songmj
 * @date 2024/4/25  1:42 PM
 */
@Slf4j
public abstract class AbstractFlowDataLoad implements FlowDataLoad {

    private List<FlowChain> flowChains;

    public void setFlowChains(List<FlowChain> flowChains){
        this.flowChains = flowChains;
    }

    @Override
    public void load() {
        log.info("load flow config start");
        for (FlowChain flowChain : flowChains) {
            LiteFlowChainELBuilder.createChain().setChainId(flowChain.getChainId()).setEL(
                    flowChain.getEl()
            ).build();
        }
        log.info("load flow config end");
    }

    @Override
    public void refresh() {
        for (FlowChain flowChain : flowChains) {
            FlowBus.reloadChain(flowChain.getChainId(), flowChain.getEl());
        }
    }
}
