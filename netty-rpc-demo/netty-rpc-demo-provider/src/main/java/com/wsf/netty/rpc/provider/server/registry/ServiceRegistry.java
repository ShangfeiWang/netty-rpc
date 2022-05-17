package com.wsf.netty.rpc.provider.server.registry;

import com.alibaba.fastjson.JSON;
import com.wsf.netty.rpc.common.config.zookeeper.CuratorClient;
import com.wsf.netty.rpc.common.constant.Constant;
import com.wsf.netty.rpc.common.protocol.RpcProtocol;
import com.wsf.netty.rpc.common.protocol.RpcServiceInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author wsf
 * @since 20220526
 */
@Slf4j
public class ServiceRegistry {

    private CuratorClient curatorClient;

    private List<String> pathList = new ArrayList<>();

    public ServiceRegistry(String registerAddress) {
        this.curatorClient = new CuratorClient(registerAddress, 5000);
    }

    public void registryService(String host, int port, Map<String, Object> serviceMap) {
        List<RpcServiceInfo> serviceInfoList = new ArrayList<>();
        for (String key : serviceMap.keySet()) {
            String[] split = key.split(Constant.split);
            String applicationName = split[0];
            String interfaceName = split[1];
            String version = split[2];

            RpcServiceInfo rpcServiceInfo = new RpcServiceInfo();
            rpcServiceInfo.setApplicationName(applicationName);
            rpcServiceInfo.setInterfaceName(interfaceName);
            rpcServiceInfo.setVersion(version);
            serviceInfoList.add(rpcServiceInfo);
            log.info("Register new Service :{}", rpcServiceInfo);
        }

        try {
            RpcProtocol protocol = new RpcProtocol();
            protocol.setHost(host);
            protocol.setPort(port);
            protocol.setServiceInfoList(serviceInfoList);
            String path = Constant.RPC_REGISTER_PATH + protocol.hashCode();
            String protocolInfo = JSON.toJSONString(protocol);
            curatorClient.createPathNodeData(path, protocolInfo.getBytes(StandardCharsets.UTF_8));
            log.info("Register {} new service, host: {}, port: {}", serviceInfoList.size(), host, port);
            pathList.add(path);
        } catch (Exception e) {
            log.error("Register service fail, exception: {}", e.getMessage());
        }

        curatorClient.addConnectionStateListener(new ConnectionStateListener() {
            @Override
            public void stateChanged(CuratorFramework client, ConnectionState newState) {
                if (newState == ConnectionState.RECONNECTED) {
                    log.info("Connection state: {}, register service after reconnected", newState);
                    registryService(host, port, serviceMap);
                }
            }
        });

    }

    public void unRegisterService() {
        log.info("移除所有注册的服务");
        try {
            for (String path : pathList) {
                curatorClient.deletePath(path);
            }
        } catch (Exception e) {
            log.error("删除节点路径error : {}", e.getMessage());
        }
        // 断开zk
        this.curatorClient.close();

    }

}
