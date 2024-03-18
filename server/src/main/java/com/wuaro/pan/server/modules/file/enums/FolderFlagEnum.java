package com.wuaro.pan.server.modules.file.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 文件夹标识枚举类
 * code=1表示是文件夹，code=0表示非文件夹
 */
@AllArgsConstructor
@Getter
public enum FolderFlagEnum {

    /**
     * 非文件夹
     */
    NO(0),
    /**
     * 是文件夹
     */
    YES(1);

    private Integer code;

}
