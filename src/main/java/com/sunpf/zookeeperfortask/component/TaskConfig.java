package com.sunpf.zookeeperfortask.component;

import com.alibaba.fastjson.JSON;
import com.sunpf.zookeeperfortask.inter.mapper.TaskDatasourceMapper;
import com.sunpf.zookeeperfortask.inter.pojo.TaskDatasource;
import com.sunpf.zookeeperfortask.util.SpringUtils;
import com.sunpf.zookeeperfortask.util.ZKClient;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * 定时任务启动
 */
@Component
public class TaskConfig {

    private final static Logger logger = LoggerFactory.getLogger(TaskConfig.class);

    private final String rootPath = "/task";

    private final String localPath = "one";

    //注入zookeeper信息
    @Autowired
    private ZKClient zkClient;


    //注入mapper
    @Autowired
    private TaskDatasourceMapper taskDatasourceMapper;


    //注入定时任务线程
    @Autowired
    private ThreadPoolTaskScheduler threadPoolTaskScheduler;

    //设置监听服务
    @Autowired
    private PathChildrenCache pathChildrenCache;


    //用来限制定时任务启停操作
    private Map<String,ScheduledFuture> scheduledFutureMap = new ConcurrentHashMap<>();



    public void startTask() throws Exception {
        //获取对应的zookeeper中的数据信息
        List<String> children = zkClient.getChildren(rootPath);
        for (String str:children){
            //获取对应的定时任务进行判断
            try {
                String data = new String(zkClient.getClient().getData().forPath(rootPath+ "/" + str));
                //进行数据转换
                TaskDatasource taskDatasource = JSON.parseObject(data, TaskDatasource.class);
                //判断现有定时任务是否需要进行添加
                if (checkTaskadd(taskDatasource)){
                    //添加定时任务
                    addTask(taskDatasource);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        addListener();
    }
    //判断定时任务调度
    private boolean checkTaskadd(TaskDatasource taskDatasource) {
        //判断定时任务状态是否需要添加到定时任务管理器中
        if (taskDatasource!=null&&taskDatasource.getStatus().equals("1"))
            return true;
        return false;
    }
    //添加定时任务runner

    private Runnable getRunnable(TaskDatasource taskDatasource){
        return new Runnable() {
            @Override
            public void run() {
                Object object = SpringUtils.getBean(taskDatasource.getTask_name());
                //判断bean是否存在
                if (object!=null){
                    Method method = null;
                    try {
                        method = object.getClass().getMethod(taskDatasource.getEx_method());
                        method.invoke(object);
                    } catch (NoSuchMethodException e) {
                        logger.error("执行出现错误！"+e);
                    } catch (IllegalAccessException e) {
                        logger.error("执行出现错误！"+e);
                    } catch (InvocationTargetException e) {
                        logger.error("执行出现错误！"+e);
                    }
                }
            }
        };
    }


    private Trigger getTrigger(TaskDatasource taskDatasource){
        return new Trigger() {
            @Override
            public Date nextExecutionTime(TriggerContext triggerContext) {
                CronTrigger trigger = new CronTrigger(taskDatasource.getCron());
                Date nextdate = trigger.nextExecutionTime(triggerContext);
                return nextdate;
            }
        };
    }



    //删除定时任务
    public void delTask(String taskName) throws Exception {
        ScheduledFuture scheduledFuture = scheduledFutureMap.get(taskName);
        if (scheduledFuture!=null){
            scheduledFuture.cancel(true);
            scheduledFutureMap.remove(taskName);
        }
        //判断zookeeper下是否有其临时节点，如果有进行删除操作
        Stat stat = zkClient.getClient().checkExists().forPath(rootPath + "/" + taskName +"/"+localPath);
        if (stat != null)
            zkClient.getClient().delete().forPath(rootPath + "/" + taskName + "/" + localPath);

    }

    //添加定时任务
    public void addTask(TaskDatasource taskDatasource) throws Exception {
        ScheduledFuture<?> schedule = threadPoolTaskScheduler.schedule(getRunnable(taskDatasource), getTrigger(taskDatasource));
        scheduledFutureMap.put(taskDatasource.getTask_name(),schedule);
        //对其临时节点进行
        Stat stat = zkClient.getClient().checkExists().forPath(rootPath + "/" + taskDatasource.getTask_name() + "/" + localPath);
        if (stat==null){
            zkClient.getClient().create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(rootPath+"/"+taskDatasource.getTask_name()+"/"+localPath,taskDatasource.getCron().getBytes());
        }else {
            zkClient.getClient().setData().forPath(rootPath+"/"+taskDatasource.getTask_name()+"/"+localPath,taskDatasource.getCron().getBytes());
        }
    }




    //启动监听服务,监听添加对子节点的增删改的监听
    public void addListener() throws Exception {
        pathChildrenCache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);

        pathChildrenCache.getListenable().addListener(new PathChildrenCacheListener() {
            @Override
            public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent pathChildrenCacheEvent) throws Exception {
                if (pathChildrenCacheEvent.getType().equals(PathChildrenCacheEvent.Type.INITIALIZED)){
                    logger.info("子节点初始化完毕");
                }else if (pathChildrenCacheEvent.getType().equals(PathChildrenCacheEvent.Type.CHILD_ADDED)){
                    //添加子节点
                    logger.info("添加子节点信息:"+pathChildrenCacheEvent.getData().getPath() + "data:" + pathChildrenCacheEvent.getData().getData());
                    String jsondata = new String(pathChildrenCacheEvent.getData().getData());
                    TaskDatasource taskDatasource = JSON.parseObject(jsondata, TaskDatasource.class);
                    if (checkTaskadd(taskDatasource))
                        addTask(taskDatasource);
                }else if (pathChildrenCacheEvent.getType().equals(PathChildrenCacheEvent.Type.CHILD_UPDATED)){
                    //修改节点
                    logger.info("节点信息修改:"+pathChildrenCacheEvent.getData().getPath()+"data:"+pathChildrenCacheEvent.getData().getData());
                    String jsondata = new String(pathChildrenCacheEvent.getData().getData());
                    TaskDatasource taskDatasource = JSON.parseObject(jsondata, TaskDatasource.class);
                    //先删除在进行添加操作
                    delTask(taskDatasource.getTask_name());
                    if (checkTaskadd(taskDatasource))
                        addTask(taskDatasource);
                }else if (pathChildrenCacheEvent.getType().equals(PathChildrenCacheEvent.Type.CHILD_REMOVED)){
                    //删除节点
                    logger.info("节点删除:"+pathChildrenCacheEvent.getData().getPath());
                    //获取对应的节点进行相应的操作
                    String path = pathChildrenCacheEvent.getData().getPath();
                    String[] task_name = path.split("/");
                    delTask(task_name[task_name.length-1]);
                }
            }
        });


    }

}
