package com.wuaro.pan.server.modules.file.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 文件分片信息表
 * r_pan_file_chunk表的映射实体类
 * @TableName r_pan_file_chunk
 */
/*
    注解：
        1. @TableName(value ="r_pan_file_chunk")
            是 MyBatis-Plus 框架中用于指定实体类对应的数据库表名的注解。
            该注解通常用在实体类的类级别（即放置在类的开头），用来指定实体类对应的数据库表名，其中 value 属性用于指定表名。
            @TableName(value ="r_pan_file_chunk") 将实体类 RPanFileChunk 映射到数据库中名为 r_pan_file_chunk 的表格。
        2. @Data
            自动提供get、set、toString等方法
 */
@TableName(value ="r_pan_file_chunk")
@Data
public class RPanFileChunk implements Serializable {
    /**
     * 主键
     */
    /*
        注解：
            1. @TableId(value = "id", type = IdType.AUTO)
                是 MyBatis-Plus 框架中用于指定实体类的 主键 属性的注解。
                该注解通常用在实体类的属性上，用来指定该属性对应数据库表的主键字段，并可以通过 value 属性指定主键字段的名称。
                type = IdType.AUTO表示以自增的方式生成主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 文件唯一标识
     */
    @TableField(value = "identifier")
    private String identifier;

    /**
     * 分片真实的存储路径
     */
    /*
        注解：
            1.  @TableField(value = "real_path")
                和@TableId的区别在于，它不用于映射主键，而是映射普通字段
     */
    @TableField(value = "real_path")
    private String realPath;

    /**
     * 分片编号
     */
    @TableField(value = "chunk_number")
    private Integer chunkNumber;

    /**
     * 过期时间
     */
    @TableField(value = "expiration_time")
    private Date expirationTime;

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