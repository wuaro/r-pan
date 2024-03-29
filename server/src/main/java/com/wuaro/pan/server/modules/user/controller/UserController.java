package com.wuaro.pan.server.modules.user.controller;

import com.wuaro.pan.core.response.R;
import com.wuaro.pan.core.utils.IdUtil;
import com.wuaro.pan.server.common.annotation.LoginIgnore;
import com.wuaro.pan.server.common.utils.UserIdUtil;
import com.wuaro.pan.server.modules.user.context.*;
import com.wuaro.pan.server.modules.user.converter.UserConverter;
import com.wuaro.pan.server.modules.user.po.*;
import com.wuaro.pan.server.modules.user.service.IUserService;
import com.wuaro.pan.server.modules.user.vo.UserInfoVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 该类是用户模块的控制器实体
 */
@RestController
@RequestMapping("user")
@Api(tags = "用户模块")
public class UserController {

    @Autowired
    private IUserService iUserService;

    @Autowired
    private UserConverter userConverter;


    /**
     * 用户注册接口
     *
     * @param userRegisterPO
     * @return
     */
    /*
    注解：
        1. @ApiOperation：
            swagger2的接口文档注解，其中如下两行的意思是 接参和返回值 格式都是JSON字符串：
              consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
              produces = MediaType.APPLICATION_JSON_UTF8_VALUE
        2. @PostMapping("register")
            是一个用于处理 HTTP POST 请求的 Spring Boot 注解。在这个注解中，"register" 是指定的请求路径。
            当客户端发送一个 POST 请求到 /register 路径时，
            Spring Boot 应用程序将调用被 @PostMapping("register") 注解标记的方法来处理该请求。
        3. @LoginIgnore
            自定义注解
            在CommonLoginAspect类中的checkNeedCheckLoginInfo()方法会检测被@LoginIgnore注解标注的方法
            这些方法全部忽略校验，即无需进行登录校验
        4. @Validated
            @Validated 是 Spring 框架中用来进行参数校验的注解。
            在上下文中，它通常与 Spring MVC 的 @RequestBody 注解一起使用，用于对请求体中的参数进行校验。
            具体来说，@Validated 可以放在 Controller 方法的参数上，表示对该参数进行校验。
            在方法参数上使用 @Validated 注解后，Spring 框架会根据对象中的注解（如 @NotNull、@NotBlank、@Min、@Max 等）进行参数校验。
            在 Spring MVC 中，如果一个类中的字段包含了校验注解（例如 @NotBlank、@NotNull 等），并且该类作为方法的参数，
            需要进行参数校验，那么该方法的参数 必须使用 @Validated 或 @Valid 注解来标记！！
        5. @RequestBody
            @RequestBody 注解用于将 HTTP 请求体中的数据绑定到方法的参数上，通常用于处理 POST 请求中的 JSON 数据。
            当客户端发送 POST 请求时，请求体中的 JSON 数据会被映射到被 @RequestBody 注解标记的方法参数上，从而可以在方法中直接使用这些数据。
            说白了就是讲JSON格式的数据映射到PO类中
    参数：
        1. UserRegisterPO userRegisterPO
            用户注册的参数实体（将JSON格式的数据映射到该实体类中）
    执行逻辑：
        1. UserRegisterContext userRegisterContext = userConverter.userRegisterPO2UserRegisterContext(userRegisterPO);
            将userRegisterPO转化为UserRegisterContext
            将实体 转化为 上下文对象
        2. Long userId = iUserService.register(userRegisterContext);
            执行注册，返回生成的用户ID
        3. return R.data(IdUtil.encrypt(userId));
            进行id加密，并将加密后的用户id封装成R对象返回给客户端。
    */
    @ApiOperation(
            value = "用户注册接口",
            notes = "该接口提供了用户注册的功能，实现了冥等性注册的逻辑，可以放心多并发调用",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @LoginIgnore
    @PostMapping("register")
    public R register(@Validated @RequestBody UserRegisterPO userRegisterPO) {
        UserRegisterContext userRegisterContext = userConverter.userRegisterPO2UserRegisterContext(userRegisterPO);
        Long userId = iUserService.register(userRegisterContext);
        return R.data(IdUtil.encrypt(userId));
    }


    /**
     * 用户登录接口
     *
     * @param userLoginPO
     * @return
     */
    /*
    注解：
        1. @ApiOperation：
            swagger2的接口文档注解，其中如下两行的意思是 接参和返回值 格式都是JSON字符串：
              consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
              produces = MediaType.APPLICATION_JSON_UTF8_VALUE
        2. @PostMapping("login")
            是一个用于处理 HTTP POST 请求的 Spring Boot 注解。在这个注解中，"login" 是指定的请求路径。
            当客户端发送一个 POST 请求到 /login 路径时，
            Spring Boot 应用程序将调用被 @PostMapping("login") 注解标记的方法来处理该请求。
        3. @LoginIgnore
            自定义注解
            在CommonLoginAspect类中的checkNeedCheckLoginInfo()方法会检测被@LoginIgnore注解标注的方法
            这些方法全部忽略校验，即无需进行登录校验
        4. @Validated
            @Validated 是 Spring 框架中用来进行参数校验的注解。
            在上下文中，它通常与 Spring MVC 的 @RequestBody 注解一起使用，用于对请求体中的参数进行校验。
            具体来说，@Validated 可以放在 Controller 方法的参数上，表示对该参数进行校验。
            在方法参数上使用 @Validated 注解后，Spring 框架会根据对象中的注解（如 @NotNull、@NotBlank、@Min、@Max 等）进行参数校验。
            在 Spring MVC 中，如果一个类中的字段包含了校验注解（例如 @NotBlank、@NotNull 等），并且该类作为方法的参数，
            需要进行参数校验，那么该方法的参数 必须使用 @Validated 或 @Valid 注解来标记！！
        5. @RequestBody
            @RequestBody 注解用于将 HTTP 请求体中的数据绑定到方法的参数上，通常用于处理 POST 请求中的 JSON 数据。
            当客户端发送 POST 请求时，请求体中的 JSON 数据会被映射到被 @RequestBody 注解标记的方法参数上，从而可以在方法中直接使用这些数据。
            说白了就是讲JSON格式的数据映射到PO类中
    参数：
        1. UserLoginPO userLoginPO
            用户登录的参数实体（将JSON格式的数据映射到该实体类中）
    执行逻辑：
        1. UserLoginContext userLoginContext = userConverter.userLoginPO2UserLoginContext(userLoginPO);
            将userLoginPO转化为userLoginContext
            将实体 转化为 上下文对象
        2. String accessToken = iUserService.login(userLoginContext);
            处理用户登录请求，生成并返回访问令牌（accessToken）
        3. return R.data(accessToken);
            将访问令牌（accessToken）封装成R对象返回给客户端。
    */
    @ApiOperation(
            value = "用户登录接口",
            notes = "该接口提供了用户登录的功能，成功登陆之后，会返回有时效性的accessToken供后续服务使用",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @LoginIgnore
    @PostMapping("login")
    public R login(@Validated @RequestBody UserLoginPO userLoginPO) {
        UserLoginContext userLoginContext = userConverter.userLoginPO2UserLoginContext(userLoginPO);
        String accessToken = iUserService.login(userLoginContext);
        return R.data(accessToken);
    }

    /**
     * 用户登出接口
     *
     * @return
     */
    /*
    注解：
        1. @ApiOperation：
            swagger2的接口文档注解，其中如下两行的意思是 接参和返回值 格式都是JSON字符串：
              consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
              produces = MediaType.APPLICATION_JSON_UTF8_VALUE
        2. @PostMapping("exit")
            是一个用于处理 HTTP POST 请求的 Spring Boot 注解。在这个注解中，"exit" 是指定的请求路径。
            当客户端发送一个 POST 请求到 /exit 路径时，
            Spring Boot 应用程序将调用被 @PostMapping("exit") 注解标记的方法来处理该请求。
    执行逻辑：
        1. iUserService.exit(UserIdUtil.get());
            执行登出逻辑
        3. return R.data(accessToken);
            返回R.success()，表示登出成果
    */
    @ApiOperation(
            value = "用户登出接口",
            notes = "该接口提供了用户登出的功能",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @PostMapping("exit")
    public R exit() {
        iUserService.exit(UserIdUtil.get());
        return R.success();
    }

    /**
     * 用户忘记密码-校验用户名
     *
     * @param checkUsernamePO
     * @return
     */
    /*
    注解：
        1. @ApiOperation：
            swagger2的接口文档注解，其中如下两行的意思是 接参和返回值 格式都是JSON字符串：
              consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
              produces = MediaType.APPLICATION_JSON_UTF8_VALUE
        2. @PostMapping("username/check")
            是一个用于处理 HTTP POST 请求的 Spring Boot 注解。在这个注解中，"username/check" 是指定的请求路径。
            当客户端发送一个 POST 请求到 /username/check 路径时，
            Spring Boot 应用程序将调用被 @PostMapping("username/check") 注解标记的方法来处理该请求。
        3. @LoginIgnore
            自定义注解
            在CommonLoginAspect类中的checkNeedCheckLoginInfo()方法会检测被@LoginIgnore注解标注的方法
            这些方法全部忽略校验，即无需进行登录校验
        4. @Validated
            @Validated 是 Spring 框架中用来进行参数校验的注解。
            在上下文中，它通常与 Spring MVC 的 @RequestBody 注解一起使用，用于对请求体中的参数进行校验。
            具体来说，@Validated 可以放在 Controller 方法的参数上，表示对该参数进行校验。
            在方法参数上使用 @Validated 注解后，Spring 框架会根据对象中的注解（如 @NotNull、@NotBlank、@Min、@Max 等）进行参数校验。
            在 Spring MVC 中，如果一个类中的字段包含了校验注解（例如 @NotBlank、@NotNull 等），并且该类作为方法的参数，
            需要进行参数校验，那么该方法的参数 必须使用 @Validated 或 @Valid 注解来标记！！
        5. @RequestBody
            @RequestBody 注解用于将 HTTP 请求体中的数据绑定到方法的参数上，通常用于处理 POST 请求中的 JSON 数据。
            当客户端发送 POST 请求时，请求体中的 JSON 数据会被映射到被 @RequestBody 注解标记的方法参数上，从而可以在方法中直接使用这些数据。
            说白了就是讲JSON格式的数据映射到PO类中
    参数：
        1. CheckUsernamePO checkUsernamePO
            检查用户名的参数实体（将JSON格式的数据映射到该实体类中）
    执行逻辑：
        1. CheckUsernameContext checkUsernameContext = userConverter.checkUsernamePO2CheckUsernameContext(checkUsernamePO);
            将checkUsernamePO转化为checkUsernameContext
            将实体 转化为 上下文对象
        2. String question = iUserService.checkUsername(checkUsernameContext);
            根据用户名上下文对象，去数据库表中查询密保问题
        3. return R.data(question);
            返回查询到的密保问题
    */
    @ApiOperation(
            value = "用户忘记密码-校验用户名",
            notes = "该接口提供了用户忘记密码-校验用户名的功能",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @LoginIgnore
    @PostMapping("username/check")
    public R checkUsername(@Validated @RequestBody CheckUsernamePO checkUsernamePO) {
        CheckUsernameContext checkUsernameContext = userConverter.checkUsernamePO2CheckUsernameContext(checkUsernamePO);
        String question = iUserService.checkUsername(checkUsernameContext);
        return R.data(question);
    }

    @ApiOperation(
            value = "用户忘记密码-校验密保答案",
            notes = "该接口提供了用户忘记密码-校验密保答案的功能",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @LoginIgnore
    @PostMapping("answer/check")
    public R checkAnswer(@Validated @RequestBody CheckAnswerPO checkAnswerPO) {
        CheckAnswerContext checkAnswerContext = userConverter.checkAnswerPO2CheckAnswerContext(checkAnswerPO);
        String token = iUserService.checkAnswer(checkAnswerContext);
        return R.data(token);
    }

    @ApiOperation(
            value = "用户忘记密码-重置新密码",
            notes = "该接口提供了用户忘记密码-重置新密码的功能",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @PostMapping("password/reset")
    @LoginIgnore
    public R resetPassword(@Validated @RequestBody ResetPasswordPO resetPasswordPO) {
        ResetPasswordContext resetPasswordContext = userConverter.resetPasswordPO2ResetPasswordContext(resetPasswordPO);
        iUserService.resetPassword(resetPasswordContext);
        return R.success();
    }

    @ApiOperation(
            value = "用户在线修改密码",
            notes = "该接口提供了用户在线修改密码的功能",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @PostMapping("password/change")
    public R changePassword(@Validated @RequestBody ChangePasswordPO changePasswordPO) {
        ChangePasswordContext changePasswordContext = userConverter.changePasswordPO2ChangePasswordContext(changePasswordPO);
        changePasswordContext.setUserId(UserIdUtil.get());
        iUserService.changePassword(changePasswordContext);
        return R.success();
    }

    @ApiOperation(
            value = "查询登录用户的基本信息",
            notes = "该接口提供了查询登录用户的基本信息的功能",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @GetMapping("/")
    public R<UserInfoVO> info() {
        UserInfoVO userInfoVO = iUserService.info(UserIdUtil.get());
        return R.data(userInfoVO);
    }

}
