package com.wsf.test;

import com.wsf.netty.rpc.common.model.Person;
import com.wsf.netty.rpc.common.service.HelloService;
import com.wsf.netty.rpc.common.service.PersonService;
import com.wsf.netty.rpc.consumer.client.RpcClient;
import com.wsf.netty.rpc.consumer.client.handler.AsyncRPCCallback;
import com.wsf.netty.rpc.consumer.client.handler.RpcFuture;
import com.wsf.netty.rpc.consumer.client.proxy.RpcService;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @author wsf
 * @since 20220526
 */
public class RpcCallbackTest {

    @Test
    public void test2() {
        RpcClient rpcClient = new RpcClient("124.223.109.220:2181");
        HelloService service = rpcClient.createService(HelloService.class, "1.0.0");
        String zhangsan = service.hello("zhangsan");
        System.out.println(zhangsan);
    }

    @Test
    public void test3() {
        final CountDownLatch countDownLatch = new CountDownLatch(1);

        RpcClient rpcClient = null;
        try {
            rpcClient = new RpcClient("124.223.109.220:2181");
            RpcService service = rpcClient.createAsyncService(PersonService.class, "1.0.0");
            RpcFuture rpcFuture = service.call("callPerson", "zhangsan", 5);
            rpcFuture.addCallback(new AsyncRPCCallback() {
                @Override
                public void success(Object result) {
                    List<Person> persons = (List<Person>) result;
                    for (int i = 0; i < persons.size(); ++i) {
                        System.out.println(persons.get(i));
                    }
                    countDownLatch.countDown();
                }

                @Override
                public void fail(Exception e) {
                    System.out.println(e);
                    countDownLatch.countDown();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        rpcClient.stop();
        System.out.println("End");
    }

}
