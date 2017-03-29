package com.cugb.andy.rpc.registry;

import org.springframework.util.StringUtils;


/**
 * Created by jbcheng on 3/3/17.
 */
public class ServiceRegistry {
    private String address;
    public ServiceRegistry(String registryAddress) {
        this.address = registryAddress;
    }

    public void register(String data) {
        if (!StringUtils.isEmpty(data)) {
            ZkHelper zk = new ZkHelper(address);
            zk.createNode(data);
        }
    }
}
