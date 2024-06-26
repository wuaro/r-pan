package com.wuaro.pan.cache.caffeine.test.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.wuaro.pan.cache.caffeine.test.config.CaffeineCacheProperties;
import com.wuaro.pan.cache.core.constants.CacheConstants;
import com.wuaro.pan.core.constants.RPanConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

/**
 * CaffeineCache配置类
 */

/**
 *  注解：
 *      注解@ComponentScan(value = RPanConstants.BASE_COMPONENT_SCAN_PATH + ".cache.caffeine.test")
 *          扫描BASE_COMPONENT_SCAN_PATH基础扫描路径（"com.wuaro.pan"）下的".cache.caffeine.test"包下的关于测试的组件
 *          全部加载到Spring容器中
 *
 *
 *  成员变量：
 *      properties对象：
 *
 *  方法：
 *      caffeineCacheManager()方法：
 *          1. new一个CaffeineCacheManager对象（cacheManager），参数传入CacheConstants.R_PAN_CACHE_NAME（服务端公用缓存名称）
 *          2. cacheManager调用setAllowNullValues设置是否允许空值作为value
 *          3. 调用Caffeine.newBuilder()方法创建一个cache的构造器（caffeineBuilder）
 *              并初始化两个属性：
 *                  1. cache的初始化容量：
 *                  2. cache的最大容量
 *                  这两个属性在CaffeineCacheProperties类中是设置好的，只需要调用properties对象相应的get方法即可获取
 *           4. 调用cacheManager的setCaffeine()方法，传入caffeineBuilder对象，设置进去，以后创建Cache的时候会根据caffeineBuilder中的属性来创建
 *           以后cacheManager进行getCache的时候，会调用caffeineBuilder，去新建一个叫CaffeineCache的东西（是Caffeine对于Cache接口的一个实现）
 *
 *
 */
@SpringBootConfiguration
@EnableCaching
@ComponentScan(value = RPanConstants.BASE_COMPONENT_SCAN_PATH + ".cache.caffeine.test")
public class CaffeineCacheConfig {

    @Autowired(required=false)
    private CaffeineCacheProperties properties;

    @Bean
    public CacheManager caffeineCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(CacheConstants.R_PAN_CACHE_NAME);
        cacheManager.setAllowNullValues(properties.getAllowNullValue());
        Caffeine<Object, Object> caffeineBuilder = Caffeine.newBuilder()
                .initialCapacity(properties.getInitCacheCapacity())
                .maximumSize(properties.getMaxCacheCapacity());
        cacheManager.setCaffeine(caffeineBuilder);
        return cacheManager;
    }

}