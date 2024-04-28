package com.wuaro.pan.server.modules.file.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wuaro.pan.core.exception.RPanBusinessException;
import com.wuaro.pan.core.utils.FileUtils;
import com.wuaro.pan.core.utils.IdUtil;
import com.wuaro.pan.server.common.event.log.ErrorLogEvent;
import com.wuaro.pan.server.modules.file.context.FileSaveContext;
import com.wuaro.pan.server.modules.file.context.QueryRealFileListContext;
import com.wuaro.pan.server.modules.file.entity.RPanFile;
import com.wuaro.pan.server.modules.file.service.IFileService;
import com.wuaro.pan.server.modules.file.mapper.RPanFileMapper;
import com.wuaro.pan.storage.engine.core.StorageEngine;
import com.wuaro.pan.storage.engine.core.context.DeleteFileContext;
import com.wuaro.pan.storage.engine.core.context.StoreFileContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Objects;
import java.io.IOException;
import java.util.Date;

/**
 * @author 11391
 * @description 针对表【r_pan_file(物理文件信息表)】的数据库操作Service实现
 * @createDate 2024-03-06 08:59:39
 */
@Service
public class FileServiceImpl extends ServiceImpl<RPanFileMapper, RPanFile>
        implements IFileService ,ApplicationContextAware{


    @Autowired
    private StorageEngine storageEngine;

    private ApplicationContext applicationContext;
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }


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

    /**
     * 上传单文件并保存实体记录
     * 
     * 1、上传单文件
     * 2、保存实体记录
     *
     * @param context
     */
    @Override
    public void saveFile(FileSaveContext context) {
        storeMultipartFile(context);
        RPanFile record = doSaveFile(context.getFilename(),
                context.getRealPath(),
                context.getTotalSize(),
                context.getIdentifier(),
                context.getUserId());
        context.setRecord(record);
    }
    /************************************************private************************************************/



    /**
     * 保存实体文件记录
     *
     * @param filename
     * @param realPath
     * @param totalSize
     * @param identifier
     * @param userId
     * @return
     */
    private RPanFile doSaveFile(String filename, String realPath, Long totalSize, String identifier, Long userId) {
        RPanFile record = assembleRPanFile(filename, realPath, totalSize, identifier, userId);
        if (!save(record)) {
            try {
                DeleteFileContext deleteFileContext = new DeleteFileContext();
                deleteFileContext.setRealFilePathList(Lists.newArrayList(realPath));
                storageEngine.delete(deleteFileContext);
            } catch (IOException e) {
                e.printStackTrace();
                ErrorLogEvent errorLogEvent = new ErrorLogEvent(this,"文件物理删除失败，请执行手动删除！文件路径: " + realPath,userId);
                applicationContext.publishEvent(errorLogEvent);
            }
        }
        return record;
    }


    /**
     * 拼装文件实体对象
     *
     * @param filename
     * @param realPath
     * @param totalSize
     * @param identifier
     * @param userId
     * @return
     */
    private RPanFile assembleRPanFile(String filename, String realPath, Long totalSize, String identifier, Long userId) {
        RPanFile record = new RPanFile();

        record.setFileId(IdUtil.get());
        record.setFilename(filename);
        record.setRealPath(realPath);
        record.setFileSize(String.valueOf(totalSize));
        record.setFileSizeDesc(FileUtils.byteCountToDisplaySize(totalSize));
        record.setFileSuffix(FileUtils.getFileSuffix(filename));
        record.setFilePreviewContentType(FileUtils.getContentType(realPath));
        record.setIdentifier(identifier);
        record.setCreateUser(userId);
        record.setCreateTime(new Date());

        return record;
    }

    /**
     * 上传单文件
     * 该方法委托文件存储引擎实现
     *
     * @param context
     */
    private void storeMultipartFile(FileSaveContext context) {
        try {
            StoreFileContext storeFileContext = new StoreFileContext();
            storeFileContext.setInputStream(context.getFile().getInputStream());
            storeFileContext.setFilename(context.getFilename());
            storeFileContext.setTotalSize(context.getTotalSize());
            storageEngine.store(storeFileContext);
            context.setRealPath(storeFileContext.getRealPath());
        } catch (IOException e) {
            e.printStackTrace();
            throw new RPanBusinessException("文件上传失败");
        }
    }


}