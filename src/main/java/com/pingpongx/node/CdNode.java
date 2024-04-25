package com.pingpongx.node;


import com.pingpongx.node.annotation.NodeField;
import com.pingpongx.node.annotation.NodeStatement;
import com.yomahub.liteflow.annotation.LiteflowMethod;
import com.yomahub.liteflow.core.NodeComponent;
import com.yomahub.liteflow.enums.LiteFlowMethodEnum;
import com.yomahub.liteflow.enums.NodeTypeEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author songmj
 * @date 2024/4/22  5:00 PM
 */
@Slf4j
@RequiredArgsConstructor
@NodeStatement(
        id = "cdNode",
        name = "测试CD_NODE",
        nodeType = NodeTypeEnum.COMMON,
        inputParams = {
                @NodeField(name = "companyName", type = String.class),
                @NodeField(name = "companyId", type = String.class)
        },
        outputParams = {
                @NodeField(name = "vi", type = Boolean.class)
        }
)
public class CdNode extends NodeBase {

    @LiteflowMethod(value = LiteFlowMethodEnum.PROCESS, nodeType = NodeTypeEnum.COMMON)
    public void process(NodeComponent bindCmp) {
        FlowContext flowContext = bindCmp.getContextBean(FlowContext.class);
        Object name1 = flowContext.get("name1", bindCmp);
        Object name2 = flowContext.get("name2", bindCmp);
        log.info("cdNode 执行 name1:{},name2:{}", name1, name2);
    }

    @Override
    @LiteflowMethod(value = LiteFlowMethodEnum.ROLLBACK, nodeType = NodeTypeEnum.COMMON)
    public void rollback(NodeComponent bindCmp) {

    }
}
