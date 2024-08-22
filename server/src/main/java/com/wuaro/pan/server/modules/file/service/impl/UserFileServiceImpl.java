package com.wuaro.pan.server.modules.file.service.impl;

import com.google.common.collect.Lists;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wuaro.pan.core.constants.RPanConstants;
import com.wuaro.pan.core.exception.RPanBusinessException;
import com.wuaro.pan.core.utils.FileUtils;
import com.wuaro.pan.core.utils.IdUtil;
import com.wuaro.pan.server.common.event.file.DeleteFileEvent;
import com.wuaro.pan.server.common.event.search.UserSearchEvent;
import com.wuaro.pan.server.common.utils.HttpUtil;
import com.wuaro.pan.server.modules.file.constants.FileConstants;
import com.wuaro.pan.server.modules.file.context.*;
import com.wuaro.pan.server.modules.file.converter.FileConverter;
import com.wuaro.pan.server.modules.file.entity.RPanFile;
import com.wuaro.pan.server.modules.file.entity.RPanUserFile;
import com.wuaro.pan.server.modules.file.enums.DelFlagEnum;
import com.wuaro.pan.server.modules.file.enums.FileTypeEnum;
import com.wuaro.pan.server.modules.file.enums.FolderFlagEnum;
import com.wuaro.pan.server.modules.file.service.IFileService;
import com.wuaro.pan.server.modules.file.service.IUserFileService;
import com.wuaro.pan.server.modules.file.mapper.RPanUserFileMapper;
import com.wuaro.pan.server.modules.file.vo.*;
import com.wuaro.pan.storage.engine.core.AbstractStorageEngine;
import com.wuaro.pan.storage.engine.core.context.ReadFileContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.apache.commons.collections.CollectionUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.stream.Collectors;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * @author 11391
 * @description 针对表【r_pan_user_file(用户文件信息表)】的数据库操作Service实现
 * @createDate 2024-03-06 08:59:39
 */
/*
    注解：
        1.  @Service(value = "userFileService")
            将 UserFileService 类标识为一个服务类，并指定在 Spring 容器中的名称为 "userFileService"，
            可以通过这个名称在其他地方引用这个服务类。
 */
