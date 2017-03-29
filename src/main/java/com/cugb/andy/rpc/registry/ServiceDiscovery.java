package com.cugb.andy.rpc.registry;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by jbcheng on 3/3/17.
 */
public class ServiceDiscovery {
    private static final Logger logger = LoggerFactory.getLogger(ServiceDiscovery.class);
    private volatile List<String> dataList = new ArrayList<String>();// 线程可见

    public ServiceDiscovery(String registryAddress) {
        ZooKeeper zk = new ZkHelper(registryAddress).connectServer();
        if (null != zk) {
            watchNode(zk, ZkHelper.DATA_PATH);
        }
    }

    public String discover() {
        String data = null;
        int size = dataList.size();
        if (size > 0) {
            if (size == 1) {
                data = dataList.get(0);
                logger.info("using only data {}", data);
            } else {
                data = dataList.get(ThreadLocalRandom.current().nextInt(size));
                logger.info("using random data {}", data);
            }
        }
        return data;
    }

    private void watchNode(final ZooKeeper zk, final String DATA_PATH) {
        final String path = DATA_PATH.substring(0,DATA_PATH.lastIndexOf("/"));
        logger.info("node path {}",path);
        try {
            List<String> nodeList = zk.getChildren(path, new Watcher() {
                public void process(WatchedEvent watchedEvent) {
                    if (watchedEvent.getType() == Event.EventType.NodeChildrenChanged) {
                        watchNode(zk, path);
                    }
                }
            });
            List<String> dataList = new ArrayList<String>();
            logger.info("node data {}",nodeList);
            for (String node : nodeList) {
                byte[] bytes = zk.getData(path + "/" + node, false, null);
                dataList.add(new String(bytes));
            }
            logger.info("node data {}", dataList);
            this.dataList = dataList;
        } catch (KeeperException e) {
            logger.error("error", e);
        } catch (InterruptedException e) {
            logger.error("interrupted by:", e);
        }
    }
}
