package com.cugb.andy.rpc.client;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;

import org.springframework.util.StringUtils;

import com.cugb.andy.pojo.RpcRequest;
import com.cugb.andy.pojo.RpcResponse;
import com.cugb.andy.rpc.registry.ServiceDiscovery;

/**
 * Created by jbcheng on 3/3/17.
 */
public class RpcProxy {
    private ServiceDiscovery serviceDiscovery;

    public RpcProxy(ServiceDiscovery serviceDiscovery) {
        this.serviceDiscovery = serviceDiscovery;
    }

    @SuppressWarnings("unchecked")
    public <T> T create(Class<?> interfaceClazz) {
        return (T) Proxy.newProxyInstance(interfaceClazz.getClassLoader(),
                interfaceClazz.getInterfaces(), new InvocationHandler() {
                    public Object invoke(Object proxy, Method method, Object[] args)
                            throws Throwable {
                        RpcRequest request = new RpcRequest();
                        request.setParameters(args);
                        request.setMethodName(method.getName());
                        request.setRequestId(UUID.randomUUID().toString());
                        request.setParameterTypes(method.getParameterTypes());
                        request.setClassName(method.getDeclaringClass().getName());

                        String address = null;
                        if (serviceDiscovery != null) {
                            address = serviceDiscovery.discover();// 发现服务
                        }
                        if (StringUtils.isEmpty(address)) {
                            throw new Exception("can not find valid server service!");
                        }
                        String[] array = address.split(":");
                        String host = array[0];
                        int port = Integer.parseInt(array[1]);

                        RpcClient client = new RpcClient(host, port);
                        RpcResponse response = client.send(request);

                        if (response.getError() != null) {
                            throw response.getError();
                        } else {
                            return response.getResult();
                        }
                    }
                });
    }
}
