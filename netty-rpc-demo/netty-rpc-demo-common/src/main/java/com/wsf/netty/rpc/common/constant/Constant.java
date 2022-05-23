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

    public static final String REGISTER_HEAD = "/registry";

    /**
     * zk服务注册路径
     */
    public static final String RPC_REGISTER_PATH = REGISTER_HEAD + "/data";

    /**
     * zk会话超时时间
     */
    public static final int ZK_SESSION_TIMEOUT = 5000;

    /**
     * zk连接超时时间
     */
    public static final int ZK_CONNECTION_TIMEOUT = 5000;
}
