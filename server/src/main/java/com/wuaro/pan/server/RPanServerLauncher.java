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
@RestController
@Api("测试接口类")
@Validated
public class RPanServerLauncher {

    public static void main(String[] args) {
        SpringApplication.run(RPanServerLauncher.class);
    }

    @GetMapping("hello")
    public R<String> hello(@NotNull(message = "name不能为空") String name) {
        return R.data("hello " + name + "!");
    }

}
