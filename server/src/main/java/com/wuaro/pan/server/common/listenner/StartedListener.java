package com.wuaro.pan.server.common.listenner;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiOutput;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

/**
 * 项目启动成功日志打印监听器
 */

/*
    注解：
        1. @Component
            当一个类使用 @Component 注解标记时，Spring 容器会扫描应用程序中的组件，并将这些组件实例化并管理它们的生命周期。
            这样，您可以通过其他 Spring 注解如 @Autowired 或 @Resource 来自动装配这些组件，而无需显式地创建它们的实例。
            这里写了@Component，在其他地方就可以通过@Autowired 或 @Resource自动获取StartedListener的实例
        2. @Slf4j
            通过 @Slf4j 注解，我们无需显式声明日志记录器，而是直接使用 log 字段进行日志记录操作。
            根据需要使用不同的日志级别，例如 debug()、info()、warn() 和 error() 方法。
     接口：
        实现了 ApplicationListener<ApplicationReadyEvent> 接口，并重写了里面的一些方法

 */
@Component
@Slf4j
public class StartedListener implements ApplicationListener<ApplicationReadyEvent> {

    /**
     * 项目启动成功将会在日志中输出对应的启动信息
     *
     * @param applicationReadyEvent
     */
    /*
        注解：
            1. @Override
                是 Java 中的一个注解，用于标识一个方法是覆盖（重写）父类或接口中的方法。
                这里是重写了当前类所实现的 ApplicationListener<ApplicationReadyEvent> 接口中的 onApplicationEvent 方法
        参数：
            1. 一个ApplicationReadyEvent实例对象
                ApplicationReadyEvent 是 Spring Framework 中的一个事件类，用于表示应用程序已经准备好接收请求并处理它们的事件。
                通常，在应用程序启动过程中，Spring Boot 会触发多个事件，其中 ApplicationReadyEvent 事件表示应用程序已经启动完成并准备好对外提供服务。
                当应用程序启动完成后，ApplicationReadyEvent 事件会被发布到 Spring 的应用程序上下文中，
                从而允许开发人员在应用程序完全启动并准备好接收请求时执行特定的逻辑或任务。
                ApplicationReadyEvent 事件在应用程序启动完成后被触发，表示应用程序已经成功启动并准备好接收请求。
        执行逻辑：
            1.String serverPort = applicationReadyEvent.getApplicationContext().getEnvironment().getProperty("server.port");
                这行代码的作用是从应用程序上下文中获取环境属性 "server.port" 的值，并将其赋给字符串变量 serverPort。
                具体来说，这行代码做了以下几件事情：
                    通过 applicationReadyEvent 获取应用程序上下文，这是在应用程序准备就绪时触发的事件。
                    使用 getApplicationContext() 方法从 applicationReadyEvent 中获取应用程序上下文对象，这个对象包含了应用程序中的各种配置信息和 Bean 实例。
                    调用 getEnvironment() 方法获取应用程序上下文的环境对象，环境对象包含了应用程序运行时的所有属性和配置。
                    使用 getProperty("server.port") 方法从环境对象中获取名为 "server.port" 的属性的值，该属性通常用于指定应用程序的端口号。
                    将获取到的端口号值赋给字符串变量 serverPort，以便后续使用。
            2. String serverUrl = String.format("http://%s:%s", "127.0.0.1", serverPort);
                这行代码使用 String.format() 方法创建了一个 URL 字符串，其中包含了主机名、端口号等信息。
                参数 "http://%s:%s" 中包含了协议、主机名和端口号的格式，其中 %s 将被后面的参数替换。
                "127.0.0.1" 是主机名，通常表示本地主机，即当前运行应用程序的主机。
                serverPort 是应用程序的端口号，通过之前的代码获取到了。
                String.format() 方法会将占位符 %s 替换为后面传入的参数，得到完整的 URL 字符串。
                最终，这行代码会生成一个类似于 http://127.0.0.1:8080 的 URL 字符串，其中 8080 是变量 serverPort 中保存的端口号。
            3. log.info(AnsiOutput.toString(AnsiColor.BRIGHT_BLUE, "r pan server started at: ", serverUrl));
                这行代码是使用 SLF4J 的日志记录器（通常是 Logback、Log4j 或其他实现）打印日志信息。
                具体来说，它使用了 log.info() 方法来记录一条 INFO 级别的日志。
                在这里，AnsiOutput.toString() 方法将字符串和 ANSI 颜色代码转换为带有 ANSI 颜色的字符串，然后通过日志记录器记录下来。
                AnsiColor.BRIGHT_BLUE 是一个 ANSI 颜色代码，表示亮蓝色。
                整个日志消息是："r pan server started at: " + serverUrl。
                这里 serverUrl 是之前生成的应用程序 URL 地址，表示应用程序启动并监听请求的地址。
                综合起来，这行代码的作用是打印一条信息，指示应用程序已经启动并开始监听请求的 URL 地址。
            4. if (checkShowServerDoc(applicationReadyEvent.getApplicationContext())) {
                    log.info(AnsiOutput.toString(AnsiColor.BRIGHT_BLUE, "r pan server's doc started at:", serverUrl + "/doc.html"));
                }
                checkShowServerDoc()是下面自定义的方法，返回true则表示需要展示服务器文档，返回false表示不需要展示服务器文档
                如果返回true，则用AnsiColor.BRIGHT_BLUE（亮蓝色）打印日志消息是 "r pan server's doc started at: " + serverUrl + "/doc.html"。
                其中serverUrl是之前生成的应用程序 URL 地址，表示应用程序启动并监听请求的地址。
            5. log.info(AnsiOutput.toString(AnsiColor.BRIGHT_YELLOW, "r pan server has started successfully!"));
                打印AnsiColor.BRIGHT_YELLOW（亮黄色）的日志消息："r pan server has started successfully!"
     */
    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        String serverPort = applicationReadyEvent.getApplicationContext().getEnvironment().getProperty("server.port");
        String serverUrl = String.format("http://%s:%s", "127.0.0.1", serverPort);
        log.info(AnsiOutput.toString(AnsiColor.BRIGHT_BLUE, "r pan server started at: ", serverUrl));
        if (checkShowServerDoc(applicationReadyEvent.getApplicationContext())) {
            log.info(AnsiOutput.toString(AnsiColor.BRIGHT_BLUE, "r pan server's doc started at:", serverUrl + "/doc.html"));
        }
        log.info(AnsiOutput.toString(AnsiColor.BRIGHT_YELLOW, "r pan server has started successfully!"));
    }

    /**
     * 校验是否开启了接口文档
     * 返回true则表示需要展示服务器文档，返回false表示不需要展示服务器文档
     *
     * @param applicationContext
     * @return
     */
    /*
        权限：
            private，此方法是为该类中的其他方法（如onApplicationEvent）服务，所以设为private
        参数：
            接受一个 ConfigurableApplicationContext 类型的参数
        返回：
            返回一个布尔值
        作用：
            检查是否需要展示服务器文档（Server Doc）。
        执行逻辑：
            1. 使用applicationContext.getEnvironment() 方法，获取到当前应用程序上下文的环境对象，然后可以使用环境对象进行配置属性的读取和设置
            2. 使用getProperty("swagger2.show", Boolean.class, true) 方法获取名为 "swagger2.show" 的配置属性，
                该属性是一个布尔值，如果配置不存在，则默认为 true。
            3. 关于"swagger2.show" 的配置属性：
                Swagger2ConfigProperties是swagger2配置属性实体类，该类中有show属性，为boolean类型
                该类上标注了@ConfigurationProperties(prefix = "swagger2")注解，
                故：可以通过"swagger2.show"来找到该类中的show属性
                注意：鼠标放到"swagger2.show" 按住ctrl并点击，可以查看Swagger2ConfigProperties类
            4. 使用 applicationContext.containsBean("swagger2Config") 方法检查是否存在名为 "swagger2Config" 的 Bean。如果有则返回true
            5. 由于如果以上两个条件都满足，则相当于true&&true
                则 此方法返回 true，表示需要展示服务器文档；否则返回 false，表示不需要展示服务器文档。
     */
    private boolean checkShowServerDoc(ConfigurableApplicationContext applicationContext) {
        return applicationContext.getEnvironment().getProperty("swagger2.show", Boolean.class, true) && applicationContext.containsBean("swagger2Config");
    }

}
