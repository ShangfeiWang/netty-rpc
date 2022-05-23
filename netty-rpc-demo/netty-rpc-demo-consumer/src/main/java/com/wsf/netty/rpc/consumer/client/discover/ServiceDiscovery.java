package com.wsf.netty.rpc.consumer.client.discover;

import com.alibaba.fastjson.JSON;
import com.wsf.netty.rpc.common.config.zookeeper.CuratorClient;
import com.wsf.netty.rpc.common.constant.Constant;
import com.wsf.netty.rpc.common.protocol.RpcProtocol;
import com.wsf.netty.rpc.consumer.client.connect.ConnectionManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author wsf
 * @since 20220526
 */
@Slf4j
public class ServiceDiscovery {

    private CuratorClient curatorClient;

    public ServiceDiscovery(String registerAddress) {
        curatorClient = new CuratorClient(registerAddress);
        discoveryService();
    }

    public void discoveryService() {
        // 获取注册服务的节点信息
        try {
            List<String> nodeList = curatorClient.getChildren(Constant.REGISTER_HEAD);
            List<RpcProtocol> serviceDateList = new ArrayList<>();
            for (String node : nodeList) {
                byte[] nodeData = curatorClient.getNodeData(Constant.REGISTER_HEAD + "/" + node);
                String json = new String(nodeData, StandardCharsets.UTF_8);
                RpcProtocol protocol = JSON.parseObject(json, RpcProtocol.class);
                serviceDateList.add(protocol);
            }
            log.info("service node dataList :{}", serviceDateList);
            ConnectionManager.getInstance().updateConnectedServer(serviceDateList);

            // 添加时间监听
            curatorClient.watchPathChildrenNode(Constant.REGISTER_HEAD, new PathChildrenCacheListener() {
                @Override
                public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent pathChildrenCacheEvent) throws Exception {
                    PathChildrenCacheEvent.Type type = pathChildrenCacheEvent.getType();
                    ChildData data = pathChildrenCacheEvent.getData();
                    switch (type) {
                        case CONNECTION_RECONNECTED:
                            log.info("reconnected to zk,try to get latest service list");
                            break;
                        case CHILD_ADDED:
                            getServiceAndUpdateServer(data, PathChildrenCacheEvent.Type.CHILD_ADDED);
                            break;
                        case CHILD_UPDATED:
                            getServiceAndUpdateServer(data, PathChildrenCacheEvent.Type.CHILD_UPDATED);
                            break;
                        case CHILD_REMOVED:
                            getServiceAndUpdateServer(data, PathChildrenCacheEvent.Type.CHILD_REMOVED);
                            break;
                    }
                }
            });
        } catch (Exception e) {
            log.error("watch node exception :{}", e.getMessage());
        }
    }

    private void getServiceAndUpdateServer(ChildData childData, PathChildrenCacheEvent.Type type) {
        String path = childData.getPath();
        byte[] data = childData.getData();
        log.info("child data updated path:{} ,data:{}", path, data);
        RpcProtocol protocol = JSON.parseObject(data, RpcProtocol.class);
        ConnectionManager.getInstance().updateConnectedServer(protocol, type);
    }

    public void stop() {
        curatorClient.close();
    }
}
