package com.pingpongx.node.core;

import lombok.Data;

/**
 * 流程实体
 *
 * @author songmj
 * @date 2024/4/25  1:46 PM
 */
@Data
public class FlowChain {

    /**
     * chainId
     */
    private String chainId;

    /**
     * chainName
     */
    private String chainName;

    /**
     * el 表达式
     */
    private String el;

}
