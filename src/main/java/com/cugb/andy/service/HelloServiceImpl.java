package com.cugb.andy.service;

import com.cugb.andy.annotation.RpcService;

/**
 * Created by jbcheng on 9/5/16. webservice接口实现类
 */
@RpcService(value = HelloService.class)
public class HelloServiceImpl implements HelloService {

    public String hello(String word) {
        System.out.println("This is a test demo");
        return "Say:" + word;
    }
}
