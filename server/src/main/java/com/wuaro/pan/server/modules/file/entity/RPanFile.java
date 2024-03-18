package com.wuaro.pan.server.modules.file.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 物理文件信息表
 * r_pan_file表的映射实体类
 * @TableName r_pan_file
 */
/*
    注解：
        1. @TableName(value ="r_pan_file")
            是 MyBatis-Plus 框架中用于指定实体类对应的数据库表名的注解。
            该注解通常用在实体类的类级别（即放置在类的开头），用来指定实体类对应的数据库表名，其中 value 属性用于指定表名。
            @TableName(value ="r_pan_file") 将实体类 PanFile 映射到数据库中名为 r_pan_file 的表格。
            注意：使用 @TableName 注解需要配合 MyBatis-Plus 框架来实现基于注解的 CRUD 操作。
        2. @Data
            自动提供get、set、toString等方法
 */
@TableName(value ="r_pan_file")
@Data
public class RPanFile implements Serializable {
    /**
     * 文件id
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
     * 文件名称
     */
    /*
        注解：
            1.  @TableField(value = "filename")
                和@TableId的区别在于，它不用于映射主键，而是映射普通字段
     */
    @TableField(value = "filename")
    private String filename;

    /**
     * 文件物理路径
     */
    @TableField(value = "real_path")
    private String realPath;

    /**
     * 文件实际大小
     */
    @TableField(value = "file_size")
    private String fileSize;

    /**
     * 文件大小展示字符
     */
    @TableField(value = "file_size_desc")
    private String fileSizeDesc;

    /**
     * 文件后缀
     */
    @TableField(value = "file_suffix")
    private String fileSuffix;

    /**
     * 文件预览的响应头Content-Type的值
     */
    @TableField(value = "file_preview_content_type")
    private String filePreviewContentType;

    /**
     * 文件唯一标识
     */
    @TableField(value = "identifier")
    private String identifier;

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

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}