@Service(value = "userFileService")
public class UserFileServiceImpl extends ServiceImpl<RPanUserFileMapper, RPanUserFile>
        implements IUserFileService , ApplicationContextAware {

    @Autowired
    private IFileService iFileService;

    private ApplicationContext applicationContext;

    @Autowired
    private FileConverter fileConverter;

    @Autowired
    private FileChunkServiceImpl iFileChunkService;

    @Autowired
    private AbstractStorageEngine storageEngine;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {

        this.applicationContext = applicationContext;
    }

    /**
     * 创建文件夹信息
     * 创建一个文件夹实体，并存进数据库表中
     *
     * @param createFolderContext
     * @return
     */
    @Override
    public Long createFolder(CreateFolderContext createFolderContext) {
        return saveUserFile(createFolderContext.getParentId(),
                createFolderContext.getFolderName(),
                FolderFlagEnum.YES,     //表示该文件是一个文件夹
                null,
                null,
                createFolderContext.getUserId(),
                null);
    }

    /**
     * 查询用户的根文件夹信息
     *
     * @param userId
     * @return
     */
    @Override
    public RPanUserFile getUserRootFile(Long userId) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("user_id", userId);
        queryWrapper.eq("parent_id", FileConstants.TOP_PARENT_ID);
        queryWrapper.eq("del_flag", DelFlagEnum.NO.getCode());
        queryWrapper.eq("folder_flag", FolderFlagEnum.YES.getCode());
        return getOne(queryWrapper);
    }

    /**
     * 查询用户的文件列表
     *
     * @param context
     * @return
     */
    @Override
    public List<RPanUserFileVO> getFileList(QueryFileListContext context) {
        return baseMapper.selectFileList(context);
    }

    /**
     * 更新文件名称
     * 1、校验更新文件名称的条件
     * 2、执行更新文件名称的操作
     *
     * @param context
     */
    @Override
    public void updateFilename(UpdateFilenameContext context) {
        checkUpdateFilenameCondition(context);
        doUpdateFilename(context);
    }

    /**
     * 批量删除用户文件
     *
     * 1、校验删除的条件
     * 2、执行批量删除的动作
     * 3、发布批量删除文件的事件，给其他模块订阅使用
     *
     * @param context
     */
    @Override
    public void deleteFile(DeleteFileContext context) {
        checkFileDeleteCondition(context);
        doDeleteFile(context);
        afterFileDelete(context);
    }

    /**
     * 文件秒传功能
     *
     * 1、判断用户之前是否上传过该文件
     * 2、如果上传过该文件，只需要生成一个该文件和当前用户在指定文件夹下面的关联关系即可
     *
     * @param context
     * @return true 代表用户之前上传过相同文件并成功挂在了关联关系 false 用户没有上传过该文件，请手动执行上传逻辑
     */
    /*
    执行逻辑：
        这段代码是一个Java方法，看起来是用于实现文件上传功能的。让我们逐步解析它的功能：
        1. 首先，通过调用`getFileListByUserIdAndIdentifier`方法获取用户ID和标识符对应的文件列表。
        2. 如果文件列表不为空（即`CollectionUtils.isNotEmpty(fileList)`返回true），
            则从文件列表中获取第一个文件（`fileList.get(RPanConstants.ZERO_INT)`）。
        3. 调用`saveUserFile`方法保存文件信息，包括父ID、文件名、文件夹标志、文件类型、文件ID、用户ID和文件大小描述。
        4. 最后，如果文件列表不为空，则返回true，表示上传成功；否则返回false，表示上传失败。
        需要注意的是，这段代码假设了`getFileListByUserIdAndIdentifier`、`saveUserFile`和其他相关方法已经在代码中定义并实现了。
        这些方法的具体实现会影响到整个文件上传流程的执行结果。
     */
    @Override
    public boolean secUpload(SecUploadFileContext context) {
        List<RPanFile> fileList = getFileListByUserIdAndIdentifier(context.getUserId(), context.getIdentifier());
        if (CollectionUtils.isNotEmpty(fileList)) {
            RPanFile record = fileList.get(RPanConstants.ZERO_INT);
            saveUserFile(context.getParentId(),
                    context.getFilename(),
                    FolderFlagEnum.NO,
                    FileTypeEnum.getFileTypeCode(FileUtils.getFileSuffix(context.getFilename())),
                    record.getFileId(),
                    context.getUserId(),
                    record.getFileSizeDesc());
            return true;
        }
        return false;
    }

    /**
     * 单文件上传
     *
     * 1、上传文件并保存实体文件的记录
     * 2、保存用户文件的关系记录
     *
     * @param context
     */
    /*
    注解：
        1. @Transactional(rollbackFor = Exception.class):
            这是Spring框架的事务管理注解，表示这个方法需要在一个事务中运行，并且如果发生异常时需要进行回滚操作。
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void upload(FileUploadContext context) {
        saveFile(context);
        saveUserFile(context.getParentId(),
                context.getFilename(),
                FolderFlagEnum.NO,
                FileTypeEnum.getFileTypeCode(FileUtils.getFileSuffix(context.getFilename())),
                context.getRecord().getFileId(),
                context.getUserId(),
                context.getRecord().getFileSizeDesc());
    }

    /**
     * 文件分片上传
     *
     * 1. 上传实体文件
     * 2. 保存分片文件记录
     * 3. 校验是否全部分片上传完成
     *
     * @param context
     * @return
     */
    @Override
    public FileChunkUploadVO chunkUpload(FileChunkUploadContext context) {
        FileChunkSaveContext fileChunkSaveContext = fileConverter.fileChunkUploadContext2FileChunkSaveContext(context);
        iFileChunkService.saveChunkFile(fileChunkSaveContext);
        FileChunkUploadVO vo = new FileChunkUploadVO();
        vo.setMergeFlag(fileChunkSaveContext.getMergeFlagEnum().getCode());
        return vo;
    }

    /**
     * 查询用户已上传的分片列表
     *
     * 1、查询已上传的分片列表
     * 2、封装返回实体
     *
     * @param context
     * @return
     */
    @Override
    public UploadedChunksVO getUploadedChunks(QueryUploadedChunksContext context) {
        QueryWrapper queryWrapper = Wrappers.query();
        queryWrapper.select("chunk_number");
        queryWrapper.eq("identifier", context.getIdentifier());
        queryWrapper.eq("create_user", context.getUserId());
        queryWrapper.gt("expiration_time", new Date());

        List<Integer> uploadedChunks = iFileChunkService.listObjs(queryWrapper, value -> (Integer) value);

        UploadedChunksVO vo = new UploadedChunksVO();
        vo.setUploadedChunks(uploadedChunks);
        return vo;
    }

    /**
     * 文件分片合并
     *
     * 1、文件分片物理合并
     * 2、保存文件实体记录
     * 3、保存文件用户关系映射
     *
     * @param context
     */
    @Override
    public void mergeFile(FileChunkMergeContext context) {
        mergeFileChunkAndSaveFile(context);
        saveUserFile(context.getParentId(),
                context.getFilename(),
                FolderFlagEnum.NO,
                FileTypeEnum.getFileTypeCode(FileUtils.getFileSuffix(context.getFilename())),
                context.getRecord().getFileId(),
                context.getUserId(),
                context.getRecord().getFileSizeDesc());
    }

    /**
     * 文件下载
     *
     * 1、参数校验：校验文件是否存在，文件是否属于该用户
     * 2、校验该文件是不是一个文件夹
     * 3、执行下载的动作
     *
     * @param context
     */
    @Override
    public void download(FileDownloadContext context) {
        RPanUserFile record = getById(context.getFileId());
        checkOperatePermission(record, context.getUserId());
        if (checkIsFolder(record)) {
            throw new RPanBusinessException("文件夹暂不支持下载");
        }
        doDownload(record, context.getResponse());
    }

    /**
     * 文件预览
     * 1、参数校验：校验文件是否存在，文件是否属于该用户
     * 2、校验该文件是不是一个文件夹
     * 3、执行预览的动作
     *
     * @param context
     */
    @Override
    public void preview(FilePreviewContext context) {
        RPanUserFile record = getById(context.getFileId());
        checkOperatePermission(record, context.getUserId());
        if (checkIsFolder(record)) {
            throw new RPanBusinessException("文件夹暂不支持下载");
        }
        doPreview(record, context.getResponse());
    }

    /**
     * 查询用户的文件夹树
     *
     * 1、查询出该用户的所有文件夹列表
     * 2、在内存中拼装文件夹树
     *
     * @param context
     * @return
     */
    @Override
    public List<FolderTreeNodeVO> getFolderTree(QueryFolderTreeContext context) {
        List<RPanUserFile> folderRecords = queryFolderRecords(context.getUserId());
        List<FolderTreeNodeVO> result = assembleFolderTreeNodeVOList(folderRecords);
        return result;
    }

    /**
     * 文件转移
     *
     * 1、权限校验
     * 2、执行工作
     *
     * @param context
     */
    @Override
    public void transfer(TransferFileContext context) {
        checkTransferCondition(context);
        doTransfer(context);
    }

    /**
     * 文件复制
     * <p>
     * 1、条件校验
     * 2、执行动作
     *
     * @param context
     */
    @Override
    public void copy(CopyFileContext context) {
        checkCopyCondition(context);
        doCopy(context);
    }


    /**
     * 文件列表搜索
     * <p>
     * 1、执行文件搜索
     * 2、拼装文件的父文件夹名称
     * 3、执行文件搜索后的后置动作
     *
     * @param context
     * @return
     */
    @Override
    public List<FileSearchResultVO> search(FileSearchContext context) {
        List<FileSearchResultVO> result = doSearch(context);
        fillParentFilename(result);
        afterSearch(context);
        return result;
    }

