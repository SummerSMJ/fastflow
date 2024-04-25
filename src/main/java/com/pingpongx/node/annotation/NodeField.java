package com.pingpongx.node.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 节点参数字段
 *
 * @author songmj
 * @date 2024/4/24  7:15 PM
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface NodeField {

     String name();

     Class<?> type();

}
