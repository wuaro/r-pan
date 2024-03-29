package com.wuaro.pan.server.modules.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wuaro.pan.cache.core.constants.CacheConstants;
import com.wuaro.pan.core.exception.RPanBusinessException;
import com.wuaro.pan.core.response.ResponseCode;
import com.wuaro.pan.core.utils.IdUtil;
import com.wuaro.pan.core.utils.JwtUtil;
import com.wuaro.pan.core.utils.PasswordUtil;
import com.wuaro.pan.server.modules.file.constants.FileConstants;
import com.wuaro.pan.server.modules.file.context.CreateFolderContext;
import com.wuaro.pan.server.modules.file.entity.RPanUserFile;
import com.wuaro.pan.server.modules.file.service.IUserFileService;
import com.wuaro.pan.server.modules.user.constants.UserConstants;
import com.wuaro.pan.server.modules.user.context.*;
import com.wuaro.pan.server.modules.user.converter.UserConverter;
import com.wuaro.pan.server.modules.user.entity.RPanUser;
import com.wuaro.pan.server.modules.user.service.IUserService;
import com.wuaro.pan.server.modules.user.mapper.RPanUserMapper;
import com.wuaro.pan.server.modules.user.vo.UserInfoVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @author 11391
 * @description 针对表【r_pan_user(用户信息表)】的数据库操作Service实现
 * @createDate 2024-03-06 08:57:37
 */
