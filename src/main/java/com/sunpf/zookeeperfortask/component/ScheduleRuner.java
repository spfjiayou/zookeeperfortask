package com.sunpf.zookeeperfortask.component;

import com.sunpf.zookeeperfortask.util.ZKClient;
import org.apache.zookeeper.CreateMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 用来进行定时任务的启动
 */
@Component
public class ScheduleRuner implements ApplicationRunner {

    //添加定时任务调度管理
    @Autowired
    private TaskConfig taskConfig;


    @Autowired
    private ZKClient zkClient;



    @Override
    public void run(ApplicationArguments args) throws Exception {

        //启动定时任务
        taskConfig.startTask();
    }
}
