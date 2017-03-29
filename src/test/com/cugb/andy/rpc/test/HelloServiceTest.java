package com.cugb.andy.rpc.test;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.cugb.andy.rpc.client.RpcProxy;
import com.cugb.andy.service.HelloService;

/**
 * Created by jbcheng on 3/3/17.
 */
public class HelloServiceTest {

    private static RpcProxy rpcProxy;
    private static HelloService clazz;
    @BeforeClass
    public static void init() {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("spring-zk-rpc-client.xml");
        rpcProxy = (RpcProxy) ctx.getBean("rpcProxy");
        clazz = (HelloService) ctx.getBean("helloService");
    }
    @Test
    public void helloTest() {
        HelloService service = rpcProxy.create(clazz.getClass());
        String result = service.hello("Hello World!");
        Assert.assertEquals("Hello Word!", result);
    }

}
