package com.wuaro.pan.cache.redis.config;


import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis Cache配置类：
 * 该缓存方案不支持事务，该缓存方案直接集成spring-boot-data-redis，所以舍弃自定义配置，直接使用Spring的配置
 *
 * 方法：
 *      redisTemplate()：定制连接和操作Redis的客户端工具
 *          返回值：RedisTemplate<String,Object>对象
 *              RedisTemplate可以理解为Spring对于redis客户端的一个抽象，通过RedisTemplate执行一些redis常用命令
*           参数：RedisConnectionFactory Redis连接工厂对象
 *          1. 创建一个Jackson2JsonRedisSerializer序列化器对象jackson2JsonRedisSerializer
 *          2. 创建一个StringRedisSerializer序列化器对象stringRedisSerializer
 *          3. 创建一个RedisTemplate对象redisTemplate
 *          4.  redisTemplate的连接工厂使用传进来的参数redisConnectionFactory
 *              redisTemplate的 key和哈希key 的序列化器使用stringRedisSerializer
 *              redisTemplate的 value和哈希value 的序列化器使用jackson2JsonRedisSerializer
 *
 *     ·
 *
 *
 *
 */

@SpringBootConfiguration
@EnableCaching
@Slf4j
public class RedisCacheConfig {
    /**
     * 定制连接和操作Redis的客户端工具
     * @param redisConnectionFactory
     * @return
     */
    @Bean
    @SuppressWarnings("all")
    public RedisTemplate<String,Object> redisTemplate(RedisConnectionFactory redisConnectionFactory){
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<Object>(Object.class);
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        RedisTemplate<String,Object> redisTemplate = new RedisTemplate<>();

        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(stringRedisSerializer);
        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.setHashKeySerializer(stringRedisSerializer);
        redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer);


        return redisTemplate;
    }

    /**
     * 定制化Redis的缓存管理器
     * @param redisConnectionFactory
     * @return
     */
    @Bean
    @SuppressWarnings("all")
    public CacheManager redisCacheManager(RedisConnectionFactory redisConnectionFactory){
        RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration
                .defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new Jackson2JsonRedisSerializer<Object>(Object.class)));

        RedisCacheManager cacheManager = RedisCacheManager
                .builder(RedisCacheWriter.lockingRedisCacheWriter(redisConnectionFactory))
                .cacheDefaults(redisCacheConfiguration)
                .transactionAware()
                .build();
        log.info("the redis cache manager is loaded successfully!");
        return cacheManager;
    }

}