@Service(value = "userService")
public class UserServiceImpl extends ServiceImpl<RPanUserMapper, RPanUser>
        implements IUserService {


    @Autowired
    private UserConverter userConverter;

    @Autowired
    private IUserFileService iUserFileService;

    @Autowired
    private CacheManager cacheManager;


    /**
     *  注册步骤：
     *  1. 装填用户实体对象
     *  2. 注册（将用户信息存入数据库表中，而且要避免用户重复）
     *  3. 创建用户所属的根文件夹
     *
     *  用户注册的业务实现
     *  需要实现的功能点：
     *  1. 注册用户信息
     *  2. 创建新用户的根本目录信息
     *
     *  需要实现的技术难点：
     *  1. 该业务是幂等的
     *  2. 要保证用户名全局唯一
     *
     *  实现技术难点的处理方案：
     *  1. 幂等性通过数据库表对于用户名字段添加唯一索引，我们上游业务捕获对应的冲突异常，转化返回
     *
     * @param userRegisterContext
     * @return
     */
    @Override
    public Long register(UserRegisterContext userRegisterContext) {
        assembleUserEntity(userRegisterContext);
        doRegister(userRegisterContext);
        createUserRootFolder(userRegisterContext);
        return userRegisterContext.getEntity().getUserId();
    }

    /**
     * 用户登录业务实现
     *
     * 需要实现的功能：
     * 1、用户的登录信息校验
     * 2、生成一个具有时效性的accessToken
     * 3、将accessToken缓存起来，去实现单机登录
     *
     * @param userLoginContext
     * @return
     */
    @Override
    public String login(UserLoginContext userLoginContext) {
        checkLoginInfo(userLoginContext);
        generateAndSaveAccessToken(userLoginContext);
        return userLoginContext.getAccessToken();
    }

    /**
     * 用户退出登录
     *
     * 1、清除用户的登录凭证缓存
     *
     * @param userId
     */
    /*
        执行逻辑：
            1. cacheManager.getCache(CacheConstants.R_PAN_CACHE_NAME)
                获取缓存管理器中指定名称的缓存对象。
            2. cache.evict(UserConstants.USER_LOGIN_PREFIX + userId)
                从缓存中移除指定key的数据，这里的key是由用户登录前缀和用户ID组成的，key对应的value是该用户登录时生成的accessToken
            3. 如果在移除缓存数据时发生异常，会捕获异常并打印异常信息，然后抛出自定义的 RPanBusinessException 异常，提示用户退出登录失败。
            总体来说，这段代码主要是用于清除用户登录时生成的访问令牌（JWT）的缓存数据，以实现用户退出登录操作。
     */
    @Override
    public void exit(Long userId) {
        try {
            Cache cache = cacheManager.getCache(CacheConstants.R_PAN_CACHE_NAME);
            cache.evict(UserConstants.USER_LOGIN_PREFIX + userId);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RPanBusinessException("用户退出登录失败");
        }
    }

    /**
     * 用户忘记密码-校验用户名称
     *
     * @param checkUsernameContext
     * @return
     */
    /*
    参数：
        1. CheckUsernameContext checkUsernameContext
    返回值：
        1. String question：密保问题
    执行逻辑：
        1. String question = baseMapper.selectQuestionByUsername(checkUsernameContext.getUsername());
            从checkUsernameContext中获取用户名，通过baseMapper.selectQuestionByUsername查询数据库表，返回密保问题
        2. if (StringUtils.isBlank(question)) {
                throw new RPanBusinessException("没有此用户");
            }
            return question;
            如果查出的值为null，说明根本没有该用户的记录，报错
            如果查出的值不为null，则返回查出的密保问题
     */
    @Override
    public String checkUsername(CheckUsernameContext checkUsernameContext) {
        String question = baseMapper.selectQuestionByUsername(checkUsernameContext.getUsername());
        if (StringUtils.isBlank(question)) {
            throw new RPanBusinessException("没有此用户");
        }
        return question;
    }

    /**
     * 用户忘记密码-校验密保答案
     *
     * @param checkAnswerContext
     * @return
     */
    /*
    参数：
        1. CheckAnswerContext checkAnswerContext
            检验密保问题答案上下文、
    执行逻辑：
        根据传进来的checkAnswerContext中的用户名、密保问题、密保答案，进数据库表中查询，
        如果查询出的数据条数>1，则说明密保问题回答正确，则生成一个临时token（寿命为5分钟）
        否则报错
     */
    @Override
    public String checkAnswer(CheckAnswerContext checkAnswerContext) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("username", checkAnswerContext.getUsername());
        queryWrapper.eq("question", checkAnswerContext.getQuestion());
        queryWrapper.eq("answer", checkAnswerContext.getAnswer());
        int count = count(queryWrapper);

        if (count == 0) {
            throw new RPanBusinessException("密保答案错误");
        }

        return generateCheckAnswerToken(checkAnswerContext);
    }
    /**
     * 重置用户密码
     * 1、校验token是不是有效
     * 2、重置密码
     *
     * @param resetPasswordContext
     */
    @Override
    public void resetPassword(ResetPasswordContext resetPasswordContext) {
        checkForgetPasswordToken(resetPasswordContext);
        checkAndResetUserPassword(resetPasswordContext);
    }
    /**
     * 在线修改密码
     * 1、校验旧密码
     * 2、重置新密码
     * 3、退出当前的登录状态
     *
     * @param changePasswordContext
     */
    @Override
    public void changePassword(ChangePasswordContext changePasswordContext) {
        checkOldPassword(changePasswordContext);
        doChangePassword(changePasswordContext);
        exitLoginStatus(changePasswordContext);
    }
    /**
     * 查询在线用户的基本信息
     * 1、查询用户的基本信息实体
     * 2、查询用户的根文件夹信息
     * 3、拼装VO对象返回
     *
     * @param userId
     * @return
     */
    /*
    参数：
        1. Long userId
            用户ID
    返回值：
        1. UserInfoVO 用户信息实体（后端传到前端的实体命名为xxxxVO）
    执行逻辑：
        1. RPanUser entity = getById(userId);
            调用getById(userId)方法从数据库中获取指定userId的用户信息，并将结果赋值给RPanUser类型的变量entity。
        2. if (Objects.isNull(entity)) {
                throw new RPanBusinessException("用户信息查询失败");
            }
            如果获取的用户信息为空，则抛出一个自定义的业务异常RPanBusinessException，并传入错误消息"用户信息查询失败"。
        3. RPanUserFile rPanUserFile = getUserRootFileInfo(userId);
            调用getUserRootFileInfo(userId)方法从数据库中获取指定userId的用户根文件夹信息，
            并将结果赋值给RPanUserFile类型的变量rPanUserFile。
        4. if (Objects.isNull(rPanUserFile)) {
                throw new RPanBusinessException("查询用户根文件夹信息失败");
            }
            如果获取的用户根文件夹信息为空，则抛出一个自定义的业务异常RPanBusinessException，并传入错误消息"查询用户根文件夹信息失败"。
        5. return userConverter.assembleUserInfoVO(entity, rPanUserFile);
            调用userConverter对象的assembleUserInfoVO方法，将获取的用户信息、用户根文件夹信息 转换为一个UserInfoVO对象，
            并将该对象作为方法的返回值。
     */
    @Override
    public UserInfoVO info(Long userId) {
        RPanUser entity = getById(userId);
        if (Objects.isNull(entity)) {
            throw new RPanBusinessException("用户信息查询失败");
        }

        RPanUserFile rPanUserFile = getUserRootFileInfo(userId);
        if (Objects.isNull(rPanUserFile)) {
            throw new RPanBusinessException("查询用户根文件夹信息失败");
        }
        return userConverter.assembleUserInfoVO(entity, rPanUserFile);
    }







    /************************************************private************************************************/


    /**
     * 生成登陆之后的凭证accessToken，并存入缓存
     *
     * @param userLoginContext
     */
    /*
        执行逻辑：
            1. RPanUser entity = userLoginContext.getEntity();
                从传入的 UserLoginContext 中获取用户实体对象。
            2. String accessToken = JwtUtil.generateToken(entity.getUsername(),
                                                        UserConstants.LOGIN_USER_ID,
                                                        entity.getUserId(),
                                                        UserConstants.ONE_DAY_LONG);
                使用 JwtUtil 工具类生成访问令牌，该方法将用户的用户名、登录用户ID、用户ID和过期时间作为参数。
                这里过期时间是UserConstants.ONE_DAY_LONG，也就是24小时
                生成的访问令牌一般是一个加密的字符串，用于标识用户身份和授权信息。
            3. Cache cache = cacheManager.getCache(CacheConstants.R_PAN_CACHE_NAME);
                通过缓存管理器获取缓存对象，这里使用了 CacheConstants.R_PAN_CACHE_NAME 指定的缓存名称。
            4. cache.put(UserConstants.USER_LOGIN_PREFIX + entity.getUserId(), accessToken);
                将生成的访问令牌存储到缓存中，使用了 UserConstants.USER_LOGIN_PREFIX + entity.getUserId() 作为缓存的键，
                将生成的访问令牌作为值存储。
            5. userLoginContext.setAccessToken(accessToken);
                将生成的访问令牌设置到 UserLoginContext 对象中，方便后续使用。
            总体来说，这段代码的作用是生成用户的访问令牌并将其存储到缓存中，以便在用户登录后使用该令牌进行身份验证和授权操作。
     */
    private void generateAndSaveAccessToken(UserLoginContext userLoginContext) {
        RPanUser entity = userLoginContext.getEntity();

        String accessToken = JwtUtil.generateToken(entity.getUsername(), UserConstants.LOGIN_USER_ID, entity.getUserId(), UserConstants.ONE_DAY_LONG);

        Cache cache = cacheManager.getCache(CacheConstants.R_PAN_CACHE_NAME);
        cache.put(UserConstants.USER_LOGIN_PREFIX + entity.getUserId(), accessToken);

        userLoginContext.setAccessToken(accessToken);
    }

    /**
     * 校验用户名密码
     * 校验失败则报错，校验成功则将用户信息设置到登录上下文对象中
     *
     * @param userLoginContext
     */
    /*
        执行逻辑：
            1. String username = userLoginContext.getUsername();
                String password = userLoginContext.getPassword();
                获取用户登录上下文中的用户名、密码。
            2. RPanUser entity = getRPanUserByUsername(username);
                根据用户名从数据库中获取用户信息。这个方法可能会查询数据库或缓存，获取用户的相关信息。
            3. if (Objects.isNull(entity)) { throw new RPanBusinessException("用户名称不存在"); }
                如果获取的用户信息为 null，则抛出异常，表示用户名不存在。
            4. String salt = entity.getSalt();
                获取用户的盐值，用于密码加密验证。
            5. String encPassword = PasswordUtil.encryptPassword(salt, password);
                使用盐值和输入的密码对密码进行加密，生成加密后的密码。
            6. String dbPassword = entity.getPassword();
                获取数据库中存储的用户密码。
            7. if (!Objects.equals(encPassword, dbPassword)) { throw new RPanBusinessException("密码信息不正确"); }
                如果加密后的密码与数据库中的密码不匹配，则抛出异常，表示密码不正确。
            8. userLoginContext.setEntity(entity);
                将获取到的用户信息设置回登录上下文对象中，以便后续操作使用。
            总之，这段代码的作用是验证用户登录信息是否正确，包括用户名的存在性和密码的正确性。
            如果验证通过，则将用户信息设置到登录上下文对象中，以便后续使用。如果验证不通过，则抛出相应的业务异常。
     */
    private void checkLoginInfo(UserLoginContext userLoginContext) {
        String username = userLoginContext.getUsername();
        String password = userLoginContext.getPassword();

        RPanUser entity = getRPanUserByUsername(username);
        if (Objects.isNull(entity)) {
            throw new RPanBusinessException("用户名称不存在");
        }

        String salt = entity.getSalt();
        String encPassword = PasswordUtil.encryptPassword(salt, password);
        String dbPassword = entity.getPassword();
        if (!Objects.equals(encPassword, dbPassword)) {
            throw new RPanBusinessException("密码信息不正确");
        }

        userLoginContext.setEntity(entity);
    }

    /**
     * 通过用户名称获取用户实体信息
     * 在数据库表中查询
     *
     * @param username
     * @return
     */
    /*
        执行逻辑：
            1. QueryWrapper queryWrapper = new QueryWrapper();
                创建一个查询条件包装器对象。
            2. queryWrapper.eq("username", username);
                设置查询条件，要求用户名字段等于指定的用户名。
            3. return getOne(queryWrapper);
                使用 MyBatis-Plus 的 getOne 方法执行查询操作，根据查询条件从数据库中获取一条符合条件的用户信息。
                如果找到符合条件的用户信息，则返回该用户信息；如果没有找到，则返回 null。
            这个方法的作用是根据用户名从数据库中查询用户信息，并返回查询结果。如果需要获取用户的详细信息，可以调用这个方法并传入用户名进行查询。
     */
    private RPanUser getRPanUserByUsername(String username) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("username", username);
        return getOne(queryWrapper);
    }

    /**
     * 实体转化
     * 由上下文信息转化成用户实体，封装进上下文
     * 在此过程中，将password利用salt值加密
     *
     * @param userRegisterContext
     */
    /*
        参数：
            1. UserRegisterContext userRegisterContext
                用户注册上下文类的一个对象
        执行逻辑：
            1. RPanUser entity = userConverter.userRegisterContext2RPanUser(userRegisterContext);
                调用 userConverter 对象的 userRegisterContext2RPanUser 方法，
                将 UserRegisterContext 对象转换为 RPanUser 对象，并将结果赋值给 entity 变量。
            2. String salt = PasswordUtil.getSalt(),
                调用 PasswordUtil 类的 getSalt 方法，生成一个随机的盐值，并将结果赋值给 salt 变量。
            3. String dbPassword = PasswordUtil.encryptPassword(salt, userRegisterContext.getPassword());
                调用 PasswordUtil 类的 encryptPassword 方法，
                使用盐值和用户注册的密码对密码进行加密，并将加密后的密码赋值给 dbPassword 变量。
            4. entity.setUserId(IdUtil.get());
                调用 IdUtil 类的 get 方法，生成一个唯一的用户ID，并将其设置到 entity 对象的 userId 属性中。
                entity.setSalt(salt);
                entity.setPassword(dbPassword);（存入加密后的密码）
                entity.setCreateTime(new Date());
                entity.setUpdateTime(new Date());
                userRegisterContext.setEntity(entity);
                以上代码组装了entity对象，将组装好的 entity 对象设置到 userRegisterContext 对象中
            总体来说，这段代码的作用是根据用户注册上下文信息组装一个用户实体对象，并对密码进行加密处理，
            设置创建时间、更新时间等属性，并将组装好的实体对象保存在 userRegisterContext 中。
     */
    private void assembleUserEntity(UserRegisterContext userRegisterContext) {
        RPanUser entity = userConverter.userRegisterContext2RPanUser(userRegisterContext);
        String salt = PasswordUtil.getSalt(),
                dbPassword = PasswordUtil.encryptPassword(salt, userRegisterContext.getPassword());
        entity.setUserId(IdUtil.get());
        entity.setSalt(salt);
        entity.setPassword(dbPassword);
        entity.setCreateTime(new Date());
        entity.setUpdateTime(new Date());
        userRegisterContext.setEntity(entity);
    }

    /**
     * 实现注册用户的业务
     * 需要捕获数据库的唯一索引冲突异常，来实现全局用户名称唯一
     *
     * @param userRegisterContext
     */
    /*
        参数：
            1. UserRegisterContext userRegisterContext
                用户注册上下文
        执行逻辑：
            1. RPanUser entity = userRegisterContext.getEntity();
                ...
                throw new RPanBusinessException(ResponseCode.ERROR);
                获取用户注册上下文对象中的用户实体，如果没有，则报错
            2. if (Objects.nonNull(entity)) {
                    try {
                        if (!save(entity)) {
                            throw new RPanBusinessException("用户注册失败");
                        }
                    } catch (DuplicateKeyException duplicateKeyException) {
                        throw new RPanBusinessException("用户名已存在");
                    }
                    return;
                }
                如果用户实体不为空，则使用mybatis-plus的save方法将用户信息存入数据库表中
                如果存入失败，则报"用户注册失败"错误
                存入过程中利用catch (DuplicateKeyException duplicateKeyException) { ... }:
                    来捕获可能出现的主键重复异常（DuplicateKeyException），表示用户名已存在，抛出用户名已存在的业务异常。
     */
    private void doRegister(UserRegisterContext userRegisterContext) {
        RPanUser entity = userRegisterContext.getEntity();
        if (Objects.nonNull(entity)) {
            try {
                if (!save(entity)) {
                    throw new RPanBusinessException("用户注册失败");
                }
            } catch (DuplicateKeyException duplicateKeyException) {
                throw new RPanBusinessException("用户名已存在");
            }
            return;
        }
        throw new RPanBusinessException(ResponseCode.ERROR);
    }

    /**
     * 创建用户的根目录信息
     * 父文件夹id=0，文件夹名称="全部文件"，用户id，
     *
     * @param userRegisterContext
     */
    private void createUserRootFolder(UserRegisterContext userRegisterContext) {
        CreateFolderContext createFolderContext = new CreateFolderContext();
        createFolderContext.setParentId(FileConstants.TOP_PARENT_ID);
        createFolderContext.setUserId(userRegisterContext.getEntity().getUserId());
        createFolderContext.setFolderName(FileConstants.ALL_FILE_CN_STR);
        iUserFileService.createFolder(createFolderContext);
    }

    /**
     * 生成用户忘记密码-校验密保答案通过的临时token
     * token的失效时间为五分钟之后
     *
     * @param checkAnswerContext
     * @return
     */
    private String generateCheckAnswerToken(CheckAnswerContext checkAnswerContext) {
        String token = JwtUtil.generateToken(checkAnswerContext.getUsername(), UserConstants.FORGET_USERNAME, checkAnswerContext.getUsername(), UserConstants.FIVE_MINUTES_LONG);
        return token;
    }

    /**
     * 校验用户信息并重置用户密码
     *
     * @param resetPasswordContext
     */
    /*
    参数：
        1. ResetPasswordContext resetPasswordContext
            重置密码上下文对象
    执行逻辑：
        1. String username = resetPasswordContext.getUsername();
           String password = resetPasswordContext.getPassword();
            从ResetPasswordContext对象中获取用户名、新密码。
        2. RPanUser entity = getRPanUserByUsername(username);
            根据用户名从数据库中获取用户信息。
        3. if (Objects.isNull(entity)) {
                throw new RPanBusinessException("用户信息不存在");
            }
            检查获取的用户信息是否为空，如果为空说明用户信息不存在，抛出相应的业务异常。
        4. String newDbPassword = PasswordUtil.encryptPassword(entity.getSalt(), password);
            使用PasswordUtil工具类对新密码进行加密，加密时需要使用用户的盐值。
        5. entity.setPassword(newDbPassword);
            将加密后的新密码设置到用户实体对象中。
        6. entity.setUpdateTime(new Date());
            设置用户信息更新时间为当前时间。
        7. if (!updateById(entity)) {
                throw new RPanBusinessException("重置用户密码失败");
            }
            调用数据库更新操作将修改后的用户信息保存到数据库中，如果更新操作失败则抛出相应的业务异常。
     */
    private void checkAndResetUserPassword(ResetPasswordContext resetPasswordContext) {
        String username = resetPasswordContext.getUsername();
        String password = resetPasswordContext.getPassword();
        RPanUser entity = getRPanUserByUsername(username);
        if (Objects.isNull(entity)) {
            throw new RPanBusinessException("用户信息不存在");
        }

        String newDbPassword = PasswordUtil.encryptPassword(entity.getSalt(), password);
        entity.setPassword(newDbPassword);
        entity.setUpdateTime(new Date());

        if (!updateById(entity)) {
            throw new RPanBusinessException("重置用户密码失败");
        }
    }

    /**
     * 验证忘记密码的token是否有效
     *
     * @param resetPasswordContext
     */
    /*
    参数：
        1. ResetPasswordContext resetPasswordContext
            重置密码上下文对象
    运行逻辑：
        1. String token = resetPasswordContext.getToken();
            从ResetPasswordContext对象中获取重置密码的令牌。
        2. Object value = JwtUtil.analyzeToken(token, UserConstants.FORGET_USERNAME);
            使用JwtUtil工具类解析令牌，其中UserConstants.FORGET_USERNAME是用于解析用户名的键。
            这个方法返回的是解析出来的令牌中的用户名（重点！！！）。
        3. if (Objects.isNull(value)) {
                throw new RPanBusinessException(ResponseCode.TOKEN_EXPIRE);
            }
            检查解析出来的值是否为空，如果为空说明令牌已过期，抛出相应的业务异常。
        4. String tokenUsername = String.valueOf(value);
            将解析出来的值转换为字符串，即令牌中的用户名。
        5. if (!Objects.equals(tokenUsername, resetPasswordContext.getUsername())) {
                throw new RPanBusinessException("token错误");
            }
            检查解析出来的用户名是否与ResetPasswordContext对象中的用户名匹配，如果不匹配说明令牌错误，抛出相应的业务异常。
     */
    private void checkForgetPasswordToken(ResetPasswordContext resetPasswordContext) {
        String token = resetPasswordContext.getToken();
        Object value = JwtUtil.analyzeToken(token, UserConstants.FORGET_USERNAME);
        if (Objects.isNull(value)) {
            throw new RPanBusinessException(ResponseCode.TOKEN_EXPIRE);
        }
        String tokenUsername = String.valueOf(value);
        if (!Objects.equals(tokenUsername, resetPasswordContext.getUsername())) {
            throw new RPanBusinessException("token错误");
        }
    }
    /**
     * 退出用户的登录状态
     *
     * @param changePasswordContext
     */
    private void exitLoginStatus(ChangePasswordContext changePasswordContext) {
        exit(changePasswordContext.getUserId());
    }

    /**
     * 修改新密码
     *
     * @param changePasswordContext
     */
    /*
    参数：
        1.
    执行逻辑：
        1. String newPassword = changePasswordContext.getNewPassword();
            RPanUser entity = changePasswordContext.getEntity();
            String salt = entity.getSalt();
            获取用户输入的新密码
            获取当前用户的实体
            从用户实体中获取用户的salt盐值
        2.String encNewPassword = PasswordUtil.encryptPassword(salt, newPassword);
            entity.setPassword(encNewPassword);
            将新密码结合salt盐值进行加密
            将加密后的密码存入用户实体中
        3. if (!updateById(entity)) {
                throw new RPanBusinessException("修改用户密码失败");
            }
            如果数据库更新失败则报错
     */
    private void doChangePassword(ChangePasswordContext changePasswordContext) {
        String newPassword = changePasswordContext.getNewPassword();
        RPanUser entity = changePasswordContext.getEntity();
        String salt = entity.getSalt();

        String encNewPassword = PasswordUtil.encryptPassword(salt, newPassword);

        entity.setPassword(encNewPassword);

        if (!updateById(entity)) {
            throw new RPanBusinessException("修改用户密码失败");
        }
    }

    /**
     * 校验用户的旧密码
     * 改不周会查询并封装用户的实体信息到上下文对象中
     *
     * @param changePasswordContext
     */
    /*
    参数：
        1.
    执行逻辑：
        1. Long userId = changePasswordContext.getUserId();
            String oldPassword = changePasswordContext.getOldPassword();
            从changePasswordContext中获取用户ID和旧密码。
        2. RPanUser entity = getById(userId);
            if (Objects.isNull(entity)) {
                throw new RPanBusinessException("用户信息不存在");
            }
            使用用户ID查询数据库中对应的用户信息。
            如果查询到的用户信息为null，表示用户信息不存在，抛出业务异常。
        3. changePasswordContext.setEntity(entity);
            将查询到的用户信息设置到changePasswordContext中，方便后续方法使用。
        4. String encOldPassword = PasswordUtil.encryptPassword(entity.getSalt(), oldPassword);
            String dbOldPassword = entity.getPassword();
            if (!Objects.equals(encOldPassword, dbOldPassword)) {
                throw new RPanBusinessException("旧密码不正确");
            }
            使用密码工具类PasswordUtil对输入的旧密码进行加密，然后与数据库中存储的旧密码进行比较。
            如果加密后的旧密码与数据库中的旧密码不相等，表示旧密码不正确，抛出业务异常。
     */
    private void checkOldPassword(ChangePasswordContext changePasswordContext) {
        Long userId = changePasswordContext.getUserId();
        String oldPassword = changePasswordContext.getOldPassword();

        RPanUser entity = getById(userId);
        if (Objects.isNull(entity)) {
            throw new RPanBusinessException("用户信息不存在");
        }
        changePasswordContext.setEntity(entity);

        String encOldPassword = PasswordUtil.encryptPassword(entity.getSalt(), oldPassword);
        String dbOldPassword = entity.getPassword();
        if (!Objects.equals(encOldPassword, dbOldPassword)) {
            throw new RPanBusinessException("旧密码不正确");
        }
    }

    /**
     * 获取用户根文件夹信息实体
     *
     * @param userId
     * @return
     */
    private RPanUserFile getUserRootFileInfo(Long userId) {
        return iUserFileService.getUserRootFile(userId);
    }

}