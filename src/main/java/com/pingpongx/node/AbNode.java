package com.pingpongx.node;


import com.pingpongx.node.annotation.NodeField;
import com.pingpongx.node.annotation.NodeStatement;
import com.yomahub.liteflow.annotation.LiteflowMethod;
import com.yomahub.liteflow.core.NodeComponent;
import com.yomahub.liteflow.enums.LiteFlowMethodEnum;
import com.yomahub.liteflow.enums.NodeTypeEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;

/**
 * @author songmj
 * @date 2024/4/22  5:00 PM
 */
@Slf4j
@RequiredArgsConstructor
@NodeStatement(
        id = "abNode",
        name = "测试CD_NODE",
        nodeType = NodeTypeEnum.COMMON,
        inputParams = {
                @NodeField(name = "userId", type = Long.class),
                @NodeField(name = "username", type = String.class)
        },
        outputParams = {
                @NodeField(name = "userId", type = Long.class),
                @NodeField(name = "username", type = String.class)
        }
)
public class AbNode extends NodeBase {

    @PostConstruct
    public void init() {
        log.info("abNode init");
    }

    @LiteflowMethod(value = LiteFlowMethodEnum.PROCESS, nodeType = NodeTypeEnum.COMMON)
    public void process(NodeComponent bindCmp) {
        FlowContext flowContext = bindCmp.getContextBean(FlowContext.class);
        Object userId = flowContext.get("userId", bindCmp);
        flowContext.put("name", "hhh", bindCmp);
        log.info("abNode 执行 userId:{}", userId);
    }

    @Override
    @LiteflowMethod(value = LiteFlowMethodEnum.ROLLBACK, nodeType = NodeTypeEnum.COMMON)
    public void rollback(NodeComponent bindCmp) {

    }
}
