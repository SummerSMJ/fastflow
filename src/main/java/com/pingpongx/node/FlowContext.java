package com.pingpongx.node;

import com.yomahub.liteflow.core.NodeComponent;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * 上下文信息
 *
 * @author songmj
 * @date 2024/4/24  1:34 PM
 */
@Slf4j
public class FlowContext {

    /**
     * 上下文数据
     */
    private final Map<String, Object> data = new HashMap<>();

    public Object get(String key) {
        return data.get(key);
    }

    /**
     * 从上下文取数据 带数据转换
     *
     * @param key     字段名
     * @param bindCmp 当前组件
     * @return data
     */
    public Object get(String key, NodeComponent bindCmp) {
        // 判断是否参数绑定
        NodeBindParam cmpData = bindCmp.getCmpData(NodeBindParam.class);
        // 处理参数转换
        if (cmpData != null && cmpData.getInputBindParams() != null) {
            String bindKey = cmpData.getInputBindParams().get(key);
            if (bindKey != null && !bindKey.isEmpty()) {
                key = bindKey;
            }
        }
        return data.get(key);
    }

    public void put(String key, Object value) {
        data.put(key, value);
    }

    /**
     * 设置上下文数据 带数据转换
     *
     * @param key     字段名
     * @param value   数据
     * @param bindCmp 当前组件
     */
    public void put(String key, Object value, NodeComponent bindCmp) {
        // 判断是否参数绑定
        NodeBindParam cmpData = bindCmp.getCmpData(NodeBindParam.class);
        // 处理参数转换
        if (cmpData != null && cmpData.getOutputBindParams() != null) {
            String bindKey = cmpData.getOutputBindParams().get(key);
            if (bindKey != null && !bindKey.isEmpty()) {
                key = bindKey;
            }
        }
        data.put(key, value);
    }

    /**
     * 从上下文获取指定类型对象 带数据转换
     *
     * @param clazz   待转化实体
     * @param bindCmp 当前组件
     * @param <T>     类对象
     * @return 指定类型对象
     */
    public <T> T getByClass(Class<T> clazz, NodeComponent bindCmp) {
        try {
            T t = clazz.newInstance();
            Field[] declaredFields = clazz.getDeclaredFields();
            for (Field declaredField : declaredFields) {
                declaredField.setAccessible(true);
                String name = declaredField.getName();
                declaredField.set(t, get(name, bindCmp));
            }
            return t;
        } catch (InstantiationException | IllegalAccessException e) {
            log.error("FlowContext getByClass error,clazz:{},bindCmp:{}", clazz, bindCmp);
            throw new RuntimeException(e);
        }
    }

    /**
     * 把对象数据放进上下文 带数据转换
     *
     * @param obj     待处理对象
     * @param bindCmp 当前组件
     */
    public void putByObj(Object obj, NodeComponent bindCmp) {
        try {
            Class<?> aClass = obj.getClass();
            Field[] declaredFields = aClass.getDeclaredFields();
            for (Field declaredField : declaredFields) {
                declaredField.setAccessible(true);
                Object o = declaredField.get(obj);
                put(declaredField.getName(), o, bindCmp);
            }
        } catch (IllegalAccessException e) {
            log.error("FlowContext putByObj error,obj:{},bindCmp:{}", obj, bindCmp);
            throw new RuntimeException(e);
        }
    }

}
