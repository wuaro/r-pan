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
 *      1. redisTemplate(RedisConnectionFactory redisConnectionFactory)：
 *          作用：定制连接和操作Redis的客户端工具
 *          返回值：RedisTemplate<String,Object>对象
 *              RedisTemplate可以理解为Spring对于redis客户端的一个抽象，通过RedisTemplate执行一些redis常用命令
*           参数：RedisConnectionFactory Redis连接工厂对象
 *          内部细节：
 *              1. Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<Object>(Object.class);
 *                  作用：创建一个Jackson2JsonRedisSerializer序列化器对象jackson2JsonRedisSerializer
 *                  关于Jackson2JsonRedisSerializer：
 *                      是 Spring Data Redis 中用于序列化和反序列化对象的一个类。
 *                      它使用 Jackson 库将 Java 对象序列化为 JSON 格式，并将 JSON 数据存储在 Redis 中，
 *                      或者从 Redis 中读取 JSON 数据并反序列化为 Java 对象。
 *              2. StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
 *                  作用：创建一个StringRedisSerializer序列化器对象stringRedisSerializer
 *                  关于StringRedisSerializer：
 *                      是 Spring Data Redis 中用于将字符串对象序列化为字节数组的一个类。
 *                      它是 Redis 的默认序列化器之一，通常用于将字符串类型的数据存储到 Redis 中。
 *                      作用是将 Java 中的字符串对象转换为字节数组，然后存储到 Redis 中。
 *                      在从 Redis 中读取数据时，它会将字节数组反序列化为 Java 中的字符串对象。
 *              3. RedisTemplate<String,Object> redisTemplate = new RedisTemplate<>();
 *                  作用：创建一个RedisTemplate对象redisTemplate
 *                  关于RedisTemplate：
 *                      上面提到了，它提供了一组模板方法，使得在 Spring 应用中使用 Redis 更加便捷和灵活。
 *                      通过 RedisTemplate，你可以执行诸如存储、读取、删除数据等一系列操作，而无需编写冗长的 Redis 命令。
 *              4.  redisTemplate的连接工厂使用redisConnectionFactory（传进来的参数）
 *                  redisTemplate的 key和哈希key 的序列化器使用stringRedisSerializer
 *                  redisTemplate的 value和哈希value 的序列化器使用jackson2JsonRedisSerializer
 *
 *     ·2. redisCacheManager(RedisConnectionFactory redisConnectionFactory)
 *          作用：定制化Redis的缓存管理器
 *          参数：RedisConnectionFactory redisConnectionFactory
 *              关于：RedisConnectionFactory
 *                  是 Spring Data Redis 中用于创建 Redis 连接的接口。
 *                  它定义了一系列方法，用于获取与 Redis 服务器之间的连接，并提供了一些配置选项，以便根据需要自定义连接的创建方式。
 *          返回值：CacheManager对象
 *          内部细节：
 *              1. RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration
 *                 .defaultCacheConfig()
 *                 .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
 *                 .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new Jackson2JsonRedisSerializer<Object>(Object.class)));
 *                 配置 Redis 缓存的配置信息，并创建一个 RedisCacheConfiguration 对象
 *                 首先：获取默认的 Redis 缓存配置，包括缓存的过期时间、序列化器等
 *                 其次：配置键（Key）的序列化器。在这里，采用了 StringRedisSerializer，将键序列化为字符串类型。
 *                 最后：配置值（Value）的序列化器。在这里，采用了 Jackson2JsonRedisSerializer，将值序列化为 JSON 格式。
 *              2. RedisCacheManager cacheManager = RedisCacheManager
 *                 .builder(RedisCacheWriter.lockingRedisCacheWriter(redisConnectionFactory))
 *                 .cacheDefaults(redisCacheConfiguration)
 *                 .transactionAware()
 *                 .build();
 *                 创建一个 Redis 缓存管理器（RedisCacheManager），并配置了相应的参数。
 *                 这段代码用于创建一个 Redis 缓存管理器（`RedisCacheManager`），并配置了相应的参数。
 *                 首先：创建一个 Redis 缓存写入器（`RedisCacheWriter`），并配置了使用锁机制来处理并发写入操作。
 *                      它接受一个 `RedisConnectionFactory` 参数，用于创建连接到 Redis 的连接工厂。通过使用锁机制，可以保证在并发写入时的数据一致性。
 *                 其次：创建一个 Redis 缓存管理器的构建器，并传入了之前创建的 `RedisCacheWriter` 对象作为参数。
 *                      通过调用该方法，可以配置缓存管理器的一些参数。
 *                 再次：设置缓存管理器的默认缓存配置。在这里，传入了之前创建的 `RedisCacheConfiguration` 对象作为参数，
 *                      以便将其作为默认配置应用到所有的缓存中。这样，所有的缓存都会采用相同的键值序列化器和其他配置。
 *                 然后：设置缓存管理器是否支持事务管理。在这里，调用了 `transactionAware()` 方法，
 *                      表示启用事务管理。这样，当缓存操作和数据库操作一起发生在同一个事务中时，可以保证缓存和数据库的一致性。
 *                 最后：调用 `build()` 方法来构建并返回最终的 Redis 缓存管理器对象。
 *                 总之，这段代码的作用是创建一个 Redis 缓存管理器，并配置了缓存写入器、默认缓存配置、事务管理等参数，以便在 Spring 应用程序中使用 Redis 缓存。
 *              3. 打印日志，并返回 缓存管理器对象
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
