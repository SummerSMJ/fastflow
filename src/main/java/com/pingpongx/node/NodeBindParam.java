package com.pingpongx.node;

import lombok.Getter;

import java.util.Map;

/**
 * 节点入参出参绑定实体
 *
 * @author songmj
 * @date 2024/4/24  1:38 PM
 */
@Getter
public class NodeBindParam {

    /**
     * 入参绑定参数 (key-value都是字段名)
     */
    private Map<String, String> inputBindParams;

    /**
     * 出参绑定参数 (key-value都是字段名)
     */
    private Map<String, String> outputBindParams;

}
