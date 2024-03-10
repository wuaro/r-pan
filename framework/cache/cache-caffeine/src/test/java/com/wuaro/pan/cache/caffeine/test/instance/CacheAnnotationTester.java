package com.wuaro.pan.cache.caffeine.test.instance;

import com.wuaro.pan.cache.core.constants.CacheConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

/**
 * Cache注解测试实体
 */


@Component
@Slf4j
public class CacheAnnotationTester {

    /**
     * sync属性：如果设置为true，则出现多个线程同时缓存未命中，需要击穿缓存去数据库查询时，只会放一个线程进入数据库查询，其他线程需要阻塞
     *          如果设置为false，则出现多个线程同时缓存未命中，需要击穿缓存去数据库查询时，就不管不顾
     *          设置为true时能在某种情况下解决缓存穿透的问题（注意是某种情况下，并不能完全解决缓存穿透）！！！
     *          情况一：多个线程同时查询同一条数据，都击穿缓存，且都是库里存在的数据，此时sync设置为true是有用的
     *                  只放一个线程进入库中查询，查询成功后会将该条数据放入缓存中，后面其他线程再来查询就可以直接去缓存中查询，避免再次击穿缓存
*               情况二：和上述情况一样，不同的是这次都是库里不存在的数据（多数为恶意攻击，目的就是缓存穿透，让数据库挂掉）
     *                  此时sync设置为true也是没用的，因为数据根本查询不到，自然也不可能在查询后放入缓存中
     *                  所以后面的一大堆线程依然会击穿缓存，如果线程数量过多，就会让数据库挂掉
     *                  这种情况需要使用布隆过滤器
     *
     *
     */
    @Cacheable(cacheNames = CacheConstants.R_PAN_CACHE_NAME, key = "#name", sync = true)
    public String testCacheable(String name){
        log.info("call com.wuaro.pan.cache.caffeine.test.instance.CacheAnnotationTester.testCacheable,param is {}",name);
        return new StringBuilder("hello ").append(name).toString();
    }

}
