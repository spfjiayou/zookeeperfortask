package com.sunpf.zookeeperfortask.controller;

import com.sunpf.zookeeperfortask.util.ZKClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TaskController {

    @Autowired
    private ZKClient zkClient;

    @RequestMapping("/taskone")
    public String checktaskOne() throws Exception {
        zkClient.getClient().setData().forPath("/task/mytaskOne","{\"cron\":\"0/6 * * * * ?\",\"task_name\":\"mytaskOne\",\"classpath\":\"com.sunpf.taskdatesourcetest.task.MytaskOne\",\"current_thead\":\"N\",\"isruning\":\"N\",\"task_id\":\"mytaskone\",\"uUID\":\"111111\",\"ex_method\":\"dotask\",\"status\":\"0\"}\n".getBytes());
        return "success";
    }





}
