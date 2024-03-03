package com.wuaro.pan.server;



import com.wuaro.pan.core.constants.RPanConstants;
import com.wuaro.pan.core.response.R;
import io.swagger.annotations.Api;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;

@SpringBootApplication(scanBasePackages = {RPanConstants.BASE_COMPONENT_SCAN_PATH})
@ServletComponentScan(basePackages = RPanConstants.BASE_COMPONENT_SCAN_PATH)
/**
 * RestController是@ResponseBody和@Controller的组合注解。
 */
@RestController
/**
 * Api：用在请求的类上，表示对类的说明
 *     tags="说明该类的作用，可以在UI界面上看到的注解"
 *     value="该参数没什么意义，在UI界面上也看到，所以不需要配置"
 * 此处表示该类为测试接口类
 */
@Api("测试接口类")
/**
 * Validated：提供了一个分组功能，可以在入参验证时，根据不同的分组采用不同的验证机制
 * Validated：可以用在类型、方法和方法参数上。但是不能用在成员属性（字段）上
 * 加了这个注解，就可以在该方法里使用入参校验
 */
@Validated
public class RPanServerLauncher {

    public static void main(String[] args) {
        SpringApplication.run(RPanServerLauncher.class);
    }

    /*
    @GetMapping用于定义为 HTTP GET 请求方法映射的注解，
    该注解的作用是将 HTTP GET 请求映射到特定的处理器方法上。
    当访问http://localhost:8080/hello时，就会执行这个方法
     */
    @GetMapping("hello")
    /*
    @NotNull(message = "name不能为空") String name
    如果那么为空就会报错，显示信息"name不能为空"
    这是方法上加了@Validated注解后才能使用的入参校验
     */
    public R<String> hello(@NotNull(message = "name不能为空") String name) {
        return R.data("hello " + name + "!");
    }

}
