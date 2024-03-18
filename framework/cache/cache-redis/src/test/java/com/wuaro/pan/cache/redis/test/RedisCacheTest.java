package com.wuaro.pan.cache.redis.test;

import cn.hutool.core.lang.Assert;
import com.wuaro.pan.cache.core.constants.CacheConstants;
import com.wuaro.pan.cache.redis.test.instance.CacheAnnotationTester;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * 测试类
 *
 * 注解：
 *
 * 成员变量：
 *
 * 方法：
 *      1. caffeineCacheManagerTest()
 *          用于测试 Caffeine 缓存管理器
 *          1. Cache cache = cacheManager.getCache(CacheConstants.R_PAN_CACHE_NAME);
 *              这行代码从缓存管理器中获取一个特定名称的缓存。
 *              在这里使用了事先在接口中定义好的常量（CacheConstants.R_PAN_CACHE_NAME）来指定缓存的名称。
*           2. Assert.notNull(cache);
 *              使用断言确保从缓存管理器中获取到了缓存对象。如果缓存对象为 null，表示未能成功获取到缓存，会触发断言失败。
 *          3. cache.put("name", "value");
 *              这行代码向缓存中放入一个键值对，键为 `"name"`，值为 `"value"`。
 *          4. String value = cache.get("name", String.class);
 *              这行代码从缓存中获取指定键的值。在这里，它尝试获取键为 `"name"` 的值，并将其转换为 `String` 类型。
 *          5. Assert.isTrue("value".equals(value));
 *              这行代码使用断言验证从缓存中获取的值是否与预期的值相等。如果相等，断言成功；否则，断言失败。
 *          总之，这个方法的作用是测试 Caffeine 缓存管理器是否正常工作。
 *          它首先获取一个特定名称的缓存对象，然后向缓存中存入一个键值对，并尝试从缓存中获取该键对应的值，最后验证获取的值是否正确。
 *      2. caffeineCacheAnnotationTest()
 *
 *
 *
 *
 */

@SpringBootTest(classes = RedisCacheTest.class)
@SpringBootApplication
@RunWith(SpringJUnit4ClassRunner.class)
public class RedisCacheTest {

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private CacheAnnotationTester cacheAnnotationTester;

    /**
     * 简单测试CacheManger的功能以及获取的Cache对象的功能
     */
    @Test
    public void caffeineCacheManagerTest() {
        Cache cache = cacheManager.getCache(CacheConstants.R_PAN_CACHE_NAME);
        Assert.notNull(cache);
        cache.put("name", "value");
        String value = cache.get("name", String.class);
        Assert.isTrue("value".equals(value));
    }

    @Test
    public void caffeineCacheAnnotationTest() {
        for (int i = 0; i < 2; i++) {
            cacheAnnotationTester.testCacheable("wuaro");
        }
    }

}
