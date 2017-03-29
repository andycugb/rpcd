package com.cugb.andy.rpc.registry;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.lang3.StringUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by jbcheng on 3/3/17.
 * zk 操作
 */
public class ZkHelper {
    private final int TIME_OUT = 1000 * 30;
    public static final String DATA_PATH = "/com/cugb/andy/data";
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private String address;
    private CountDownLatch latch = new CountDownLatch(1);

    public ZkHelper() {

    }

    public ZkHelper(String registryAddress) {
        this.address = registryAddress;
    }

    public ZooKeeper connectServer() {
        ZooKeeper zk = null;
        try {
            zk = new ZooKeeper(address, TIME_OUT, new Watcher() {
                public void process(WatchedEvent watchedEvent) {
                    if (watchedEvent.getState() == Event.KeeperState.SyncConnected) {
                        latch.countDown();
                    }
                }
            });
            latch.await();
        } catch (IOException e) {
            logger.error("failed connect to zk server at[" + address + "],caused by:", e);
        } catch (InterruptedException e) {
            logger.error("connection interrupt:", e);
        }
        return zk;
    }

    public void createNode(String data) {
        byte[] bytes;
        try {
            ZooKeeper zk = connectServer();
            if (DATA_PATH.contains("/")) {
                String path = "";
                String temp = DATA_PATH;
                String[] parent = StringUtils.split(temp,"/");
                int len = parent.length;
                boolean last = false;
                for (int index = 0; index < len ;index ++) {
                    path = path + "/" + parent[index];
                    bytes = path.getBytes();
                    if (index == len -1) {
                        last = true;
                        bytes = data.getBytes();
                    }
                    if (null == zk.exists(path,null)) {
                        zk.create(path, bytes, ZooDefs.Ids.OPEN_ACL_UNSAFE, last ? CreateMode.EPHEMERAL_SEQUENTIAL :CreateMode.PERSISTENT);
                        logger.info("create zk node,path:" + path + ",data:" + new String(bytes));
                    }
                }
            }
        } catch (KeeperException e) {
            logger.error("failed create node at path,caused by:", e);
        } catch (InterruptedException e) {
            logger.error("create node interrupt:", e);
        }
    }
    public static void main(String[] args) {
        System.out.println(Arrays.toString(StringUtils.split(DATA_PATH, "/")));
    }
}
