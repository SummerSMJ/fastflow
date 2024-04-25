package com.pingpongx.node;

import com.yomahub.liteflow.core.FlowExecutor;
import com.yomahub.liteflow.core.FlowExecutorHolder;
import com.yomahub.liteflow.flow.LiteflowResponse;
import com.yomahub.liteflow.property.LiteflowConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author songmj
 * @date 2024/4/22  5:08 PM
 */
@Slf4j
public class FlowNodeExecutor {

    public static LiteflowResponse execute(String flowId, Object params, Object context) {
        LiteflowConfig config = new LiteflowConfig();
        FlowExecutor flowExecutor = FlowExecutorHolder.loadInstance(config);
        Future<LiteflowResponse> response = flowExecutor.execute2Future(flowId, params, context);
        try {
            LiteflowResponse liteflowResponse = response.get(10, TimeUnit.SECONDS);
            boolean success = liteflowResponse.isSuccess();
            if (!success) {
                log.error("flow exec fail,flowId:{},res:{}", flowId, liteflowResponse);
            } else {
                log.info("flow exec success,flowId:{},step:{}", flowId, liteflowResponse.getExecuteStepStrWithTime());
            }
            return liteflowResponse;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            log.error("flow exec timeout");
            throw new RuntimeException(e);
        }
    }

}
