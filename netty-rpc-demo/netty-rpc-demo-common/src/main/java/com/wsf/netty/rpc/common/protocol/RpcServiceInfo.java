package com.wsf.netty.rpc.common.protocol;

import lombok.Data;

/**
 * @author wsf
 * @since 20220526
 */
@Data
public class RpcServiceInfo {

    /**
     * 应用名称
     */
    private String applicationName;

    /**
     * 接口名称
     */
    private String interfaceName;

    /**
     * 版本号
     */
    private String version;

}
