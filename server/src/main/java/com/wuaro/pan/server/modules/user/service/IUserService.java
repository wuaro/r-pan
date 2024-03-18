package com.wuaro.pan.server.modules.user.service;

import com.wuaro.pan.server.modules.user.context.UserRegisterContext;
import com.wuaro.pan.server.modules.user.entity.RPanUser;
import com.baomidou.mybatisplus.extension.service.IService;

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
}