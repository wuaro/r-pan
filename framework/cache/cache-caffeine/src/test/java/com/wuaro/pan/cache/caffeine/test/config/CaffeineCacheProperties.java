package com.wuaro.pan.cache.caffeine.test.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Caffeine Cache自定义配置属性类
 */

/**
 * 注解：
 *      注解@Data：自动提供getset方法（在CaffeineCacheConfig类中会使用get方法获取值）
 *      注解@Component：注入Spring容器
 *      注解@ConfigurationProperties(prefix = "com.wuaro.pan.cache.caffeine")：
 *
 */
@Data
@Component
@ConfigurationProperties(prefix = "com.wuaro.pan.cache.caffeine.test")
public class CaffeineCacheProperties {

    /**
     * 缓存初始容量
     * com.wuaro.pan.cache.caffeine.init-cache-capacity
     */
    private Integer initCacheCapacity = 256;

    /**
     * 缓存最大容量，超过之后会按照recently or very often（最近最少）策略进行缓存剔除
     * com.wuaro.pan.cache.caffeine.max-cache-capacity
     */
    private Long maxCacheCapacity = 10000L;

    /**
     * 是否允许空值null作为缓存的value
     * com.wuaro.pan.cache.caffeine.allow-null-value
     */
    private Boolean allowNullValue = Boolean.TRUE;

}
