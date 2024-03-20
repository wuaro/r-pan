package com.wuaro.pan.server.modules.user.converter;

import com.wuaro.pan.server.modules.file.entity.RPanUserFile;
import com.wuaro.pan.server.modules.user.context.*;
import com.wuaro.pan.server.modules.user.entity.RPanUser;
import com.wuaro.pan.server.modules.user.po.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * 用户模块实体转化工具类
 *
 * 注解：
 *      1.@Mapper(componentModel = "spring")
 *          是 MapStruct 库中的注解，用于生成 Spring Bean 的映射器接口。
 *          MapStruct 是一个用于生成 Java 对象之间映射代码的代码生成工具，它可以帮助简化对象之间的转换工作。
 *          componentModel = "spring" 表示使用 Spring 容器管理生成的映射器接口，
 *          这样在 Spring Boot 或 Spring Framework 中可以通过自动装配将生成的映射器注入到其他组件中使用。
 */
@Mapper(componentModel = "spring")
public interface UserConverter {

    /**
     * UserRegisterPO转化成UserRegisterContext
     *
     * @param userRegisterPO
     * @return
     */
    UserRegisterContext userRegisterPO2UserRegisterContext(UserRegisterPO userRegisterPO);

    /**
     * UserRegisterContext转RPanUser
     *
     * @param userRegisterContext
     * @return
     */
    /*
        注解：
            1. @Mapping(target = "password" , ignore = true)
                需要把上下文里面用户传过来的password忽略掉，以为我们需要对明文的password进行加密处理
     */
    @Mapping(target = "password" , ignore = true)
    RPanUser userRegisterContext2RPanUser(UserRegisterContext userRegisterContext);

    /**
     * UserLoginPO转UserLoginContext
     *
     * @param userLoginPO
     * @return
     */
    UserLoginContext userLoginPO2UserLoginContext(UserLoginPO userLoginPO);
//
//    /**
//     * CheckUsernamePO转CheckUsernameContext
//     *
//     * @param checkUsernamePO
//     * @return
//     */
//    CheckUsernameContext checkUsernamePO2CheckUsernameContext(CheckUsernamePO checkUsernamePO);
//
//    /**
//     * CheckAnswerPO转CheckAnswerContext
//     *
//     * @param checkAnswerPO
//     * @return
//     */
//    CheckAnswerContext checkAnswerPO2CheckAnswerContext(CheckAnswerPO checkAnswerPO);
//
//    /**
//     * ResetPasswordPO转ResetPasswordContext
//     *
//     * @param resetPasswordPO
//     * @return
//     */
//    ResetPasswordContext resetPasswordPO2ResetPasswordContext(ResetPasswordPO resetPasswordPO);
//
//    /**
//     * ChangePasswordPO转ChangePasswordContext
//     *
//     * @param changePasswordPO
//     * @return
//     */
//    ChangePasswordContext changePasswordPO2ChangePasswordContext(ChangePasswordPO changePasswordPO);

//    /**
//     * 拼装用户基本信息返回实体
//     *
//     * @param rPanUser
//     * @param rPanUserFile
//     * @return
//     */
//    @Mapping(source = "rPanUser.username", target = "username")
//    @Mapping(source = "rPanUserFile.fileId", target = "rootFileId")
//    @Mapping(source = "rPanUserFile.filename", target = "rootFilename")
//    UserInfoVO assembleUserInfoVO(RPanUser rPanUser, RPanUserFile rPanUserFile);

}
