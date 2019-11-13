package com.example.demo;

import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class ZkDistributeLock implements Lock {

    private ZkClient client;

    private  String lockPath;

    public  ZkDistributeLock(String lockPath){
        if(lockPath == null || "".equals(lockPath)){
            throw  new IllegalArgumentException("path不能为空");
        }
        this.lockPath = lockPath;

        client = new ZkClient("localhost:2181");
        client.setZkSerializer(new MyZkSerializer());

    }

    /**
     * 尝试获得锁
     * @return
     */
    @Override
    public boolean tryLock() {
        try {
            client.createEphemeral(lockPath);
        }catch (Exception e){
            return false;
        }
        return true;
    }

    @Override
    public void lock() {
        if(!tryLock()){
            waitForLock();
            //从等待中醒过来，继续尝试获得锁
            lock();
        }

    }
    private void waitForLock(){
        CountDownLatch cdl = new CountDownLatch(1);
        IZkDataListener listener = new IZkDataListener() {
            @Override
            public void handleDataChange(String s, Object o) throws Exception {

            }

            @Override
            public void handleDataDeleted(String s) throws Exception {
                //有人释放了锁,唤醒阻塞的线程
                cdl.countDown();
            }
        };
        client.subscribeDataChanges(lockPath,listener);

        try {
            cdl.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        client.unsubscribeDataChanges(lockPath,listener);

    }

    @Override
    public void unlock() {
        client.delete(lockPath);

    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {

        return false;
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {

    }






    @Override
    public Condition newCondition() {
        return null;
    }
}
