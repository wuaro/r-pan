package com.wuaro.pan.swagger2;

import com.wuaro.pan.core.constants.RPanConstants;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * swagger2配置属性实体
 */

/*
    注解：
        1. @Data ： 注在类上，提供类的get、set、equals、hashCode、canEqual、toString方法
            拓展：
                注解@AllArgsConstructor ： 注在类上，提供类的全参构造
                注解@NoArgsConstructor ： 注在类上，提供类的无参构造
                注解@Setter ： 注在属性上，提供 set 方法
                注解@Getter ： 注在属性上，提供 get 方法
                注解@EqualsAndHashCode ： 注在类上，提供对应的 equals 和 hashCode 方法
                注解@Log4j/@Slf4j ： 注在类上，提供对应的 Logger 对象，变量名为 log
        2. @Component
            将一个普通类注入Spring容器
            普通类也就是：不是controller层也不是service...
            总是就是不能通过业务进行归类的类，就可以用@Component注入Spring容器
        3. @ConfigurationProperties(prefix = "swagger2")
            是一个 Spring Boot 中用来将配置文件中的属性映射到 Java 类的注解。
            在这里，prefix = "swagger2" 意味着会将以 swagger2 开头的配置属性映射到标注了 @ConfigurationProperties 注解的 Java 类中的对应字段或者属性上。
 */
@Data
@Component
@ConfigurationProperties(prefix = "swagger2")
public class Swagger2ConfigProperties {

    /**
     * swagger文件是否展示
     */
    private boolean show = true;
    /**
     * 组名称
     */
    private String groupName = "r-pan";
    /**
     * 基础扫描路径（这里是使用com.wuaro.pan.core.constants.RPanConstants常量类中事先配置好的路径）
     */
    private String basePackage = RPanConstants.BASE_COMPONENT_SCAN_PATH;
    /**
     * title
     */
    private String title = "r-pan-server";
    /**
     * 描述
     */
    private String description = "r-pan-server";
    /**
     * url
     */
    private String termsOfServiceUrl = "http://127.0.0.1:${server.port}";
    /**
     * 联系人
     */
    private String contactName = "wuaro";
    /**
     * 联系地址
     */
    private String contactUrl = "https://wuaro.github.io/";
    /**
     * 联系人邮箱
     */
    private String contactEmail = "1139188981@qq.com";
    /**
     * 版本
     */
    private String version = "1.0";

}
