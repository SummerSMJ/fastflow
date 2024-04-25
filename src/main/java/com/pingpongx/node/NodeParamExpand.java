package com.pingpongx.node;

import com.alibaba.fastjson2.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * 提供参数转换能力
 *
 * @author songmj
 * @date 2024/4/23  10:15 AM
 */
public class NodeParamExpand {

    private Map<String, Object> inputParamsMap = new HashMap<>();

    private Map<String, Object> outputParamsMap = new HashMap<>();

    public NodeParamExpand(String inputParamsJson, String outputParamsJson) {
        if (inputParamsJson != null && !inputParamsJson.isEmpty()) {
            // 转换入参
            inputParamsMap = JSONObject.parseObject(inputParamsJson);
        }
        if (outputParamsJson != null && !outputParamsJson.isEmpty()) {
            // 转换出参
            outputParamsMap = JSONObject.parseObject(outputParamsJson);
        }
    }

    public void addInputParam(String key, Object value) {
        inputParamsMap.put(key, value);
    }

    public void addOutputParam(String key, Object value) {
        outputParamsMap.put(key, value);
    }

    public Object getInputParam(String key) {
        return inputParamsMap.get(key);
    }

    public Object getOutputParam(String key) {
        return outputParamsMap.get(key);
    }

}
