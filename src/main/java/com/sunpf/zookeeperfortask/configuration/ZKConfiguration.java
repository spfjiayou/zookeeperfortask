package com.sunpf.zookeeperfortask.configuration;

import com.sunpf.zookeeperfortask.properties.ZookeeperProperties;
import com.sunpf.zookeeperfortask.util.ZKClient;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import sun.net.www.protocol.http.HttpURLConnection;

/**
 * 添加zookeeper调度信息
 */
@Configuration
public class ZKConfiguration {

    @Autowired
    private ZookeeperProperties zookeeperProperties;



    //创建zookeeper定时调度问题
    @Bean(initMethod = "init",destroyMethod = "stop")
    public ZKClient getZKClient(){
        //创建定时调度管理器如创建失败则返回空信息
        ZKClient zkClient = new ZKClient();
        zkClient.setBaseSleepTimeMs(zookeeperProperties.getBaseSleepTimeMs());
        zkClient.setConnectionTimeoutMs(zookeeperProperties.getConnectionTimeoutMs());
        zkClient.setMaxRetries(zookeeperProperties.getMaxRetries());
        zkClient.setRootPath(zookeeperProperties.getRootPath());
        zkClient.setZookeeperServer(zookeeperProperties.getServer());
        zkClient.setSessionTimeOutMs(zookeeperProperties.getSessionTimeoutMs());
        return zkClient;
    }

    //创建监听服务
    @Bean
    public PathChildrenCache getPathChildrenCache() throws Exception {
        //设置监听信息
        PathChildrenCache childrenCache = new PathChildrenCache(getZKClient().getClient(),"/task", true);
        return childrenCache;
    }


}
