package com.pan.server;

import com.wuaro.pan.core.constants.RPanConstants;
import com.wuaro.pan.core.response.R;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication(scanBasePackages = {RPanConstants.BASE_COMPONENT_SCAN_PATH})
@ServletComponentScan(basePackages = RPanConstants.BASE_COMPONENT_SCAN_PATH)
@RestController
public class RPanServerLauncher {

    public static void main(String[] args) {
        SpringApplication.run(RPanServerLauncher.class);
    }

    @GetMapping("hello")
    public R hello(String name) {
        return R.data("hello " + name + "!");
    }

}
