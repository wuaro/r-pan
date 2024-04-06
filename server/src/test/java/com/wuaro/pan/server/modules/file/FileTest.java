package com.wuaro.pan.server.modules.file;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Assert;
import com.google.common.collect.Lists;
import com.wuaro.pan.core.exception.RPanBusinessException;
import com.wuaro.pan.core.utils.IdUtil;
import com.wuaro.pan.server.RPanServerLauncher;
import com.wuaro.pan.server.modules.file.context.*;
import com.wuaro.pan.server.modules.file.entity.RPanFile;
import com.wuaro.pan.server.modules.file.entity.RPanFileChunk;
import com.wuaro.pan.server.modules.file.enums.DelFlagEnum;
//import com.wuaro.pan.server.modules.file.enums.MergeFlagEnum;
//import com.wuaro.pan.server.modules.file.service.IFileChunkService;
//import com.wuaro.pan.server.modules.file.service.IFileService;
import com.wuaro.pan.server.modules.file.service.IUserFileService;
import com.wuaro.pan.server.modules.file.vo.*;
import com.wuaro.pan.server.modules.user.context.UserLoginContext;
import com.wuaro.pan.server.modules.user.context.UserRegisterContext;
import com.wuaro.pan.server.modules.user.service.IUserService;
import com.wuaro.pan.server.modules.user.vo.UserInfoVO;
import lombok.AllArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * 文件模块单元测试类
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = RPanServerLauncher.class)
@Transactional
public class FileTest {
    @Autowired
    private IUserFileService iUserFileService;

    @Autowired
    private IUserService iUserService;

    /**
     * 测试用户查询文件列表成功
     */
    @Test
    public void testQueryUserFileListSuccess() {
        Long userId = register();
        UserInfoVO userInfoVO = info(userId);

        QueryFileListContext context = new QueryFileListContext();
        context.setParentId(userInfoVO.getRootFileId());
        context.setUserId(userId);
        context.setFileTypeArray(null);
        context.setDelFlag(DelFlagEnum.NO.getCode());

        List<RPanUserFileVO> result = iUserFileService.getFileList(context);
        Assert.isTrue(CollectionUtils.isEmpty(result));
    }

    /**
     * 测试创建文件夹成功
     */
    @Test
    public void testCreateFolderSuccess() {

        Long userId = register();
        UserInfoVO userInfoVO = info(userId);

        CreateFolderContext context = new CreateFolderContext();
        context.setParentId(userInfoVO.getRootFileId());
        context.setUserId(userId);
        context.setFolderName("folder-name");

        Long fileId = iUserFileService.createFolder(context);
        Assert.notNull(fileId);
    }

    /**
     * 测试文件重命名失败-文件ID无效
     */
    @Test(expected = RPanBusinessException.class)
    public void testUpdateFilenameFailByWrongFileId() {
        Long userId = register();
        UserInfoVO userInfoVO = info(userId);

        CreateFolderContext context = new CreateFolderContext();
        context.setParentId(userInfoVO.getRootFileId());
        context.setUserId(userId);
        context.setFolderName("folder-name");

        Long fileId = iUserFileService.createFolder(context);
        Assert.notNull(fileId);

        UpdateFilenameContext updateFilenameContext = new UpdateFilenameContext();
        updateFilenameContext.setFileId(fileId + 1);
        updateFilenameContext.setUserId(userId);
        updateFilenameContext.setNewFilename("folder-name-new");

        iUserFileService.updateFilename(updateFilenameContext);
    }

    /**
     * 测试当前用户ID无效
     */
    @Test(expected = RPanBusinessException.class)
    public void testUpdateFilenameFailByWrongUserId() {
        Long userId = register();
        UserInfoVO userInfoVO = info(userId);

        CreateFolderContext context = new CreateFolderContext();
        context.setParentId(userInfoVO.getRootFileId());
        context.setUserId(userId);
        context.setFolderName("folder-name");

        Long fileId = iUserFileService.createFolder(context);
        Assert.notNull(fileId);

        UpdateFilenameContext updateFilenameContext = new UpdateFilenameContext();
        updateFilenameContext.setFileId(fileId);
        updateFilenameContext.setUserId(userId + 1);
        updateFilenameContext.setNewFilename("folder-name-new");

        iUserFileService.updateFilename(updateFilenameContext);
    }

