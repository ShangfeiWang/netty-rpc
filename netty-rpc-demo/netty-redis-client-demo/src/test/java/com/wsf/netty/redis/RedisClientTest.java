package com.wsf.netty.redis;

import com.wsf.netty.redis.client.service.enums.ClientTypeEnum;
import com.wsf.netty.redis.client.service.enums.ExpireMode;
import com.wsf.netty.redis.client.service.enums.XMode;
import com.wsf.netty.redis.client.service.impl.RedisStringClientImpl;
import org.junit.Test;

import java.util.UUID;

/**
 * @author wsf
 * @since 20220623
 */
public class RedisClientTest {

    @Test
    public void testSet() {
        RedisStringClientImpl impl = new RedisStringClientImpl(ClientTypeEnum.STRING);
        System.out.println(impl.set("zhangsan", "123"));
    }

    @Test
    public void testGet() {
        RedisStringClientImpl impl = new RedisStringClientImpl(ClientTypeEnum.STRING);
        System.out.println(impl.get("lisi"));
    }

    @Test
    public void testSetWithExpire() throws InterruptedException {
        RedisStringClientImpl impl = new RedisStringClientImpl(ClientTypeEnum.STRING);
        System.out.println(impl.setWithExpireTime("lisi", "123", 3));
        while (true) {
            Thread.sleep(1000);
            System.out.println("获取数据：" + impl.get("lisi"));
        }
    }

    @Test
    public void test() {
        RedisStringClientImpl impl = new RedisStringClientImpl(ClientTypeEnum.STRING);
        boolean flag = impl.setNx("zhangsan", "123");
        System.out.println(flag);

        boolean flag2 = impl.setNx(UUID.randomUUID().toString(), "123");
        System.out.println(flag2);
    }

    @Test
    public void testSetEXNX() throws InterruptedException {
        RedisStringClientImpl impl = new RedisStringClientImpl(ClientTypeEnum.STRING);
        System.out.println(impl.set("zhangsan", "123", ExpireMode.EX, 3, XMode.NX));
        Thread.sleep(1000);
        System.out.println(impl.set("zhangsan", "123", ExpireMode.EX, 3, XMode.NX));
        Thread.sleep(1000);
        System.out.println(impl.set("zhangsan", "123", ExpireMode.EX, 3, XMode.NX));
        System.out.println(impl.set("zhangsan", "123", ExpireMode.EX, 3, XMode.NX));
    }
}
