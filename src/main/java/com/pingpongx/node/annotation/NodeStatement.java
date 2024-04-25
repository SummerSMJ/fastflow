package com.pingpongx.node.annotation;

import com.yomahub.liteflow.annotation.LFAliasFor;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.enums.NodeTypeEnum;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 节点声明
 *
 * @author songmj
 * @date 2024/4/24  7:07 PM
 */
@LiteflowComponent
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface NodeStatement {

    @AliasFor(
            annotation = LiteflowComponent.class,
            attribute = "id"
    )
    @LFAliasFor("id")
    String id();

    @AliasFor(
            annotation = LiteflowComponent.class,
            attribute = "name"
    )
    @LFAliasFor("name")
    String name();

    NodeTypeEnum nodeType() default NodeTypeEnum.COMMON;

    NodeField[] inputParams() default {};

    NodeField[] outputParams() default {};
}
