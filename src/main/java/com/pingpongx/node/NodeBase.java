package com.pingpongx.node;

import com.yomahub.liteflow.annotation.LiteflowMethod;
import com.yomahub.liteflow.core.NodeComponent;
import com.yomahub.liteflow.enums.LiteFlowMethodEnum;
import com.yomahub.liteflow.enums.NodeTypeEnum;
import lombok.extern.slf4j.Slf4j;

/**
 * 节点基础类 做一些日志记录
 *
 * @author songmj
 * @date 2024/4/22  5:03 PM
 */
@Slf4j
public abstract class NodeBase {

    @LiteflowMethod(value = LiteFlowMethodEnum.ON_SUCCESS, nodeType = NodeTypeEnum.COMMON)
    public void success(NodeComponent bindCmp) {
        String nodeId = bindCmp.getNodeId();
        String name = bindCmp.getName();
        log.info("node exec success,nodeId:{},nodeName:{}", nodeId, name);
    }

    @LiteflowMethod(value = LiteFlowMethodEnum.ON_ERROR, nodeType = NodeTypeEnum.COMMON)
    public void error(NodeComponent bindCmp) {
        String nodeId = bindCmp.getNodeId();
        String name = bindCmp.getName();
        log.error("node exec error,nodeId:{},nodeName:{}", nodeId, name);
    }

    /**
     * 回滚
     *
     * @param bindCmp bindCmp
     */
    public abstract void rollback(NodeComponent bindCmp);

}