//    /**
//     * 获取面包屑列表
//     * <p>
//     * 1、获取用户所有文件夹信息
//     * 2、拼接需要用到的面包屑的列表
//     *
//     * @param context
//     * @return
//     */
//    @Override
//    public List<BreadcrumbVO> getBreadcrumbs(QueryBreadcrumbsContext context) {
//        List<RPanUserFile> folderRecords = queryFolderRecords(context.getUserId());
//        Map<Long, BreadcrumbVO> prepareBreadcrumbVOMap = folderRecords.stream().map(BreadcrumbVO::transfer).collect(Collectors.toMap(BreadcrumbVO::getId, a -> a));
//        BreadcrumbVO currentNode;
//        Long fileId = context.getFileId();
//        List<BreadcrumbVO> result = Lists.newLinkedList();
//        do {
//            currentNode = prepareBreadcrumbVOMap.get(fileId);
//            if (Objects.nonNull(currentNode)) {
//                result.add(0, currentNode);
//                fileId = currentNode.getParentId();
//            }
//        } while (Objects.nonNull(currentNode));
//        return result;
//    }

    /**
     * 递归查询所有的子文件信息
     *
     * @param records
     * @return
     */
    @Override
    public List<RPanUserFile> findAllFileRecords(List<RPanUserFile> records) {
        List<RPanUserFile> result = Lists.newArrayList(records);
        if (CollectionUtils.isEmpty(result)) {
            return result;
        }
        long folderCount = result.stream().filter(record -> Objects.equals(record.getFolderFlag(), FolderFlagEnum.YES.getCode())).count();
        if (folderCount == 0) {
            return result;
        }
        records.stream().forEach(record -> doFindAllChildRecords(result, record));
        return result;
    }

    /**
     * 递归查询所有的子文件信息
     *
     * @param fileIdList
     * @return
     */
    @Override
    public List<RPanUserFile> findAllFileRecordsByFileIdList(List<Long> fileIdList) {
        if (CollectionUtils.isEmpty(fileIdList)) {
            return Lists.newArrayList();
        }
        List<RPanUserFile> records = listByIds(fileIdList);
        if (CollectionUtils.isEmpty(records)) {
            return Lists.newArrayList();
        }
        return findAllFileRecords(records);
    }

    /**
     * 实体转换
     *
     * @param records
     * @return
     */
    @Override
    public List<RPanUserFileVO> transferVOList(List<RPanUserFile> records) {
        if (CollectionUtils.isEmpty(records)) {
            return Lists.newArrayList();
        }
        return records.stream().map(fileConverter::rPanUserFile2RPanUserFileVO).collect(Collectors.toList());
    }

    /************************************************private************************************************/


    /**
     * 递归查询所有的子文件列表
     * 忽略是否删除的标识
     *
     * @param result
     * @param record
     */
    private void doFindAllChildRecords(List<RPanUserFile> result, RPanUserFile record) {
        if (Objects.isNull(record)) {
            return;
        }
        if (!checkIsFolder(record)) {
            return;
        }
        List<RPanUserFile> childRecords = findChildRecordsIgnoreDelFlag(record.getFileId());
        if (CollectionUtils.isEmpty(childRecords)) {
            return;
        }
        result.addAll(childRecords);
        childRecords.stream()
                .filter(childRecord -> FolderFlagEnum.YES.getCode().equals(childRecord.getFolderFlag()))
                .forEach(childRecord -> doFindAllChildRecords(result, childRecord));
    }

    /**
     * 查询文件夹下面的文件记录，忽略删除标识
     *
     * @param fileId
     * @return
     */
    private List<RPanUserFile> findChildRecordsIgnoreDelFlag(Long fileId) {
        QueryWrapper queryWrapper = Wrappers.query();
        queryWrapper.eq("parent_id", fileId);
        List<RPanUserFile> childRecords = list(queryWrapper);
        return childRecords;
    }

    /**
     * 搜索的后置操作
     * <p>
     * 1、发布文件搜索的事件
     *
     * @param context
     */
    private void afterSearch(FileSearchContext context) {
        UserSearchEvent event = new UserSearchEvent(this,context.getKeyword(),context.getUserId());
        applicationContext.publishEvent(event);
    }
