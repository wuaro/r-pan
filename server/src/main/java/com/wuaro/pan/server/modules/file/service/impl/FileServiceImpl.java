package com.wuaro.pan.server.modules.file.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wuaro.pan.server.modules.file.context.QueryRealFileListContext;
import com.wuaro.pan.server.modules.file.entity.RPanFile;
import com.wuaro.pan.server.modules.file.service.IFileService;
import com.wuaro.pan.server.modules.file.mapper.RPanFileMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * @author 11391
 * @description 针对表【r_pan_file(物理文件信息表)】的数据库操作Service实现
 * @createDate 2024-03-06 08:59:39
 */
@Service
public class FileServiceImpl extends ServiceImpl<RPanFileMapper, RPanFile>
        implements IFileService {

    /**
     * 根据条件查询用户的实际文件列表
     *
     * @param context
     * @return
     */
    /*
    执行逻辑：
        这段代码是一个Java方法，用于根据给定的查询上下文（`QueryRealFileListContext`）获取文件列表（`getFileList`方法）：
        1. 首先，从给定的查询上下文中获取用户ID（`context.getUserId()`）和文件唯一标识符（`context.getIdentifier()`）。
        2. 创建一个Lambda查询包装器（`LambdaQueryWrapper<RPanFile>`）用于构建查询条件。
        3. 使用`queryWrapper.eq`方法添加查询条件：
           - 如果用户ID非空，则添加一个等于条件，匹配文件的创建用户为指定的用户ID（`RPanFile::getCreateUser, userId`）。
           - 如果标识符非空且不为空白，则添加一个等于条件，匹配文件的标识符为指定的标识符（`RPanFile::getIdentifier, identifier`）。
        4. 使用`list(queryWrapper)`方法执行查询，并返回符合条件的文件列表。
        这个方法的作用是根据用户ID和标识符查询文件列表，并返回符合条件的`RPanFile`对象列表。
     */
    @Override
    public List<RPanFile> getFileList(QueryRealFileListContext context) {
        Long userId = context.getUserId();
        String identifier = context.getIdentifier();
        LambdaQueryWrapper<RPanFile> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Objects.nonNull(userId), RPanFile::getCreateUser, userId);
        queryWrapper.eq(StringUtils.isNotBlank(identifier), RPanFile::getIdentifier, identifier);
        return list(queryWrapper);
    }
}