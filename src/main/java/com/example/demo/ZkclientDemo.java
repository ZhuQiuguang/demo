package com.example.demo;

import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.IZkStateListener;
import org.I0Itec.zkclient.ZkClient;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.Watcher;

import java.util.List;

public class ZkclientDemo {
    public static void main(String[] args) {

        ZkClient client = new ZkClient("localhost:2181");
        client.setZkSerializer(new MyZkSerializer());
        client.create("/zk/app6","128", CreateMode.PERSISTENT);
        client.subscribeChildChanges("/zk/app6", new IZkChildListener() {
            @Override
            public void handleChildChange(String s, List<String> list) throws Exception {
                System.out.println("s = " + s + ", 子节点发生变化 list = " + list);
            }
        });
        client.subscribeDataChanges("/zk/app6", new IZkDataListener() {
            @Override
            public void handleDataChange(String s, Object o) throws Exception {
                System.out.println("s = " + s + ", 数据发生变化 o = " + o);
            }

            @Override
            public void handleDataDeleted(String s) throws Exception {
                System.out.println("s = " + s + ", 节点被删除 ");
            }
        });
        client.subscribeStateChanges(new IZkStateListener() {
            @Override
            public void handleStateChanged(Watcher.Event.KeeperState keeperState) throws Exception {
                System.out.println("keeperState = " + keeperState);
            }

            @Override
            public void handleNewSession() throws Exception {
                System.out.println("处理新的会话");
            }

            @Override
            public void handleSessionEstablishmentError(Throwable throwable) throws Exception {
                throwable.printStackTrace();
            }
        });


        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}

