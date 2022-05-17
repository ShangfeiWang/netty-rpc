package com.wsf.netty.rpc.common.protocol;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author wsf
 * @since 20220526
 */
@Data
public class RpcProtocol implements Serializable {

    private static final long serialVersionUID = -1649018876416426032L;

    // service host
    private String host;

    // port
    private int port;

    // 服务列表
    private List<RpcServiceInfo> serviceInfoList;

}
