package com.wuaro.pan.schedule;

import com.wuaro.pan.core.exception.RPanFrameworkException;
import com.wuaro.pan.core.utils.UUIDUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;


/**
 * 定时任务管理器类：
 *      1、创建并启动一个定时任务
 *      2、停止一个定时任务
 *      3、更新一个定时任务
 * 注解：
 *      1. @Component
 *          在Java中，@Component注解用于将一个普通的Java类标记为Spring容器中的一个组件。
 *          Spring容器会自动扫描带有@Component注解的类，创建对应的对象并将其加入到容器中进行管理。
 *          如果在实现类中用到了@Autowired注解，被注解的这个类是从Spring容器中取出来的，那调用的实现类也需要被Spring容器管理，加上@Component
 *      2. @Autowired
 *          这里@Autowired注解的意思就是，当Spring发现@Autowired注解时，
 *          将自动在代码上下文中找到和其匹配（默认是类型匹配）的Bean，并自动注入到相应的地方去。
 *      3. @Slf4j
 *          加了@Slf4j注解就能使用日志功能
 *          下面代码中能使用log.info()方法，就是因为引入了@Slf4j注解
 *
 * 成员变量：
 *      1. taskScheduler
 *          类型：ThreadPoolTaskScheduler类型的 线程池任务调度器 对象（并使用@Autowired注解来自动装配）
 *      2. cache
 *          作用：作为内部正在执行的定时任务缓存
 *          类型：Map<String, ScheduleTaskHolder>类型的ConcurrentHashMap对象
 *          map的key为字符串类型、value为ScheduleTaskHolder类型
 *              ScheduleTaskHolder是我们自己定义的定时任务和定时任务结果的缓存对象类
 *          为什么使用ConcurrentHashMap，不用HashMap？
 *              ConcurrentHashMap是属于JUC工具包中的并发容器之一，在多线程开发中很经常会使用到这个类，
 *              它与HashMap的区别是：HashMap是线程不安全的，在高并发的情况下，
 *              使用HashMap进行大量变更操作容易出现问题，但是ConcurrentHashMap是线程安全的。
 *  方法：
 *      1. startTask()
 *          参数：
 *              1. ScheduleTask scheduleTask
 *
 */
@Component
@Slf4j
public class ScheduleManager {

    //自动装配一个 线程池任务调度器 对象
    @Autowired
    private ThreadPoolTaskScheduler taskScheduler;

    /**
     * 内部正在执行的定时任务缓存
     */
    private Map<String, ScheduleTaskHolder> cache = new ConcurrentHashMap<>();

    /**
     * 启动一个定时任务
     *
     * @param scheduleTask 定时任务实现类
     * @param cron         定时任务的cron表达式
     * @return
     */
    public String startTask(ScheduleTask scheduleTask, String cron) {
        ScheduledFuture<?> scheduledFuture = taskScheduler.schedule(scheduleTask, new CronTrigger(cron));
        String key = UUIDUtil.getUUID();
        ScheduleTaskHolder holder = new ScheduleTaskHolder(scheduleTask, scheduledFuture);
        cache.put(key, holder);
        log.info("{} 启动成功！唯一标识为：{}", scheduleTask.getName(), key);
        return key;
    }

    /**
     * 停止一个定时任务
     *
     * @param key 定时任务的唯一标识
     */
    public void stopTask(String key) {
        if (StringUtils.isBlank(key)) {
            return;
        }
        ScheduleTaskHolder holder = cache.get(key);
        if (Objects.isNull(holder)) {
            return;
        }
        ScheduledFuture scheduledFuture = holder.getScheduledFuture();
        boolean cancel = scheduledFuture.cancel(true);
        if (cancel) {
            log.info("{} 停止成功！唯一标识为：{}", holder.getScheduleTask().getName(), key);
        } else {
            log.error("{} 停止失败！唯一标识为：{}", holder.getScheduleTask().getName(), key);
        }
    }

    /**
     * 更新一个定时任务的执行时间
     *
     * @param key  定时任务的唯一标识
     * @param cron 新的cron表达式
     * @return
     */
    public String changeTask(String key, String cron) {
        if (StringUtils.isAnyBlank(key, cron)) {
            throw new RPanFrameworkException("定时任务的唯一标识以及新的执行表达式不能为空");
        }
        ScheduleTaskHolder holder = cache.get(key);
        if (Objects.isNull(holder)) {
            throw new RPanFrameworkException(key + "唯一标识不存在");
        }
        stopTask(key);
        return startTask(holder.getScheduleTask(), cron);
    }

}