    /**
     * 测试文件名称重复
     */
    @Test(expected = RPanBusinessException.class)
    public void testUpdateFilenameFailByWrongFilename() {
        Long userId = register();
        UserInfoVO userInfoVO = info(userId);

        CreateFolderContext context = new CreateFolderContext();
        context.setParentId(userInfoVO.getRootFileId());
        context.setUserId(userId);
        context.setFolderName("folder-name");

        Long fileId = iUserFileService.createFolder(context);
        Assert.notNull(fileId);

        UpdateFilenameContext updateFilenameContext = new UpdateFilenameContext();
        updateFilenameContext.setFileId(fileId);
        updateFilenameContext.setUserId(userId);
        updateFilenameContext.setNewFilename("folder-name");

        iUserFileService.updateFilename(updateFilenameContext);
    }

    /**
     * 校验文件名称已被占用
     */
    @Test(expected = RPanBusinessException.class)
    public void testUpdateFilenameFailByFilenameUnAvailable() {
        Long userId = register();
        UserInfoVO userInfoVO = info(userId);

        CreateFolderContext context = new CreateFolderContext();
        context.setParentId(userInfoVO.getRootFileId());
        context.setUserId(userId);
        context.setFolderName("folder-name-1");

        Long fileId = iUserFileService.createFolder(context);
        Assert.notNull(fileId);

        context.setParentId(userInfoVO.getRootFileId());
        context.setUserId(userId);
        context.setFolderName("folder-name-2");

        fileId = iUserFileService.createFolder(context);
        Assert.notNull(fileId);

        UpdateFilenameContext updateFilenameContext = new UpdateFilenameContext();
        updateFilenameContext.setFileId(fileId);
        updateFilenameContext.setUserId(userId);
        updateFilenameContext.setNewFilename("folder-name-1");

        iUserFileService.updateFilename(updateFilenameContext);
    }

    /**
     * 测试更新文件名称成功
     */
    @Test
    public void testUpdateFilenameSuccess() {
        Long userId = register();
        UserInfoVO userInfoVO = info(userId);

        CreateFolderContext context = new CreateFolderContext();
        context.setParentId(userInfoVO.getRootFileId());
        context.setUserId(userId);
        context.setFolderName("folder-name-old");

        Long fileId = iUserFileService.createFolder(context);
        Assert.notNull(fileId);

        UpdateFilenameContext updateFilenameContext = new UpdateFilenameContext();
        updateFilenameContext.setFileId(fileId);
        updateFilenameContext.setUserId(userId);
        updateFilenameContext.setNewFilename("folder-name-new");

        iUserFileService.updateFilename(updateFilenameContext);
    }


    /************************************************private************************************************/


    /**
     * 用户注册
     *
     * @return 新用户的ID
     */
    private Long register() {
        UserRegisterContext context = createUserRegisterContext();
        Long register = iUserService.register(context);
        Assert.isTrue(register.longValue() > 0L);
        return register;
    }

    /**
     * 查询登录用户的基本信息
     *
     * @param userId
     * @return
     */
    private UserInfoVO info(Long userId) {
        UserInfoVO userInfoVO = iUserService.info(userId);
        Assert.notNull(userInfoVO);
        return userInfoVO;
    }
    private final static String USERNAME = "wuaro";
    private final static String PASSWORD = "123456789";
    private final static String QUESTION = "question";
    private final static String ANSWER = "answer";

    /**
     * 构建注册用户上下文信息
     *
     * @return
     */
    private UserRegisterContext createUserRegisterContext() {
        UserRegisterContext context = new UserRegisterContext();
        context.setUsername(USERNAME);
        context.setPassword(PASSWORD);
        context.setQuestion(QUESTION);
        context.setAnswer(ANSWER);
        return context;
    }

    /**
     * 构建用户登录上下文实体
     *
     * @return
     */
    private UserLoginContext createUserLoginContext() {
        UserLoginContext userLoginContext = new UserLoginContext();
        userLoginContext.setUsername(USERNAME);
        userLoginContext.setPassword(PASSWORD);
        return userLoginContext;
    }


}
