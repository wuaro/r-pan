package com.wuaro.pan.server.modules.file.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wuaro.pan.core.constants.RPanConstants;
import com.wuaro.pan.core.exception.RPanBusinessException;
import com.wuaro.pan.core.utils.IdUtil;
import com.wuaro.pan.server.modules.file.constants.FileConstants;
import com.wuaro.pan.server.modules.file.context.CreateFolderContext;
import com.wuaro.pan.server.modules.file.context.QueryFileListContext;
import com.wuaro.pan.server.modules.file.context.UpdateFilenameContext;
import com.wuaro.pan.server.modules.file.entity.RPanUserFile;
import com.wuaro.pan.server.modules.file.enums.DelFlagEnum;
import com.wuaro.pan.server.modules.file.enums.FolderFlagEnum;
import com.wuaro.pan.server.modules.file.service.IUserFileService;
import com.wuaro.pan.server.modules.file.mapper.RPanUserFileMapper;
import com.wuaro.pan.server.modules.file.vo.RPanUserFileVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.stream.Collectors;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;

import java.util.Date;

/**
 * @author 11391
 * @description 针对表【r_pan_user_file(用户文件信息表)】的数据库操作Service实现
 * @createDate 2024-03-06 08:59:39
 */
/*
    注解：
        1. @Service(value = "userFileService")
            将 UserFileService 类标识为一个服务类，并指定在 Spring 容器中的名称为 "userFileService"，
            可以通过这个名称在其他地方引用这个服务类。
 */
@Service(value = "userFileService")
public class UserFileServiceImpl extends ServiceImpl<RPanUserFileMapper, RPanUserFile>
        implements IUserFileService {

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


    /************************************************private************************************************/

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
     * <p>
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
        //这里save方法是怎么知道要将数据保存进哪个表的呢？？？？？
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