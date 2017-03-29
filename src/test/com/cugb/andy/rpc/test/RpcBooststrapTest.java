package com.cugb.andy.rpc.test;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by jbcheng on 3/3/17.
 */
public class RpcBooststrapTest {

    public static void main(String[] args) {
        new ClassPathXmlApplicationContext("spring-zk-rpc-server.xml");
    }
}
