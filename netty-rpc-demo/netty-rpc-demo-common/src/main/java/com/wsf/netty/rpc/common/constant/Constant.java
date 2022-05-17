package com.wsf.netty.rpc.common.constant;

/**
 * 常量
 *
 * @author wsf
 * @since 20220526
 */
public class Constant {

    /**
     * 分隔符
     */
    public static final String split = "#";

    /**
     * zk命名空间
     */
    public static final String ZK_NAMESPACE = "netty-rpc-wsf";

    /**
     * zk服务注册路径
     */
    public static final String RPC_REGISTER_PATH = "/registry/";

    /**
     * zk会话超时时间
     */
    public static final int ZK_SESSION_TIMEOUT = 5000;

    /**
     * zk连接超时时间
     */
    public static final int ZK_CONNECTION_TIMEOUT = 5000;
}
