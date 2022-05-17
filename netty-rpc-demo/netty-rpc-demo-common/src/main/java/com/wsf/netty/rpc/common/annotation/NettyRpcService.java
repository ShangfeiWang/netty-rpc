package com.wsf.netty.rpc.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface NettyRpcService {

    /**
     * 接口类型
     *
     * @return 接口类型
     */
    Class<?> value();

    /**
     * 版本号
     *
     * @return 版本号
     */
    String version() default "1.0.0";

}
