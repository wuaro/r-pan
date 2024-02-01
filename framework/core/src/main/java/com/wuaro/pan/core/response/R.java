package com.wuaro.pan.core.response;
import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;

/**
 * 公用返回对象
 */
// 保证json序列化的时候，如果value为null的时候，key也就会一起消失
@JsonInclude(JsonInclude.Include.NON_NULL)
public class R<T> implements Serializable {

    /**
     * 状态码
     *
     * @see ResponseCode
     */
    private int code;
    private String message;
    private T data;

    private R(int code) {
        this.code = code;
    }

    private R(int code, String message) {
        this.code = code;
        this.message = message;
    }

    private R(int code, T data) {
        this.code = code;
        this.data = data;
    }

    private R(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // 使之在json序列化的结果当中
    @JsonIgnore
    @JSONField(serialize = false)
    public boolean isSuccess() {
        return code == ResponseCode.SUCCESS.getCode();
    }

    public static <T> R<T> success() {
        return new R<T>(ResponseCode.SUCCESS.getCode());
    }

    public static <T> R<T> success(String message) {
        return new R<T>(ResponseCode.SUCCESS.getCode(), message);
    }

    public static <T> R<T> data(T data) {
        return new R<T>(ResponseCode.SUCCESS.getCode(), data);
    }

    public static <T> R<T> success(String message, T data) {
        return new R<T>(ResponseCode.SUCCESS.getCode(), message, data);
    }

    public static <T> R<T> fail() {
        return new R<T>(ResponseCode.ERROR.getCode(), ResponseCode.ERROR.getDesc());
    }

    public static <T> R<T> fail(String error_message) {
        return new R<T>(ResponseCode.ERROR.getCode(), error_message);
    }

    public static <T> R<T> fail(int error_code, String error_message) {
        return new R<T>(error_code, error_message);
    }

    public static <T> R<T> fail(ResponseCode responseCode) {
        return new R<T>(responseCode.getCode(), responseCode.getDesc());
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

}