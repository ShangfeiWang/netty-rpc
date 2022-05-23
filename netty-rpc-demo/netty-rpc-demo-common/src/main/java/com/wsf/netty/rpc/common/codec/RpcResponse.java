package com.wsf.netty.rpc.common.codec;

import lombok.Data;

import java.io.Serializable;

/**
 * @author wsf
 * @since 20220526
 */
@Data
public class RpcResponse implements Serializable {

    private static final long serialVersionUID = -4550532862264674543L;

    private String requestId;

    private Object result;

    private boolean success;

    private String errorMessage;

}
