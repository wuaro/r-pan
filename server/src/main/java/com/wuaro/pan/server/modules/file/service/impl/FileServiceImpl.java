package com.wuaro.pan.server.modules.file.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wuaro.pan.core.exception.RPanBusinessException;
import com.wuaro.pan.core.utils.FileUtils;
import com.wuaro.pan.core.utils.IdUtil;
import com.wuaro.pan.server.common.event.log.ErrorLogEvent;
import com.wuaro.pan.server.modules.file.context.FileChunkMergeAndSaveContext;
import com.wuaro.pan.server.modules.file.context.FileSaveContext;
import com.wuaro.pan.server.modules.file.context.QueryRealFileListContext;
import com.wuaro.pan.server.modules.file.entity.RPanFile;
import com.wuaro.pan.server.modules.file.entity.RPanFileChunk;
import com.wuaro.pan.server.modules.file.service.IFileChunkService;
import com.wuaro.pan.server.modules.file.service.IFileService;
import com.wuaro.pan.server.modules.file.mapper.RPanFileMapper;
import com.wuaro.pan.storage.engine.core.StorageEngine;
import com.wuaro.pan.storage.engine.core.context.DeleteFileContext;
import com.wuaro.pan.storage.engine.core.context.MergeFileContext;
import com.wuaro.pan.storage.engine.core.context.StoreFileContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;
import com.google.common.collect.Lists;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.io.IOException;
import java.util.Date;
import java.util.stream.Collectors;

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

    @Autowired
    private IFileChunkService iFileChunkService;

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

    /**
     * 合并物理文件并保存物理文件记录
     *
     * 1、委托文件存储引擎合并文件分片
     * 2、保存物理文件记录
     *
     * @param context
     */
    @Override
    public void mergeFileChunkAndSaveFile(FileChunkMergeAndSaveContext context) {
        doMergeFileChunk(context);
        RPanFile record = doSaveFile(context.getFilename(), context.getRealPath(), context.getTotalSize(), context.getIdentifier(), context.getUserId());
        context.setRecord(record);
    }
    /************************************************private************************************************/

    /**
     * 委托文件存储引擎合并文件分片
     *
     * 1、查询文件分片的记录
     * 2、根据文件分片的记录去合并物理文件
     * 3、删除文件分片记录
     * 4、封装合并文件的真实存储路径到上下文信息中
     *
     * @param context
     */
    /*
    代码解释：
        1. List<String> realPathList = chunkRecoredList.stream()
                .sorted(Comparator.comparing(RPanFileChunk::getChunkNumber))
                .map(RPanFileChunk::getRealPath)
                .collect(Collectors.toList());
       解释：
            .stream(): 将 chunkRecoredList 转换为流，以便进行流式操作。
            .sorted(Comparator.comparing(RPanFileChunk::getChunkNumber)):
                使用 Comparator.comparing 方法基于 chunkNumber 属性创建一个比较器，并将流中的元素按照 chunkNumber 属性进行排序。
            .map(RPanFileChunk::getRealPath):
                使用 map 方法将流中的每个 RPanFileChunk 对象映射为其对应的 realPath 属性，从而得到一个包含所有 realPath 字符串的流。
            .collect(Collectors.toList()):
                使用 collect 方法将流中的元素收集到一个列表中，最终得到一个 List<String> 类型的 realPathList 列表，
                其中包含了排序后的 RPanFileChunk 对象的 realPath 属性字符串。
            总之，这行代码的作用是对文件分片记录列表进行排序，并提取每个记录的实际路径，将这些路径字符串存储在 realPathList 列表中。
        2. 关于RPanFileChunk::getChunkNumber的补充解释：
            RPanFileChunk::getChunkNumber 是一个方法引用，它指向 RPanFileChunk 类中的 getChunkNumber 方法。
            在 Java 中，方法引用可以用来直接引用已有方法或构造方法，而不需要像 lambda 表达式那样提供方法体。方法引用通常用于函数式接口的实现，可以简化代码并提高可读性。
            具体到这个方法引用 RPanFileChunk::getChunkNumber，它的作用是引用 RPanFileChunk 类中的 getChunkNumber 方法，而不是调用该方法。在上下文中，
            这个方法引用被用作比较器，用来对 RPanFileChunk 对象进行排序。
            例如，如果有一个 List<RPanFileChunk> 类型的列表 chunkRecoredList，你可以使用方法引用来排序这个列表，如下所示：
                List<RPanFileChunk> sortedList = chunkRecoredList.stream()
                    .sorted(Comparator.comparing(RPanFileChunk::getChunkNumber))
                    .collect(Collectors.toList());
                在这段代码中，Comparator.comparing(RPanFileChunk::getChunkNumber) 就是一个比较器，
                它告诉 sorted 方法按照 RPanFileChunk 对象的 chunkNumber 属性进行排序。
     */
    private void doMergeFileChunk(FileChunkMergeAndSaveContext context) {
        QueryWrapper<RPanFileChunk> queryWrapper = Wrappers.query();
        queryWrapper.eq("identifier", context.getIdentifier());
        queryWrapper.eq("create_user", context.getUserId());
        queryWrapper.ge("expiration_time", new Date());
        List<RPanFileChunk> chunkRecoredList = iFileChunkService.list(queryWrapper);
        if (CollectionUtils.isEmpty(chunkRecoredList)) {
            throw new RPanBusinessException("该文件未找到分片记录");
        }
        List<String> realPathList = chunkRecoredList.stream()
                .sorted(Comparator.comparing(RPanFileChunk::getChunkNumber))
                .map(RPanFileChunk::getRealPath)
                .collect(Collectors.toList());

        try {
            MergeFileContext mergeFileContext = new MergeFileContext();
            mergeFileContext.setFilename(context.getFilename());
            mergeFileContext.setIdentifier(context.getIdentifier());
            mergeFileContext.setUserId(context.getUserId());
            mergeFileContext.setRealPathList(realPathList);
            storageEngine.mergeFile(mergeFileContext);
            context.setRealPath(mergeFileContext.getRealPath());
        } catch (IOException e) {
            e.printStackTrace();
            throw new RPanBusinessException("文件分片合并失败");
        }

        List<Long> fileChunkRecordIdList = chunkRecoredList.stream().map(RPanFileChunk::getId).collect(Collectors.toList());
        iFileChunkService.removeByIds(fileChunkRecordIdList);
    }

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
    /*
    执行逻辑：
        1. 组装文件记录对象
        2. 尝试保存文件记录到数据库
            如果保存失败则尝试删除物理文件：
                创建删除文件上下文实体，设置要删除的文件的路径集合，删除
            如果删除失败则抛出异常，并发布错误日志事件
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
    /*
    注意：
        1. 关于storeFileContext.setInputStream(context.getFile().getInputStream())：
            storeFileContext中的File属性类型是MultipartFile
            是org.springframework.web.multipart.MultipartFile;
            存储的是文件实体，其中MultipartFile对象的getInputStream()方法可以获取文件内容的输入流
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