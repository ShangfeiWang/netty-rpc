package com.wsf.netty.rpc.common.utils;

import com.wsf.netty.rpc.common.constant.Constant;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

/**
 * @author wsf
 * @since 20220526
 */
public class ServiceUtil {

    /**
     * 生成唯一的服务key
     *
     * @param application 应用名称
     * @param interfaceName 接口名称
     * @param version 版本号
     * @return 唯一的服务key
     */
    public static String generateUniqueServiceKey(String application, String interfaceName, String version) {
        return StringUtils.join(Arrays.asList(application, interfaceName, version), Constant.split);
    }

}
