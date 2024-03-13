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
 *      1. startTask(ScheduleTask scheduleTask, String cron)
 *          作用：开启一个定时任务
 *          参数：
 *              1. ScheduleTask scheduleTask
 *                  这里应该是传进来一个ScheduleTask接口的实现类对象，刚好ScheduleTask接口继承了Runnable接口，符合
 *              2. String cron
 *                  定时任务的cron表达式，类似如下格式：
 *                      0/5 * * * * ?
 *          内部细节：
 *              1. ScheduledFuture<?> scheduledFuture = taskScheduler.schedule(scheduleTask, new CronTrigger(cron));
 *                  taskScheduler是类中定义的 ThreadPoolTaskScheduler对象 成员变量
 *                  关于schedule()方法：
 *                      ThreadPoolTaskScheduler类的schedule()方法用于在一个线程池中安排执行定时任务。
 *                      它允许你在指定的延迟后执行任务，也可以设置任务的执行周期。
 *                      通常用于在后台执行一些周期性的任务，比如定时清理缓存、发送定时通知等。
 *              2. String key = UUIDUtil.getUUID();
 *                  作用：上面定义了一个名为cache的map类型成员变量，生成一个独一无二的key，后面会存入到这个map中
 *                  关于UUIDUtil.getUUID()方法：
 *                      用于生成唯一标识符(UUID)的工具方法。
 *                      UUID (Universally Unique Identifier) 是一种标准化的格式，用于唯一标识信息。
 *                      它是一个128位的数字，通常以32个十六进制数字的形式表示，如 `550e8400-e29b-41d4-a716-446655440000`。
 *                      总之，`UUIDUtil.getUUID()` 方法的主要作用就是生成一个全局唯一的标识符，用于标识应用程序中的各种实体或事件，从而保证系统的数据一致性和唯一性。
 *              3. ScheduleTaskHolder holder = new ScheduleTaskHolder(scheduleTask, scheduledFuture);
 *                  ScheduleTaskHolder是我们自己定义的类，用于存放执行定时任务的实体 和 执行任务的结果实体，也就是该构造方法传入的两个参数
 *                  也就是说ScheduleTaskHolder对象存放了一个定时任务的所有信息
 *              4. cache.put(key, holder);
 *                  将此次定时任务的独一无二的key 和 包含所有信息的holder对象存入map中
 *              5. log.info("{} 启动成功！唯一标识为：{}", scheduleTask.getName(), key);
 *                  打印日志，表达一下定时任务启动成功了
 *              6. 最终返回任务独一无二的key值
 *      2. stopTask(String key)
 *          作用：停止一个定时任务
 *          参数：任务的唯一标识key
 *          内部细节：
 *              1. if (StringUtils.isBlank(key)) {  return; }
 *                  StringUtils.isBlank(key) 是 Apache Commons Lang 库中的一个静态方法，
 *                  用于检查字符串是否为 null、空字符串或仅包含空白字符。
 *                  如果字符串为 null 或长度为 0，或者字符串中只包含空白字符（空格、制表符、换行符等），则返回 true，否则返回 false。
 *                  所以这句话的意思就是如果该key不存在（也就是这个定时任务不存在），则什么也不做直接返回、
 *              2. ScheduleTaskHolder holder = cache.get(key);
 *                  if (Objects.isNull(holder)) {
 *                      return;
 *                  }
 *                  如果key不为空，则从map中取出对应的holder
 *                  如果key不为null，但是任务实体holder是null，那么也直接返回
 *              3. ScheduledFuture scheduledFuture = holder.getScheduledFuture();
 *                  boolean cancel = scheduledFuture.cancel(true);
 *                  从取出的holder中用get方法获取 执行任务的结果实体scheduledFuture，并执行cancel方法关闭该任务
 *                  返回一个boolean类型的值（true代表关闭成功，false表示关闭失败）
 *                  关于scheduledFuture.cancel(true)：
 *                      用于取消与此 ScheduledFuture 关联的任务的执行。
 *                      具体地说，这个方法会尝试取消任务的执行，如果任务尚未开始，那么任务将被取消；
 *                      如果任务已经开始执行，那么根据传入的参数决定是否中断任务的执行：
 *                          传入true，则尝试中断正在执行的任务
 *                          传入false，则不会中断任务的执行
 *              4. 根据关闭成功/失败，打印不同的日志
 *      3. changeTask(String key, String cron)
 *          作用：更新一个定时任务的执行时间
 *          参数：定时任务的唯一标识key、新的cron表达式cron
 *          返回：更新后任务的key
 *          内部细节：
 *          1. if (StringUtils.isAnyBlank(key, cron)) {
 *                  throw new RPanFrameworkException("定时任务的唯一标识以及新的执行表达式不能为空");
 *             }
 *              这个方法的作用是检查多个字符串中是否至少有一个为空白
 *              也就是key和cron这两个字符串都必须非空，否则抛出异常
 *          2. ScheduleTaskHolder holder = cache.get(key);
 *              if (Objects.isNull(holder)) {
 *                  throw new RPanFrameworkException(key + "唯一标识不存在");
 *              }
 *              stopTask(key);
 *              key不唯一则从map中取出对应的holder，如果holder为null则抛出异常，不为null则终止任务
 *          3. return startTask(holder.getScheduleTask(), cron);
 *              重新按照新的cron表达式创建新的定时任务
 *              新建一个定时任务会创建一个新的key，返回这个key
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
