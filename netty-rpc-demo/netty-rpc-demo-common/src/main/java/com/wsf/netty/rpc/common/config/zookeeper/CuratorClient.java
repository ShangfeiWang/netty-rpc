package com.wsf.netty.rpc.common.config.zookeeper;

import com.wsf.netty.rpc.common.constant.Constant;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import java.util.List;

/**
 * @author wsf
 * @since 20220526
 */
public class CuratorClient {

    private final CuratorFramework curatorFrameworkClient;

    public CuratorClient(String connectString, int timeout) {
        this(Constant.ZK_NAMESPACE, connectString, timeout, timeout);
    }

    public CuratorClient(String connectString) {
        this(Constant.ZK_NAMESPACE, connectString, Constant.ZK_SESSION_TIMEOUT, Constant.ZK_CONNECTION_TIMEOUT);
    }

    public CuratorFramework getCuratorFrameworkClient() {
        return curatorFrameworkClient;
    }

    public CuratorClient(String namespace, String connectString, int sessionTimeOutMs, int connectTimeOutMs) {
        curatorFrameworkClient = CuratorFrameworkFactory.builder()
                .namespace(namespace)
                .connectString(connectString)
                .sessionTimeoutMs(sessionTimeOutMs)
                .connectionTimeoutMs(connectTimeOutMs)
                .retryPolicy(new ExponentialBackoffRetry(100, 10))
                .build();
        curatorFrameworkClient.start();
    }

    /**
     * 添加zk节点状态监听
     *
     * @param listener 监听器
     */
    public void addConnectionStateListener(ConnectionStateListener listener) {
        curatorFrameworkClient.getConnectionStateListenable().addListener(listener);
    }

    /**
     * 创建节点路径
     *
     * @param path 路径
     * @param bytes 数据
     * @throws Exception 异常信息
     */
    public void createPathNodeData(String path, byte[] bytes) throws Exception {
        curatorFrameworkClient.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path, bytes);
    }

    /**
     * 创建持久节点路径
     *
     * @param path 路径
     * @param bytes 数据
     * @throws Exception 异常信息
     */
    public void createPersistentPathNodeData(String path, byte[] bytes) throws Exception {
        curatorFrameworkClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path, bytes);
    }

    /**
     * 删除一个节点路径
     *
     * @param path 路径
     * @throws Exception 异常信息
     */
    public void deletePath(String path) throws Exception {
        curatorFrameworkClient.delete().forPath(path);
    }

    /**
     * 给某个节点添加一个监听
     *
     * @param path 路径
     * @param watcher 监听器
     * @throws Exception 异常新
     */
    public void watchNode(String path, CuratorWatcher watcher) throws Exception {
        curatorFrameworkClient.getData().usingWatcher(watcher).forPath(path);
    }

    /**
     * 获取节点的数据
     *
     * @param path 路径
     * @return 数据
     * @throws Exception 异常信息
     */
    public byte[] getNodeData(String path) throws Exception {
        return curatorFrameworkClient.getData().forPath(path);
    }

    public List<String> getChildren(String path) throws Exception {
        return curatorFrameworkClient.getChildren().forPath(path);
    }

    public void watchTreeNode(String path, TreeCacheListener listener) {
        TreeCache treeCache = new TreeCache(curatorFrameworkClient, path);
        treeCache.getListenable().addListener(listener);
    }

    public void watchPathChildrenNode(String path, PathChildrenCacheListener listener) throws Exception {
        PathChildrenCache pathChildrenCache = new PathChildrenCache(curatorFrameworkClient, path, true);
        //BUILD_INITIAL_CACHE 代表使用同步的方式进行缓存初始化。
        pathChildrenCache.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);
        pathChildrenCache.getListenable().addListener(listener);
    }

    public void close() {
        curatorFrameworkClient.close();
    }

}
