package com.sunpf.zookeeperfortask.util;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class ZKClient {
    private final static Logger logger = LoggerFactory.getLogger(ZKClient.class);

    private CuratorFramework client;
    private String zookeeperServer;
    private int sessionTimeOutMs;
    private int connectionTimeoutMs;
    private int baseSleepTimeMs;
    private int maxRetries;
    private String rootPath;

    //初始化zkclient
    public void init(){
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(baseSleepTimeMs,maxRetries);
        client = CuratorFrameworkFactory.builder().connectString(zookeeperServer).retryPolicy(retryPolicy).sessionTimeoutMs(sessionTimeOutMs).connectionTimeoutMs(connectionTimeoutMs).build();
        client.start();
    }

    public void stop(){
        client.close();
    }

    //获取client信息
    public CuratorFramework getClient(){
        return client;
    }

    public void register(){
        try {
            String rootPath = "/" + getRootPath();
            String hostAddress = InetAddress.getLocalHost().getHostAddress();
            String serviceInstance = "promethrus" + "-" + hostAddress + "-";
            client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(rootPath+"/" + serviceInstance);
        }catch (Exception e){
            logger.error("注册出错",e);
        }
    }

    //获取子节点信息
    public List<String> getChildren(String path){
        List<String> childrenList = new ArrayList<>();
        //获取所有的子节点信息
        try {
            childrenList = client.getChildren().forPath(path);
        } catch (Exception e) {
            logger.error("获取子节点失败",e);
        }
        return childrenList;
    }
    //获取子节点个数
    public int getChildrenCount(String path){
        return getChildren(path).size();
    }




    public void setClient(CuratorFramework client) {
        this.client = client;
    }

    public void setZookeeperServer(String zookeeperServer) {
        this.zookeeperServer = zookeeperServer;
    }

    public void setSessionTimeOutMs(int sessionTimeOutMs) {
        this.sessionTimeOutMs = sessionTimeOutMs;
    }

    public void setConnectionTimeoutMs(int connectionTimeoutMs) {
        this.connectionTimeoutMs = connectionTimeoutMs;
    }

    public void setBaseSleepTimeMs(int baseSleepTimeMs) {
        this.baseSleepTimeMs = baseSleepTimeMs;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }

    public static Logger getLogger() {
        return logger;
    }

    public String getZookeeperServer() {
        return zookeeperServer;
    }

    public int getSessionTimeOutMs() {
        return sessionTimeOutMs;
    }

    public int getConnectionTimeoutMs() {
        return connectionTimeoutMs;
    }

    public int getBaseSleepTimeMs() {
        return baseSleepTimeMs;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public String getRootPath() {
        return rootPath;
    }
}
