package com.wsf.netty.rpc.consumer.client.route;

import com.wsf.netty.rpc.common.protocol.RpcProtocol;
import com.wsf.netty.rpc.consumer.client.handler.RpcClientHandler;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * @author wsf
 * @since 20220526
 */
public class RpcLoadBalanceRandom extends RpcLoadBalance {

    private Random random = new Random();

    @Override
    public RpcProtocol route(String serviceKey, Map<RpcProtocol, RpcClientHandler> connectionServerNodes) throws Exception {
        Map<String, List<RpcProtocol>> serviceMap = getServiceMap(connectionServerNodes);
        List<RpcProtocol> rpcProtocolList = serviceMap.get(serviceKey);
        if (CollectionUtils.isEmpty(rpcProtocolList)) {
            throw new Exception("Can not find connection for service: " + serviceKey);
        }
        int index = random.nextInt(rpcProtocolList.size());
        return rpcProtocolList.get(index);
    }
}
