package com.wsf.netty.rpc.common.codec;

import lombok.Data;

import java.io.Serializable;

/**
 * @author wsf
 * @since 20220526
 */
@Data
public class RpcRequest implements Serializable {

    private static final long serialVersionUID = -2424084431771398086L;

    /**
     * 请求id
     */
    private String requestId;

    /**
     * 类名称
     */
    private String className;

    /**
     * 方法名称
     */
    private String methodName;

    /**
     * 方法参数类型集合
     */
    private Class<?>[] parameterTypes;

    /**
     * 方法参数集合
     */
    private Object[] parameters;

    /**
     * 版本号
     */
    private String version;
}
