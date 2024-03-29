package com.wuaro.pan.server.modules.user.service;

import com.wuaro.pan.server.modules.user.context.*;
import com.wuaro.pan.server.modules.user.entity.RPanUser;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wuaro.pan.server.modules.user.vo.UserInfoVO;

/**
 * @author 11391
 * @description 针对表【r_pan_user(用户信息表)】的数据库操作Service
 * @createDate 2024-03-06 08:57:37
 */
public interface IUserService extends IService<RPanUser> {

    /**
     * 用户注册业务
     *
     * @param userRegisterContext
     * @return
     */
    Long register(UserRegisterContext userRegisterContext);

    /**
     * 用户登录业务
     *
     * @param userLoginContext
     * @return
     */
    String login(UserLoginContext userLoginContext);

    /**
     * 用户退出登录
     *
     * @param userId
     */
    void exit(Long userId);

    /**
     * 校验用户名
     *
     * @param checkUsernameContext
     * @return
     */
    String checkUsername(CheckUsernameContext checkUsernameContext);

    /**
     * 用户忘记密码-校验密保答案
     *
     * @param checkAnswerContext
     * @return
     */
    String checkAnswer(CheckAnswerContext checkAnswerContext);

    /**
     * 重置用户密码
     *
     * @param resetPasswordContext
     */
    void resetPassword(ResetPasswordContext resetPasswordContext);


    /**
     * 修改用户密码
     *
     * @param changePasswordContext
     */
    void changePassword(ChangePasswordContext changePasswordContext);

    /**
     * 查询在线用户的基本信息
     *
     * @param userId
     * @return
     */
    UserInfoVO info(Long userId);
}