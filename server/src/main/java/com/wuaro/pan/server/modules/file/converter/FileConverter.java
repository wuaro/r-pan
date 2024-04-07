package com.wuaro.pan.server.modules.file.converter;

import com.wuaro.pan.server.modules.file.context.*;
import com.wuaro.pan.server.modules.file.entity.RPanUserFile;
import com.wuaro.pan.server.modules.file.po.*;
import com.wuaro.pan.server.modules.file.vo.FolderTreeNodeVO;
import com.wuaro.pan.server.modules.file.vo.RPanUserFileVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * 文件模块实体转化工具类
 */
@Mapper(componentModel = "spring")
public interface FileConverter {


    /**
     * CreateFolderPO转CreateFolderContext
     *
     * @param createFolderPO
     * @return
     */
    /*
    注解：
        1. @Mapping(target = "parentId", expression = "java(com.wuaro.pan.core.utils.IdUtil.decrypt(createFolderPO.getParentId()))")
            这表示将 CreateFolderPO 对象中的 parentId 属性映射到 CreateFolderContext 对象中的同名属性，
            并使用 IdUtil.decrypt 方法对 parentId 进行解密操作。
            说白了就是：将CreateFolderPO的parentId变量，解密后 映射给 CreateFolderContext中的parentId
        2. @Mapping(target = "userId", expression = "java(com.wuaro.pan.server.common.utils.UserIdUtil.get())")
            这表示将调用 UserIdUtil.get() 方法获得的用户ID 映射为 CreateFolderContext 对象中的 userId 属性。
        这些映射关系可以在对象转换时方便地进行属性赋值，使得代码更加简洁和可读。
     */
    @Mapping(target = "parentId", expression = "java(com.wuaro.pan.core.utils.IdUtil.decrypt(createFolderPO.getParentId()))")
    @Mapping(target = "userId", expression = "java(com.wuaro.pan.server.common.utils.UserIdUtil.get())")
    CreateFolderContext createFolderPO2CreateFolderContext(CreateFolderPO createFolderPO);

    /**
     * UpdateFilenamePO转UpdateFilenameContext
     *
     * @param updateFilenamePO
     * @return
     */
    /*
    注解：
        1. @Mapping(target = "fileId", expression = "java(com.wuaro.pan.core.utils.IdUtil.decrypt(updateFilenamePO.getFileId()))")
            这表示将 updateFilenamePO 对象中的 fileId 属性映射到 UpdateFilenameContext 对象中的同名属性，
            并使用 IdUtil.decrypt 方法对 userId 进行解密操作。
            说白了就是：UpdateFilenamePO 的 userId 变量，解密后 映射给 UpdateFilenameContext 中的 userId
        2. @Mapping(target = "userId", expression = "java(com.wuaro.pan.server.common.utils.UserIdUtil.get())")
            这表示将调用 UserIdUtil.get() 方法获得的用户ID 映射为 UpdateFilenameContext 对象中的 userId 属性。
        这些映射关系可以在对象转换时方便地进行属性赋值，使得代码更加简洁和可读。
     */
    @Mapping(target = "fileId", expression = "java(com.wuaro.pan.core.utils.IdUtil.decrypt(updateFilenamePO.getFileId()))")
    @Mapping(target = "userId", expression = "java(com.wuaro.pan.server.common.utils.UserIdUtil.get())")
    UpdateFilenameContext updateFilenamePO2UpdateFilenameContext(UpdateFilenamePO updateFilenamePO);

    @Mapping(target = "userId", expression = "java(com.wuaro.pan.server.common.utils.UserIdUtil.get())")
    DeleteFileContext deleteFilePO2DeleteFileContext(DeleteFilePO deleteFilePO);

//    @Mapping(target = "parentId", expression = "java(com.wuaro.pan.core.utils.IdUtil.decrypt(secUploadFilePO.getParentId()))")
//    @Mapping(target = "userId", expression = "java(com.wuaro.pan.server.common.utils.UserIdUtil.get())")
//    SecUploadFileContext secUploadFilePO2SecUploadFileContext(SecUploadFilePO secUploadFilePO);
//
//    @Mapping(target = "parentId", expression = "java(com.wuaro.pan.core.utils.IdUtil.decrypt(fileUploadPO.getParentId()))")
//    @Mapping(target = "userId", expression = "java(com.wuaro.pan.server.common.utils.UserIdUtil.get())")
//    FileUploadContext fileUploadPO2FileUploadContext(FileUploadPO fileUploadPO);
//
//    @Mapping(target = "record", ignore = true)
//    FileSaveContext fileUploadContext2FileSaveContext(FileUploadContext context);
//
//    @Mapping(target = "userId", expression = "java(com.wuaro.pan.server.common.utils.UserIdUtil.get())")
//    FileChunkUploadContext fileChunkUploadPO2FileChunkUploadContext(FileChunkUploadPO fileChunkUploadPO);
//
//    FileChunkSaveContext fileChunkUploadContext2FileChunkSaveContext(FileChunkUploadContext context);
//
//    @Mapping(target = "realPath", ignore = true)
//    StoreFileChunkContext fileChunkSaveContext2StoreFileChunkContext(FileChunkSaveContext context);
//
//    @Mapping(target = "userId", expression = "java(com.wuaro.pan.server.common.utils.UserIdUtil.get())")
//    QueryUploadedChunksContext queryUploadedChunksPO2QueryUploadedChunksContext(QueryUploadedChunksPO queryUploadedChunksPO);
//
//    @Mapping(target = "userId", expression = "java(com.wuaro.pan.server.common.utils.UserIdUtil.get())")
//    @Mapping(target = "parentId", expression = "java(com.wuaro.pan.core.utils.IdUtil.decrypt(fileChunkMergePO.getParentId()))")
//    FileChunkMergeContext fileChunkMergePO2FileChunkMergeContext(FileChunkMergePO fileChunkMergePO);
//
//    FileChunkMergeAndSaveContext fileChunkMergeContext2FileChunkMergeAndSaveContext(FileChunkMergeContext context);

    @Mapping(target = "label", source = "record.filename")
    @Mapping(target = "id", source = "record.fileId")
    @Mapping(target = "children", expression = "java(com.google.common.collect.Lists.newArrayList())")
    FolderTreeNodeVO rPanUserFile2FolderTreeNodeVO(RPanUserFile record);

    RPanUserFileVO rPanUserFile2RPanUserFileVO(RPanUserFile record);

}
