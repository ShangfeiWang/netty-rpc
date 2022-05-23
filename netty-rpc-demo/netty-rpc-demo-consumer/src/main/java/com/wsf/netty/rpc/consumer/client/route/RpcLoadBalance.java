package com.wsf.netty.rpc.consumer.client.route;

import com.wsf.netty.rpc.common.protocol.RpcProtocol;
import com.wsf.netty.rpc.common.protocol.RpcServiceInfo;
import com.wsf.netty.rpc.common.utils.ServiceUtil;
import com.wsf.netty.rpc.consumer.client.handler.RpcClientHandler;
import org.apache.commons.collections4.MapUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author wsf
 * @since 20220526
 */
public abstract class RpcLoadBalance {

    protected Map<String, List<RpcProtocol>> getServiceMap(Map<RpcProtocol, RpcClientHandler> connectionServerNodes) {
        Map<String, List<RpcProtocol>> serviceMap = new HashMap<>();
        if (MapUtils.isNotEmpty(connectionServerNodes)) {
            for (RpcProtocol protocol : connectionServerNodes.keySet()) {
                for (RpcServiceInfo rpcServiceInfo : protocol.getServiceInfoList()) {
                    String serviceKey = ServiceUtil.generateUniqueServiceKey(rpcServiceInfo.getApplicationName(), rpcServiceInfo.getInterfaceName(), rpcServiceInfo.getVersion());
                    serviceMap.computeIfAbsent(serviceKey, key -> {
                        List<RpcProtocol> serviceList = new ArrayList<>();
                        serviceList.add(protocol);
                        return serviceList;
                    });
                }
            }
        }
        return serviceMap;
    }

    public abstract RpcProtocol route(String serviceKey, Map<RpcProtocol, RpcClientHandler> connectionServerNodes) throws Exception;

}
