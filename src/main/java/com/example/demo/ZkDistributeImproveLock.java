package com.example.demo;

import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkNodeExistsException;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class ZkDistributeImproveLock implements Lock {

    private ZkClient client;

    private  String lockPath; // 父节点

    // 当前的序号
    private  ThreadLocal<String> currentPath = new ThreadLocal<>();

    //排在我前面的序号
    private  ThreadLocal<String> beforePath = new ThreadLocal<>();


    public ZkDistributeImproveLock(String lockPath){
        if(lockPath == null || "".equals(lockPath)){
            throw  new IllegalArgumentException("path不能为空");
        }
        this.lockPath = lockPath;

        client = new ZkClient("localhost:2181");
        client.setZkSerializer(new MyZkSerializer());
        if(!this.client.exists(lockPath)){
            try {

                this.client.createPersistent(lockPath,true);
            }catch (ZkNodeExistsException e){

            }
        }

    }

    /**
     * 尝试获得锁
     * @return
     */
    @Override
    public boolean tryLock() {

        if(this.currentPath.get() == null || !client.exists(this.currentPath.get())){
            String node =  client.createEphemeralSequential(lockPath,"locked");
            currentPath.set(node);
        }

        //获得所有子节点
        List<String> children = this.client.getChildren(lockPath);

        //排序
        Collections.sort(children);

        //判断当前节点是否最小
        if(currentPath.get().equals(lockPath+"/"+children.get(0))){
            return true;
        }else{
            //取到前一个
            //得到字节的索引号
            int curIndex = children.indexOf(currentPath.get().substring(lockPath.length()+1));
            String befNode = lockPath+"/"+children.get(curIndex-1);
            beforePath.set(befNode);
        }

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
        client.subscribeDataChanges(beforePath.get(),listener);

        if(this.client.exists(this.beforePath.get())){
            try {
                cdl.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        client.unsubscribeDataChanges(beforePath.get(),listener);

    }

    @Override
    public void unlock() {
        System.out.println(Thread.currentThread().getName()+
                "----------释放分布式锁");
        if(this.currentPath.get()!=null){
            client.delete(currentPath.get());
            this.currentPath.set(null);
        }

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
