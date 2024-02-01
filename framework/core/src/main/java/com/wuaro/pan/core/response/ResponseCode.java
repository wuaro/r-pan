package com.wuaro.pan.core.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ResponseCode {
    /**
     * 成功
     */
    SUCCESS(0, "SUCCESS"),
    /**
     * 错误
     */
    ERROR(1, "ERROR"),
    /**
     * token过期
     */
    TOKEN_EXPIRE(2, "TOKEN_EXPIRE"),
    /**
     * 参数错误
     */
    ERROR_PARAM(3, "ERROR_PARAM"),
    /**
     * 无权限访问
     */
    ACCESS_DENIED(4, "ACCESS_DENIED"),
    /**
     * 需要登录
     */
    NEED_LOGIN(10, "NEED_LOGIN");

    /**
     * 状态码
     */
    private Integer code;

    /**
     * 描述信息
     */
    private String desc;

}
