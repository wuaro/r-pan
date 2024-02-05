package com.wuaro.pan.swagger2;

import com.wuaro.pan.core.constants.RPanConstants;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * swagger2配置属性实体
 */

@Data
@Component
@ConfigurationProperties(prefix = "swagger2")
public class Swagger2ConfigProperties {

    //swagger文件是否展示
    private boolean show = true;
    //组名称
    private String groupName = "r-pan";
    //基础扫描路径
    private String basePackage = RPanConstants.BASE_COMPONENT_SCAN_PATH;
    //title
    private String title = "r-pan-server";
    //描述
    private String description = "r-pan-server";
    //url
    private String termsOfServiceUrl = "http://127.0.0.1:${server.port}";
    //联系人
    private String contactName = "wuaro";
    //联系地址
    private String contactUrl = "https://wuaro.github.io/";
    //联系人邮箱
    private String contactEmail = "1139188981@qq.com";
    //版本
    private String version = "1.0";

}
