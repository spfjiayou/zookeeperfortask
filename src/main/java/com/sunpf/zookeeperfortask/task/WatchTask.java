package com.sunpf.zookeeperfortask.task;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class WatchTask {

    private final static Logger logger = LoggerFactory.getLogger(WatchTask.class);




    public void dotask(){
        logger.info("守护定时任务启动定时任务已经启动"+ LocalDateTime.now() );
        //获取所有的定时任务进行判断，判断其是需要添加或者删除
        logger.info("守护定时任务完成。。。。");

    }
}