//    private void afterSearch(FileSearchContext context) {
//        UserSearchEvent event = new UserSearchEvent(context.getKeyword(), context.getUserId());
//        producer.sendMessage(PanChannels.USER_SEARCH_OUTPUT, event);
//    }

    /**
     * 填充文件列表的父文件名称
     *
     * @param result
     */
    private void fillParentFilename(List<FileSearchResultVO> result) {
        if (CollectionUtils.isEmpty(result)) {
            return;
        }
        List<Long> parentIdList = result.stream().map(FileSearchResultVO::getParentId).collect(Collectors.toList());
        List<RPanUserFile> parentRecords = listByIds(parentIdList);
        Map<Long, String> fileId2filenameMap = parentRecords.stream().collect(Collectors.toMap(RPanUserFile::getFileId, RPanUserFile::getFilename));
        result.stream().forEach(vo -> vo.setParentFilename(fileId2filenameMap.get(vo.getParentId())));
    }

    /**
     * 搜索文件列表
     *
     * @param context
     * @return
     */
    private List<FileSearchResultVO> doSearch(FileSearchContext context) {
        return baseMapper.searchFile(context);
    }

    /**
     * 执行文件复制的动作
     *
     * @param context
     */
    private void doCopy(CopyFileContext context) {
        List<RPanUserFile> prepareRecords = context.getPrepareRecords();
        if (CollectionUtils.isNotEmpty(prepareRecords)) {
            List<RPanUserFile> allRecords = Lists.newArrayList();
            prepareRecords.stream().forEach(record -> assembleCopyChildRecord(allRecords, record, context.getTargetParentId(), context.getUserId()));
            if (!saveBatch(allRecords)) {
                throw new RPanBusinessException("文件复制失败");
            }
        }
    }

    /**
     * 拼装当前文件记录以及所有的子文件记录
     *
     * @param allRecords
     * @param record
     * @param targetParentId
     * @param userId
     */
    private void assembleCopyChildRecord(List<RPanUserFile> allRecords, RPanUserFile record, Long targetParentId, Long userId) {
        Long newFileId = IdUtil.get();
        Long oldFileId = record.getFileId();

        record.setParentId(targetParentId);
        record.setFileId(newFileId);
        record.setUserId(userId);
        record.setCreateUser(userId);
        record.setCreateTime(new Date());
        record.setUpdateUser(userId);
        record.setUpdateTime(new Date());
        handleDuplicateFilename(record);

        allRecords.add(record);

        if (checkIsFolder(record)) {
            List<RPanUserFile> childRecords = findChildRecords(oldFileId);
            if (CollectionUtils.isEmpty(childRecords)) {
                return;
            }
            childRecords.stream().forEach(childRecord -> assembleCopyChildRecord(allRecords, childRecord, newFileId, userId));
        }

    }

    /**
     * 查找下一级的文件记录
     *
     * @param parentId
     * @return
     */
    private List<RPanUserFile> findChildRecords(Long parentId) {
        QueryWrapper queryWrapper = Wrappers.query();
        queryWrapper.eq("parent_id", parentId);
        queryWrapper.eq("del_flag", DelFlagEnum.NO.getCode());
        return list(queryWrapper);
    }

    /**
     * 文件复制的条件校验
     *
     * 1、目标文件必须是一个文件夹
     * 2、选中的要转移的文件列表中不能含有目标文件夹以及其子文件夹
     *
     * @param context
     */
    private void checkCopyCondition(CopyFileContext context) {
        Long targetParentId = context.getTargetParentId();
        if (!checkIsFolder(getById(targetParentId))) {
            throw new RPanBusinessException("目标文件不是一个文件夹");
        }
        List<Long> fileIdList = context.getFileIdList();
        List<RPanUserFile> prepareRecords = listByIds(fileIdList);
        context.setPrepareRecords(prepareRecords);
        if (checkIsChildFolder(prepareRecords, targetParentId, context.getUserId())) {
            throw new RPanBusinessException("目标文件夹ID不能是选中文件列表的文件夹ID或其子文件夹ID");
        }
    }

    /**
     * 执行文件转移的动作
     *
     * @param context
     */
    private void doTransfer(TransferFileContext context) {
        List<RPanUserFile> prepareRecords = context.getPrepareRecords();
        prepareRecords.stream().forEach(record -> {
            record.setParentId(context.getTargetParentId());
            record.setUserId(context.getUserId());
            record.setCreateUser(context.getUserId());
            record.setCreateTime(new Date());
            record.setUpdateUser(context.getUserId());
            record.setUpdateTime(new Date());
            handleDuplicateFilename(record);
        });
        if (!updateBatchById(prepareRecords)) {
            throw new RPanBusinessException("文件转移失败");
        }
    }

    /**
     * 文件转移的条件校验：
     * 1、目标文件必须是一个文件夹（毕竟不可能把一堆文件转移到一个文件中去吧，肯定是转移到某个文件夹中）
     * 2、选中的要转移的文件列表中不能含有目标文件夹以及其子文件夹
     *      比如A文件夹里有B文件夹，那么我们不能将A转移到B中（否则会循环嵌套）
     *
     * @param context
     */
    /*
    注意：
        List<RPanUserFile> prepareRecords = listByIds(fileIdList);
            根据待转移文件的ID列表查询数据库，获取待转移的文件记录列表。
     */
    private void checkTransferCondition(TransferFileContext context) {
        Long targetParentId = context.getTargetParentId();
        if (!checkIsFolder(getById(targetParentId))) {
            throw new RPanBusinessException("目标文件不是一个文件夹");
        }
        List<Long> fileIdList = context.getFileIdList();
        List<RPanUserFile> prepareRecords = listByIds(fileIdList);
        context.setPrepareRecords(prepareRecords);
        if (checkIsChildFolder(prepareRecords, targetParentId, context.getUserId())) {
            throw new RPanBusinessException("目标文件夹ID不能是选中文件列表的文件夹ID或其子文件夹ID");
        }
    }

    /**
     * 校验目标文件夹ID是都是要操作的文件记录的文件夹ID以及其子文件夹ID
     *
     * 1、如果要操作的文件列表中没有文件夹，那就直接返回false
     * 2、拼装文件夹ID以及所有子文件夹ID，判断存在即可
     *
     * @param prepareRecords
     * @param targetParentId
     * @param userId
     * @return
     */
    private boolean checkIsChildFolder(List<RPanUserFile> prepareRecords, Long targetParentId, Long userId) {
        prepareRecords = prepareRecords.stream().filter(record -> Objects.equals(record.getFolderFlag(), FolderFlagEnum.YES.getCode())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(prepareRecords)) {
            return false;
        }
        List<RPanUserFile> folderRecords = queryFolderRecords(userId);
        Map<Long, List<RPanUserFile>> folderRecordMap = folderRecords.stream().collect(Collectors.groupingBy(RPanUserFile::getParentId));
        List<RPanUserFile> unavailableFolderRecords = Lists.newArrayList();
        unavailableFolderRecords.addAll(prepareRecords);
        prepareRecords.stream().forEach(record -> findAllChildFolderRecords(unavailableFolderRecords, folderRecordMap, record));
        List<Long> unavailableFolderRecordIds = unavailableFolderRecords.stream().map(RPanUserFile::getFileId).collect(Collectors.toList());
        return unavailableFolderRecordIds.contains(targetParentId);
    }

    /**
     * 查找文件夹的所有子文件夹记录
     *
     * @param unavailableFolderRecords
     * @param folderRecordMap
     * @param record
     */
    private void findAllChildFolderRecords(List<RPanUserFile> unavailableFolderRecords, Map<Long, List<RPanUserFile>> folderRecordMap, RPanUserFile record) {
        if (Objects.isNull(record)) {
            return;
        }
        List<RPanUserFile> childFolderRecords = folderRecordMap.get(record.getFileId());
        if (CollectionUtils.isEmpty(childFolderRecords)) {
            return;
        }
        unavailableFolderRecords.addAll(childFolderRecords);
        childFolderRecords.stream().forEach(childRecord -> findAllChildFolderRecords(unavailableFolderRecords, folderRecordMap, childRecord));
    }

    /**
     * 拼装文件夹树列表
     *
     * @param folderRecords
     * @return
     */
    /*
    执行逻辑：
        if (CollectionUtils.isEmpty(folderRecords)) { return Lists.newArrayList(); }
            如果传入的文件夹记录列表是空的，则直接返回一个空的列表。
        List<FolderTreeNodeVO> mappedFolderTreeNodeVOList = folderRecords.stream().map(fileConverter::rPanUserFile2FolderTreeNodeVO).collect(Collectors.toList());
            将传入的文件夹记录列表转换为文件夹树节点（FolderTreeNodeVO）列表。这里使用了 map 方法将每个文件夹记录转换为对应的文件夹树节点。
        Map<Long, List<FolderTreeNodeVO>> mappedFolderTreeNodeVOMap = mappedFolderTreeNodeVOList.stream().collect(Collectors.groupingBy(FolderTreeNodeVO::getParentId));
            将文件夹树节点列表按照父节点ID进行分组，得到一个以父节点ID为键，对应子节点列表为值的映射。
        for (FolderTreeNodeVO node : mappedFolderTreeNodeVOList) {
            List<FolderTreeNodeVO> children = mappedFolderTreeNodeVOMap.get(node.getId());
            if (CollectionUtils.isNotEmpty(children)) {
                node.getChildren().addAll(children);
            }
        }
           遍历文件夹树节点列表，对于每个节点，从映射中获取其子节点列表，并将子节点添加到该节点的子节点列表中。
        return mappedFolderTreeNodeVOList.stream().filter(node -> Objects.equals(node.getParentId(), FileConstants.TOP_PARENT_ID)).collect(Collectors.toList());
            最后，将经过处理的文件夹树节点列表进行筛选，只保留顶层节点（即父节点ID为顶层父节点ID的节点），并返回这个顶层节点列表。
        总体来说，这个方法的作用是将文件夹记录列表转换为文件夹树节点列表，并组装成一个树形结构，其中每个节点包含其对应的子节点列表。最后返回的是顶层节点列表，即树的根节点列表。
     */
    private List<FolderTreeNodeVO> assembleFolderTreeNodeVOList(List<RPanUserFile> folderRecords) {
        if (CollectionUtils.isEmpty(folderRecords)) {
            return Lists.newArrayList();
        }
        List<FolderTreeNodeVO> mappedFolderTreeNodeVOList = folderRecords.stream().map(fileConverter::rPanUserFile2FolderTreeNodeVO).collect(Collectors.toList());
        Map<Long, List<FolderTreeNodeVO>> mappedFolderTreeNodeVOMap = mappedFolderTreeNodeVOList.stream().collect(Collectors.groupingBy(FolderTreeNodeVO::getParentId));
        for (FolderTreeNodeVO node : mappedFolderTreeNodeVOList) {
            List<FolderTreeNodeVO> children = mappedFolderTreeNodeVOMap.get(node.getId());
            if (CollectionUtils.isNotEmpty(children)) {
                node.getChildren().addAll(children);
            }
        }
        return mappedFolderTreeNodeVOList.stream().filter(node -> Objects.equals(node.getParentId(), FileConstants.TOP_PARENT_ID)).collect(Collectors.toList());
    }

    /**
     * 查询用户所有有效的文件夹信息
     *
     * @param userId
     * @return
     */
    private List<RPanUserFile> queryFolderRecords(Long userId) {
        QueryWrapper queryWrapper = Wrappers.query();
        queryWrapper.eq("user_id", userId);
        queryWrapper.eq("folder_flag", FolderFlagEnum.YES.getCode());
        queryWrapper.eq("del_flag", DelFlagEnum.NO.getCode());
        return list(queryWrapper);
    }

    /**
     * 执行文件预览的动作
     * 1、查询文件的真实存储路径
     * 2、添加跨域的公共响应头
     * 3、委托文件存储引擎去读取文件内容到响应的输出流中
     *
     * @param record
     * @param response
     */
    private void doPreview(RPanUserFile record, HttpServletResponse response) {
        RPanFile realFileRecord = iFileService.getById(record.getRealFileId());
        if (Objects.isNull(realFileRecord)) {
            throw new RPanBusinessException("当前的文件记录不存在");
        }
        addCommonResponseHeader(response, realFileRecord.getFilePreviewContentType());
        realFile2OutputStream(realFileRecord.getRealPath(), response);
    }

    /**
     * 执行文件下载的动作
     *
     * 1、查询文件的真实存储路径
     * 2、添加跨域的公共响应头
     *      跨域：两个不同域名之间的调用，会有一个跨域的校验
     *      为什么会出现跨域错误：
     *          浏览器不允许在当前域名下去调用另一个域名的数据请求
     *          除非我们服务器给它的响应头里，给它带上了一些允许跨域的一些特定的响应头
     *          这样的话浏览器才会绕过自身的一个跨域限制，做正常的数据交换
 *          这里为什么要添加跨域？
 *              因为H5下载动作都会模拟一个a标签，模拟一个触发去调用一个下载
 *              如果没有进行一些跨域的设置，浏览器会直接拦截的
     * 3、拼装下载文件的名称、长度等等响应信息
     * 4、委托文件存储引擎去读取文件内容到响应的输出流中
     *
     * @param record
     * @param response
     */
    private void doDownload(RPanUserFile record, HttpServletResponse response) {
        RPanFile realFileRecord = iFileService.getById(record.getRealFileId());
        if (Objects.isNull(realFileRecord)) {
            throw new RPanBusinessException("当前的文件记录不存在");
        }
        addCommonResponseHeader(response, MediaType.APPLICATION_OCTET_STREAM_VALUE);
        addDownloadAttribute(response, record, realFileRecord);
        realFile2OutputStream(realFileRecord.getRealPath(), response);
    }

    /**
     * 委托文件存储引擎去读取文件内容并写入到输出流中
     *
     * @param realPath
     * @param response
     */
    private void realFile2OutputStream(String realPath, HttpServletResponse response) {
        try {
            ReadFileContext context = new ReadFileContext();
            context.setRealPath(realPath);
            context.setOutputStream(response.getOutputStream());
            storageEngine.realFile(context);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RPanBusinessException("文件下载失败");
        }
    }

    /**
     * 添加文件下载的属性信息
     *
     * @param response
     * @param record
     * @param realFileRecord
     */
    /*
    注意：
        response.addHeader(FileConstants.CONTENT_DISPOSITION_STR,
                    FileConstants.CONTENT_DISPOSITION_VALUE_PREFIX_STR + new String(record.getFilename().getBytes(FileConstants.GB2312_STR),
                    FileConstants.IOS_8859_1_STR));
            这行代码添加了一个 HTTP 响应头，其中包含了文件下载属性。
            具体地说，它设置了 Content-Disposition 头，值为 "attachment; filename=..."，
            这个值告诉浏览器下载文件而不是直接打开，并指定了下载文件的文件名。这里使用了 GB2312 编码来处理文件名。
        response.setContentLengthLong(Long.valueOf(realFileRecord.getFileSize()));
            这行代码设置了 HTTP 响应的内容长度，即下载文件的大小。它通过 realFileRecord.getFileSize() 获取文件大小，并将其设置为响应的内容长度。
        综合起来，这个方法的作用是设置 HTTP 响应的下载属性，包括指定下载文件的文件名和文件大小，以便客户端下载文件时能够正确显示这些信息。
     */
    private void addDownloadAttribute(HttpServletResponse response, RPanUserFile record, RPanFile realFileRecord) {
        try {
            response.addHeader(FileConstants.CONTENT_DISPOSITION_STR,
                    FileConstants.CONTENT_DISPOSITION_VALUE_PREFIX_STR + new String(record.getFilename().getBytes(FileConstants.GB2312_STR), FileConstants.IOS_8859_1_STR));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new RPanBusinessException("文件下载失败");
        }
        response.setContentLengthLong(Long.valueOf(realFileRecord.getFileSize()));
    }

    /**
     *
     * 添加公共的文件读取响应头
     *
     * @param response
     * @param contentTypeValue
     */
    /*
    作用：
        这个 addCommonResponseHeader 方法用于向响应对象添加常见的 HTTP 头，特别是在处理文件下载或其他 HTTP 响应时使用。
    执行逻辑：
        response.reset();
            这行代码重置了响应。它会清除之前设置在响应中的任何数据。通常用于确保在添加新的头或内容之前处于干净的状态。
        HttpUtil.addCorsResponseHeaders(response);
            这行代码向响应添加了跨源资源共享（CORS）头。CORS 头用于控制来自不同来源（域）的资源的访问。
            此方法可能会根据您的 CORS 配置添加诸如 Access-Control-Allow-Origin、Access-Control-Allow-Methods 等头。
        response.addHeader(FileConstants.CONTENT_TYPE_STR, contentTypeValue);
            这行代码向响应添加了一个自定义头。
            FileConstants.CONTENT_TYPE_STR 是代表内容类型头的常量： "Content-Type"。
            contentTypeValue 是内容类型头的实际值，例如 "application/json"、"text/html"、"image/jpeg" 等。
        response.setContentType(contentTypeValue);
            这行代码设置了响应的内容类型。它类似于添加头，但此方法专门设置了 "Content-Type" 头。
            对于客户端来说，知道发送的内容类型（例如 JSON、HTML、图像）非常重要，这样它就可以正确处理响应。
        总体而言，这个方法确保响应中设置了必要的头，以便客户端正确处理，特别是在文件下载或 API 响应等场景中，内容类型和 CORS 可能对兼容性和安全性至关重要。
     */
    private void addCommonResponseHeader(HttpServletResponse response, String contentTypeValue) {
        response.reset();
        HttpUtil.addCorsResponseHeaders(response);
        response.addHeader(FileConstants.CONTENT_TYPE_STR, contentTypeValue);
        response.setContentType(contentTypeValue);
    }

    /**
     * 校验用户的操作权限
     *
     * 1、文件记录必须存在
     * 2、文件记录的创建者必须是该登录用户
     *
     * @param record
     * @param userId
     */
    private void checkOperatePermission(RPanUserFile record, Long userId) {
        if (Objects.isNull(record)) {
            throw new RPanBusinessException("当前文件记录不存在");
        }
        if (!record.getUserId().equals(userId)) {
            throw new RPanBusinessException("您没有该文件的操作权限");
        }
    }

    /**
     * 检查当前文件记录是不是一个文件夹
     *
     * @param record
     * @return
     */
    private boolean checkIsFolder(RPanUserFile record) {
        if (Objects.isNull(record)) {
            throw new RPanBusinessException("当前文件记录不存在");
        }
        return FolderFlagEnum.YES.getCode().equals(record.getFolderFlag());
    }

    /**
     *
     * 合并文件分片并保存物理文件记录
     *
     * @param context
     */
    private void mergeFileChunkAndSaveFile(FileChunkMergeContext context) {
        FileChunkMergeAndSaveContext fileChunkMergeAndSaveContext = fileConverter.fileChunkMergeContext2FileChunkMergeAndSaveContext(context);
        iFileService.mergeFileChunkAndSaveFile(fileChunkMergeAndSaveContext);
        context.setRecord(fileChunkMergeAndSaveContext.getRecord());
    }

    /**
     * 上传文件并保存实体文件记录
     * 委托给实体文件的Service去完成该操作
     *
     * @param context
     */
    private void saveFile(FileUploadContext context) {
        FileSaveContext fileSaveContext = fileConverter.fileUploadContext2FileSaveContext(context);
        iFileService.saveFile(fileSaveContext);
        context.setRecord(fileSaveContext.getRecord());
    }

    /**
     * 查询用户文件列表根据文件的唯一标识
     *
     * @param userId
     * @param identifier
     * @return
     */
    private List<RPanFile> getFileListByUserIdAndIdentifier(Long userId, String identifier) {
        QueryRealFileListContext context = new QueryRealFileListContext();
        context.setUserId(userId);
        context.setIdentifier(identifier);

        //之所以是getFileList是因为在秒传情况下我们不能保证在高并发情况下，文件的标识是唯一标识，所以可能查询出来多条数据，要用list来接收
        return iFileService.getFileList(context);
    }

    /**
     * 文件删除的后置操作
     *
     * 1、对外发布文件删除的事件
     *
     * @param context
     */
    /*
    执行逻辑：
        1.  DeleteFileEvent deleteFileEvent = new DeleteFileEvent(this, context.getFileIdList());
            创建一个 DeleteFileEvent 对象，该对象包含了文件删除事件的相关信息，例如事件源和文件 ID 列表。
            this 表示当前对象是事件源，context.getFileIdList() 获取文件 ID 列表。
        2.  applicationContext.publishEvent(deleteFileEvent);
            调用 applicationContext 对象的 publishEvent 方法，将创建的文件删除事件发布到应用程序上下文中，
            通知相关的监听器处理这个事件。
     */
    private void afterFileDelete(DeleteFileContext context) {
        DeleteFileEvent deleteFileEvent = new DeleteFileEvent(this,context.getFileIdList());
        applicationContext.publishEvent(deleteFileEvent);
    }

    /**
     * 执行文件删除的操作
     * 这里是逻辑删除，并不会在数据库中真的删除，而是会将del_flag设置为true
     *
     * @param context
     */
    /*
    执行逻辑：
        1.  List<Long> fileIdList = context.getFileIdList();
            UpdateWrapper updateWrapper = new UpdateWrapper();
            updateWrapper.in("file_id", fileIdList);
            从传入的 context 中获取要删除的文件 ID 列表。
            创建一个更新操作的包装器 updateWrapper。
            在包装器中设置条件，表示要删除的文件 ID 在 fileIdList 列表中。
        2.  updateWrapper.set("del_flag", DelFlagEnum.YES.getCode());
            updateWrapper.set("update_time", new Date());
            设置更新操作，将文件的 del_flag 字段设为已删除的状态。
            设置更新操作，将文件的 update_time 字段设为当前时间。
        3. 如果删除失败则报错
     */
    private void doDeleteFile(DeleteFileContext context) {
        List<Long> fileIdList = context.getFileIdList();

        UpdateWrapper updateWrapper = new UpdateWrapper();
        updateWrapper.in("file_id", fileIdList);
        updateWrapper.set("del_flag", DelFlagEnum.YES.getCode());
        updateWrapper.set("update_time", new Date());

        if (!update(updateWrapper)) {
            throw new RPanBusinessException("文件删除失败");
        }
    }

    /**
     * 删除文件之前的前置校验
     * <p>
     * 1、文件ID合法校验
     * 2、用户拥有删除该文件的权限
     *
     * @param context
     */
    /*
    执行逻辑：
        1. `List<Long> fileIdList = context.getFileIdList();`
            从传入的 `DeleteFileContext` 中获取文件 ID 列表。
        2. `List<RPanUserFile> rPanUserFiles = listByIds(fileIdList);`
            根据文件 ID 列表从数据库中查询对应的文件记录。
        3. `if (rPanUserFiles.size() != fileIdList.size()) { throw new RPanBusinessException("存在不合法的文件记录"); }`
            检查查询到的文件记录数量 是否与 传入的文件 ID 数量一致，如果不一致说明存在不合法的文件记录。
        4. `Set<Long> fileIdSet = rPanUserFiles.stream().map(RPanUserFile::getFileId).collect(Collectors.toSet());`
            将查询到的文件记录转换为文件 ID 的集合。
            也就是将 rPanUserFiles 列表中的每个 RPanUserFile 对象映射为其对应的文件 ID，并将这些文件 ID 放入一个 Set<Long> 集合中。
            逐行解释：
                rPanUserFiles.stream()
                    将 rPanUserFiles 列表转换为流，以便进行后续的操作。
                .map(RPanUserFile::getFileId)
                    使用 map 操作将 RPanUserFile 对象映射为其对应的文件 ID。
                    这里使用了 Java 8 的方法引用语法 RPanUserFile::getFileId，它等同于 lambda 表达式 (file) -> file.getFileId()。
                    说白了就是把一个RPanUserFile的list转化成一个getFileId的list，其中的getFileId对应原本list中每一个RPanUserFile的fileId
                .collect(Collectors.toSet())：
                    使用 collect 收集操作，将流中的元素收集到一个 Set<Long> 集合中。
                    这里使用了 Collectors.toSet()，表示收集为一个集合，集合中的元素不重复！！！
                    如果有重复的fileId，会被忽略，不进行存储，按理来讲，正常情况应该是每个文件id都是不同的
        5. `int oldSize = fileIdSet.size();
            fileIdSet.addAll(fileIdList);
            int newSize = fileIdSet.size();`
            将传入的文件 ID 列表加入到文件 ID 集合中，然后比较加入前后集合的大小，如果不一致说明存在重复的文件 ID。
        6. `if (oldSize != newSize) { throw new RPanBusinessException("存在不合法的文件记录"); }`
            如果加入后集合大小发生变化，说明存在重复的文件 ID，抛出异常。
        7. `Set<Long> userIdSet = rPanUserFiles.stream().map(RPanUserFile::getUserId).collect(Collectors.toSet());`
            将查询到的文件记录转换为用户 ID 的集合，和上面的fileIdSet是一个道理
            只不过这里的userIdSet里应该只有一个元素，因为批量删除的一定是同一个用户的文件（你总不可能去删除别人的文件吧！）
            所以所有文件的userID应该都是重复的才对，所以最终只能存进去一个userID，剩下重复的全部被忽略了
        8. `if (userIdSet.size() != 1) { throw new RPanBusinessException("存在不合法的文件记录"); }`
            检查用户 ID 集合的大小是否为1，如果不是1说明存在不合法的文件记录。
        9. `Long dbUserId = userIdSet.stream().findFirst().get();
            if (!Objects.equals(dbUserId, context.getUserId())) { throw new RPanBusinessException("当前登录用户没有删除该文件的权限"); }`
            从用户 ID 集合中获取用户 ID，并与传入的删除操作上下文中的用户 ID 进行比较，如果不一致说明当前登录用户没有删除该文件的权限，抛出异常。
     */
    private void checkFileDeleteCondition(DeleteFileContext context) {
        List<Long> fileIdList = context.getFileIdList();

        List<RPanUserFile> rPanUserFiles = listByIds(fileIdList);
        if (rPanUserFiles.size() != fileIdList.size()) {
            throw new RPanBusinessException("存在不合法的文件记录");
        }

        Set<Long> fileIdSet = rPanUserFiles.stream().map(RPanUserFile::getFileId).collect(Collectors.toSet());
        int oldSize = fileIdSet.size();
        fileIdSet.addAll(fileIdList);
        int newSize = fileIdSet.size();

        if (oldSize != newSize) {
            throw new RPanBusinessException("存在不合法的文件记录");
        }

        Set<Long> userIdSet = rPanUserFiles.stream().map(RPanUserFile::getUserId).collect(Collectors.toSet());
        if (userIdSet.size() != 1) {
            throw new RPanBusinessException("存在不合法的文件记录");
        }

        Long dbUserId = userIdSet.stream().findFirst().get();
        if (!Objects.equals(dbUserId, context.getUserId())) {
            throw new RPanBusinessException("当前登录用户没有删除该文件的权限");
        }
    }

    /**
     * 执行文件重命名的操作
     *
     * @param context
     */
    private void doUpdateFilename(UpdateFilenameContext context) {
        RPanUserFile entity = context.getEntity();
        entity.setFilename(context.getNewFilename());
        entity.setUpdateUser(context.getUserId());
        entity.setUpdateTime(new Date());

        if (!updateById(entity)) {
            throw new RPanBusinessException("文件重命名失败");
        }
    }

    /**
     * 更新文件名称的条件校验
     *
     * 1、文件ID是有效的
     * 2、用户有权限更新该文件的文件名称
     * 3、新旧文件名称不能一样
     * 4、不能使用当前文件夹下面的子文件的名称
     *
     * @param context
     */
    /*
    作用：
        这段代码用于检查更新文件名的条件，确保新的文件名符合要求并且没有重复。
    执行逻辑：
        1. 首先，通过传入的 `fileId` 获取数据库中对应的 `RPanUserFile` 实体对象 `entity`。
        2. 如果获取的 `entity` 为 null，说明传入的文件 ID 无效，抛出异常。
        3. 如果当前登录用户的 ID 不等于文件的拥有者 ID，说明当前用户没有修改该文件名称的权限，抛出异常。
        4. 如果新的文件名与当前文件名相同，说明没有修改，要求换一个新的文件名来修改，抛出异常。
        5. 使用查询条件 `QueryWrapper` 检查新的文件名在相同父级目录下是否已经存在，如果存在，则说明新文件名已被占用，抛出异常。
        6. 如果以上检查都通过，将获取到的 `entity` 设置到上下文对象 `context` 中，表示条件检查通过。
        总体来说，这个方法确保了在更新文件名时，文件 ID 有效、权限符合要求、新文件名未被占用，并将相关信息设置到上下文对象中供后续使用。
     */
    private void checkUpdateFilenameCondition(UpdateFilenameContext context) {

        Long fileId = context.getFileId();
        RPanUserFile entity = getById(fileId);

        if (Objects.isNull(entity)) {
            throw new RPanBusinessException("该文件ID无效");
        }

        if (!Objects.equals(entity.getUserId(), context.getUserId())) {
            throw new RPanBusinessException("当前登录用户没有修改该文件名称的权限");
        }

        if (Objects.equals(entity.getFilename(), context.getNewFilename())) {
            throw new RPanBusinessException("请换一个新的文件名称来修改");
        }

        //如果表中能找到parent_id和filename都相同的文件数据，说明重复了，不能更改
        QueryWrapper queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("parent_id", entity.getParentId());
        queryWrapper.eq("filename", context.getNewFilename());
        int count = count(queryWrapper);

        if (count > 0) {
            throw new RPanBusinessException("该文件名称已被占用");
        }

        context.setEntity(entity);
    }

    /**
     * 保存用户文件的映射记录（将数据存入数据库表中）
     * 保存失败则抛出异常，保存成功则返回id
     *
     * @param parentId
     * @param filename
     * @param folderFlagEnum
     * @param fileType       文件类型（1 普通文件 2 压缩文件 3 excel 4 word 5 pdf 6 txt 7 图片 8 音频 9 视频 10 ppt 11 源码文件 12 csv）
     * @param realFileId
     * @param userId
     * @param fileSizeDesc
     * @return
     */
    private Long saveUserFile(Long parentId,
                              String filename,
                              FolderFlagEnum folderFlagEnum,
                              Integer fileType,
                              Long realFileId,
                              Long userId,
                              String fileSizeDesc) {
        RPanUserFile entity = assembleRPanFUserFile(parentId, userId, filename, folderFlagEnum, fileType, realFileId, fileSizeDesc);
        if (!save((entity))) {
            throw new RPanBusinessException("保存文件信息失败");
        }
        return entity.getFileId();
    }

    /**
     * 用户文件映射关系实体转化
     * 1、构建并填充实体信息
     * 2、处理文件命名一致的问题
     *
     * 返回 RPanUserFile对象
     *
     * @param parentId
     * @param userId
     * @param filename
     * @param folderFlagEnum
     * @param fileType
     * @param realFileId
     * @param fileSizeDesc
     * @return
     */
    private RPanUserFile assembleRPanFUserFile(Long parentId, Long userId, String filename, FolderFlagEnum folderFlagEnum, Integer fileType, Long realFileId, String fileSizeDesc) {
        RPanUserFile entity = new RPanUserFile();

        entity.setFileId(IdUtil.get());
        entity.setUserId(userId);
        entity.setParentId(parentId);
        entity.setRealFileId(realFileId);
        entity.setFilename(filename);
        entity.setFolderFlag(folderFlagEnum.getCode());
        entity.setFileSizeDesc(fileSizeDesc);
        entity.setFileType(fileType);
        entity.setDelFlag(DelFlagEnum.NO.getCode());
        entity.setCreateUser(userId);
        entity.setCreateTime(new Date());
        entity.setUpdateUser(userId);
        entity.setUpdateTime(new Date());

        handleDuplicateFilename(entity);

        return entity;
    }

    /**
     * 处理用户重复名称
     * 如果同一文件夹下面有文件名称重复
     * 按照系统级规则重命名文件
     *
     * @param entity
     */
    /*
        参数：
            1. RPanUserFile类型 文件实体对象entity
        执行逻辑：
            1. String filename = entity.getFilename(),
                newFilenameWithoutSuffix,
                newFilenameSuffix;
                定义三个字符串filename、newFilenameWithoutSuffix、newFilenameSuffix
                分别存储 文件全名、去掉后缀的文件名、后缀
                entity.getFilename()
            2. int newFilenamePointPosition = filename.lastIndexOf(RPanConstants.POINT_STR);
                定义一个int型变量newFilenamePointPosition，
                用来存储filename.lastIndexOf(RPanConstants.POINT_STR)返回的文件名里最后一个"."的位置index值（没找到返回-1）
                其中RPanConstants.POINT_STR是以前在常量类中定义的常量字符串，值为"."
            3. if (newFilenamePointPosition == RPanConstants.MINUS_ONE_INT) {
                    newFilenameWithoutSuffix = filename;
                    newFilenameSuffix = StringUtils.EMPTY;
                } else {
                    newFilenameWithoutSuffix = filename.substring(RPanConstants.ZERO_INT, newFilenamePointPosition);
                    newFilenameSuffix = filename.replace(newFilenameWithoutSuffix, StringUtils.EMPTY);
                }
                如果文件名里最后一个"."的index值等于RPanConstants.MINUS_ONE_INT(-1)，说明没有"."
                则此时 文件全名 等于 去掉后缀的文件名，后缀 等于 空串
                否则 裁剪 文件全名中"."之前的部分作为 去掉后缀的文件名
                将文件全名中 去掉后缀的文件名 部分替换为空串，则剩下的部分就是 后缀
            4. List<RPanUserFile> existRecords = getDuplicateFilename(entity, newFilenameWithoutSuffix);
                if (CollectionUtils.isEmpty(existRecords)) {
                    return;
                }
                首先调用 getDuplicateFilename 方法获取具有相同文件名主体部分的文件记录列表。
                如果存在这样的记录（即 existRecords 不为空），则继续执行；否则直接返回。
            5. List<String> existFilenames = existRecords.stream().map(RPanUserFile::getFilename).collect(Collectors.toList());
                将 existRecords 转换为文件名字符串列表 existFilenames，这里使用了流操作和映射。
            6. int count = 1;
                String newFilename;
                do {
                    newFilename = assembleNewFilename(newFilenameWithoutSuffix, count, newFilenameSuffix);
                    count++;
                } while (existFilenames.contains(newFilename));
                entity.setFilename(newFilename);
                初始化一个计数器 count，然后进入一个循环。
                在循环中，使用 assembleNewFilename 方法根据当前计数器值、新文件名的主体部分和后缀部分构建新的文件名 newFilename。
                检查新文件名是否已经存在于 existFilenames 中，如果存在，则递增计数器并重复上述步骤，直到找到一个不存在于列表中的新文件名。
                最后，将找到的新文件名设置给实体对象的文件名属性 entity.setFilename(newFilename)。
                综上所述，这段代码的作用是处理重复的文件名，通过增加计数器的方式生成新的文件名，确保新文件名在列表中不存在，
                并将其设置给实体对象的文件名属性。
     */
    private void handleDuplicateFilename(RPanUserFile entity) {
        String filename = entity.getFilename(),
                newFilenameWithoutSuffix,
                newFilenameSuffix;
        int newFilenamePointPosition = filename.lastIndexOf(RPanConstants.POINT_STR);
        if (newFilenamePointPosition == RPanConstants.MINUS_ONE_INT) {
            newFilenameWithoutSuffix = filename;
            newFilenameSuffix = StringUtils.EMPTY;
        } else {
            newFilenameWithoutSuffix = filename.substring(RPanConstants.ZERO_INT, newFilenamePointPosition);
            newFilenameSuffix = filename.replace(newFilenameWithoutSuffix, StringUtils.EMPTY);
        }

        List<RPanUserFile> existRecords = getDuplicateFilename(entity, newFilenameWithoutSuffix);

        if (CollectionUtils.isEmpty(existRecords)) {
            return;
        }

        List<String> existFilenames = existRecords.stream().map(RPanUserFile::getFilename).collect(Collectors.toList());

        int count = 1;
        String newFilename;

        do {
            newFilename = assembleNewFilename(newFilenameWithoutSuffix, count, newFilenameSuffix);
            count++;
        } while (existFilenames.contains(newFilename));

        entity.setFilename(newFilename);
    }

    /**
     * 拼装新文件名称
     * 拼装规则参考操作系统重复文件名称的重命名规范
     * 通过字符串拼接的方式，将重名文件名后加上"（1）或（2）....."
     *
     * @param newFilenameWithoutSuffix
     * @param count
     * @param newFilenameSuffix
     * @return
     */
    private String assembleNewFilename(String newFilenameWithoutSuffix, int count, String newFilenameSuffix) {
        String newFilename = new StringBuilder(newFilenameWithoutSuffix)
                .append(FileConstants.CN_LEFT_PARENTHESES_STR)
                .append(count)
                .append(FileConstants.CN_RIGHT_PARENTHESES_STR)
                .append(newFilenameSuffix)
                .toString();
        return newFilename;
    }


    /**
     * 查找同一个父文件夹下面的同名文件数量
     * 作用：根据给定的实体对象和文件名主体部分，查询具有相同文件名主体的文件记录列表，并返回结果。
     *
     * @param entity
     * @param newFilenameWithoutSuffix
     * @return
     */
    /*
        参数：
            1. RPanUserFile entity
                用户文件信息表映射实体类RPanUserFile的实例entity
            2. String newFilenameWithoutSuffix
                一个字符串，表示一个 去掉后缀的文件名
        执行逻辑：
            1. QueryWrapper queryWrapper = new QueryWrapper();
                创建一个 QueryWrapper 对象，用于构建查询条件。
            2. queryWrapper.eq("parent_id", entity.getParentId());
                添加一个相等条件，要求文件记录的 `parent_id` 等于给定实体对象 `entity` 的 `parentId` 属性。
                下面两个也是同理...
            5. queryWrapper.eq("del_flag", DelFlagEnum.NO.getCode());
                添加一个相等条件，要求文件记录的 `del_flag` 等于枚举类型 `DelFlagEnum.NO` 的代码值。
            6. queryWrapper.likeRight("filename", newFilenameWithoutSuffix);
                添加一个右模糊查询条件，要求文件名 `filename` 以给定的 `newFilenameWithoutSuffix` 开头。
            7. return list(queryWrapper);
                使用 list(queryWrapper) 方法执行查询，返回满足上述条件的文件记录列表。
     */
    private List<RPanUserFile> getDuplicateFilename(RPanUserFile entity, String newFilenameWithoutSuffix) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("parent_id", entity.getParentId());
        queryWrapper.eq("folder_flag", entity.getFolderFlag());
        queryWrapper.eq("user_id", entity.getUserId());
        queryWrapper.eq("del_flag", DelFlagEnum.NO.getCode());
        queryWrapper.likeRight("filename", newFilenameWithoutSuffix);
        return list(queryWrapper);
    }

}