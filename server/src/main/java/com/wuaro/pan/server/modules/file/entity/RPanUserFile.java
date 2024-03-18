package com.wuaro.pan.server.modules.file.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户文件信息表
 * r_pan_user_file表的映射实体类
 * @TableName r_pan_user_file
 */

/*
    注解：
        1. @TableName(value ="r_pan_user_file")
            是 MyBatis-Plus 框架中用于指定实体类对应的数据库表名的注解。
            该注解通常用在实体类的类级别（即放置在类的开头），用来指定实体类对应的数据库表名，其中 value 属性用于指定表名。
            @TableName(value ="r_pan_user_file") 将实体类 RPanUserFile 映射到数据库中名为 r_pan_user_file 的表格。
        2. @Data
            自动提供get、set、toString等方法
 */
@TableName(value ="r_pan_user_file")
@Data
public class RPanUserFile implements Serializable {
    /**
     * 文件记录ID
     */
    /*
        注解：
            1. @TableId(value = "file_id")
                是 MyBatis-Plus 框架中用于指定实体类的 主键 属性的注解。
                该注解通常用在实体类的属性上，用来指定该属性对应数据库表的主键字段，并可以通过 value 属性指定主键字段的名称。
     */
    @TableId(value = "file_id")
    private Long fileId;

    /**
     * 用户ID
     */
    /*
        注解：
            1.  @TableField(value = "user_id")
                和@TableId的区别在于，它不用于映射主键，而是映射普通字段
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 上级文件夹ID,顶级文件夹为0
     */
    @TableField(value = "parent_id")
    private Long parentId;

    /**
     * 真实文件id
     */
    @TableField(value = "real_file_id")
    private Long realFileId;

    /**
     * 文件名
     */
    @TableField(value = "filename")
    private String filename;

    /**
     * 是否是文件夹 （0 否 1 是）
     */
    @TableField(value = "folder_flag")
    private Integer folderFlag;

    /**
     * 文件大小展示字符
     */
    @TableField(value = "file_size_desc")
    private String fileSizeDesc;

    /**
     * 文件类型（1 普通文件 2 压缩文件 3 excel 4 word 5 pdf 6 txt 7 图片 8 音频 9 视频 10 ppt 11 源码文件 12 csv）
     */
    @TableField(value = "file_type")
    private Integer fileType;

    /**
     * 删除标识（0 否 1 是）
     */
    @TableField(value = "del_flag")
    private Integer delFlag;

    /**
     * 创建人
     */
    @TableField(value = "create_user")
    private Long createUser;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    /**
     * 更新人
     */
    @TableField(value = "update_user")
    private Long updateUser;

    /**
     * 更新时间
     */
    @TableField(value = "update_time")
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}