package com.wuaro.pan.server.common.config;

import com.wuaro.pan.core.constants.RPanConstants;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "com.wuaro.pan.server")
@Data
public class PanServerConfig {

    /**
     * 文件分片的过期天数(一天)
     */
    private Integer chunkFileExpirationDays = RPanConstants.ONE_INT;

    /**
     * 分享链接的前缀
     */
    private String sharePrefix = "http://127.0.0.1:8080/share/";

}
