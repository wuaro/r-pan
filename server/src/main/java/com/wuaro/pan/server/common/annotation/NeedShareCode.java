package com.wuaro.pan.server.common.annotation;

import java.lang.annotation.*;

/**
 * 该注解主要影响需要分享码校验的接口
 * 标注该注解的方法会自动屏蔽统一的登录拦截校验逻辑
 */
/*
    注解：
        1. @Documented：
            指示该注解应该被 javadoc 工具记录。
        2. @Retention(RetentionPolicy.RUNTIME)：
            指定该注解在运行时可用，因此可以通过反射机制获取注解信息。
        3. @Target({ElementType.METHOD})：
            指定该注解可以应用在方法上（只支持标注到方法级别）
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface NeedShareCode {
}
