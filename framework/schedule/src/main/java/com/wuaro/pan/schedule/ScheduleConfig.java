package com.wuaro.pan.schedule;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 *  定时模块配置类:配置定时器执行器
 *
 *  注解：
 *      1. @SpringBootConfiguration：
 *          是 SpringBoot 项目的配置注解，这也是一个组合注解，
 *          SpringBootConfiguration 注解可以用 java 代码的形式实现 Spring 中 xml 配置文件配置的效果，
 *          并会将当前类内声明的一个或多个以 @Bean 注解标记的方法的实例纳入到 spring 容器中，并且实例名就是方法名。
 *
 *  方法：
 *      1. taskScheduler()
 *          返回值：线程池任务调度器ThreadPoolTaskScheduler对象
 *          作用：new一个ThreadPoolTaskScheduler对象
 *          知识点：
 *              1. ThreadPoolTaskScheduler：
 *                  是 spring taskSchedule 接口的实现，可以用来做定时任务使用。
 */

@SpringBootConfiguration
public class ScheduleConfig {

    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        return taskScheduler;
    }

}
