package com.wuaro.pan.server.common.aspect;

import com.wuaro.pan.cache.core.constants.CacheConstants;
import com.wuaro.pan.core.response.R;
import com.wuaro.pan.core.response.ResponseCode;
import com.wuaro.pan.core.utils.JwtUtil;
import com.wuaro.pan.server.common.annotation.LoginIgnore;
import com.wuaro.pan.server.common.utils.UserIdUtil;
import com.wuaro.pan.server.modules.user.constants.UserConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * 统一的登录拦截校验切面逻辑实现类
 */
@Component
@Aspect
@Slf4j
public class CommonLoginAspect {

    /**
     * 登录认证参数名称
     */
    private static final String LOGIN_AUTH_PARAM_NAME = "authorization";

    /**
     * 请求头登录认证key
     */
    private static final String LOGIN_AUTH_REQUEST_HEADER_NAME = "Authorization";

    /**
     * 切点表达式
     * com.wuaro.pan.server.modules下的 所有子module下的 controller包下的 所有方法
     */
    private final static String POINT_CUT = "execution(* com.wuaro.pan.server.modules" +
            ".*.controller..*(..))";

    @Autowired
    private CacheManager cacheManager;

    /**
     * 切点模版方法
     */
    @Pointcut(value = POINT_CUT)
    public void loginAuth() {



    }

    /**
     * 切点的环绕增强逻辑
     * 1、判断需不需要校验登录信息（方法是否被LoginIgnore注解标记）
     * 2、校验登录信息：判断是否登录，如果已登录则执行，未登录则不执行目标方法并报错
     *
     * @param proceedingJoinPoint
     * @return
     * @throws Throwable
     */
    /*
    注解：
        1. @Around("loginAuth()")
            这个注解表示该环绕通知将会拦截所有被 loginAuth() 切点表达式匹配到的方法。
            关于环绕通知：
                1. 环绕通知（Around Advice）是Spring AOP中的一种通知类型，可以在目标方法执行前后都进行拦截和处理。
                    在环绕通知中，你可以自由地控制目标方法的执行，甚至可以完全阻止目标方法的执行，也可以在执行前后进行一系列的操作，
                    比如日志记录、权限校验、事务管理等。
                2. 环绕通知与其他类型的通知不同之处在于它需要手动调用目标方法的执行。
                    在通知方法中，你可以通过 `proceedingJoinPoint.proceed()` 来调用目标方法的执行。
                    这个方法的调用实际上就是触发了整个目标方法的执行过程。
                3. 你还可以在 `proceed()` 方法前后 添加自己的逻辑，实现对目标方法的增强或控制。
                4. 环绕通知方法接收一个 `ProceedingJoinPoint` 类型的参数，通过这个参数可以调用目标方法的执行。
                5. 环绕通知方法最终需要返回一个对象，通常是目标方法的返回值，也可以是其他自定义的返回值。
    参数：
        1. ProceedingJoinPoint proceedingJoinPoint
            是 Spring AOP 中的一个接口，用于表示连接点（Join Point）
            调用proceedingJoinPoint.proceed()可以执行原方法
            在环绕通知中，在执行proceedingJoinPoint.proceed()之前或之后，都可以自定义一些其他操作来执行
    执行逻辑：
        1. @Around("loginAuth()")：
            这个注解表示该环绕通知将会拦截所有被 loginAuth() 切点表达式匹配到的方法。
        2. public Object loginAuthAround(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {：
            这是环绕通知的方法体，接收一个 ProceedingJoinPoint 类型的参数，可以在方法中通过该参数调用目标方法。
        3. checkNeedCheckLoginInfo(proceedingJoinPoint)：
            该方法在后面定义了，该方法主要作用是通过检测目标方法是否被LoginIgnore注解标记来判断该方法是否需要进行本环绕通知
        4. ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();：
            通过 RequestContextHolder 获取当前请求的 ServletRequestAttributes 对象，从而获取请求的相关信息。
            关于RequestContextHolder.getRequestAttributes()：
                用于获取当前请求的 ServletRequestAttributes 对象，它是 Spring 提供的一个用于包装 HTTP 请求和响应的类。
                这个对象包含了当前请求的各种信息，例如请求头、请求参数、请求方法等等。
                它可以用于在任何地方获取当前线程的请求上下文，其中包括 ServletRequestAttributes 对象。
                这个工具类主要用于在非 Web 环境下获取当前请求的相关信息，比如在多线程环境中处理请求。
                在本方法中，通过 RequestContextHolder.getRequestAttributes() 获取到了当前请求的 ServletRequestAttributes 对象，
                并将其强制转换为 ServletRequestAttributes 类型，以便后续对请求的处理和获取请求信息。
                例如，可以通过这个对象获取请求的 URI、参数、头部信息等，用于业务逻辑处理或者日志记录等操作。
        5. HttpServletRequest request = servletRequestAttributes.getRequest();
            String requestURI = request.getRequestURI();
            log.info("成功拦截到请求，URI为：{}", requestURI);
            通过 ServletRequestAttributes 获取到的对象，可以获取到当前请求的 HttpServletRequest 对象，
            进而获取请求的 URI，并打印日志。
        6. if (!checkAndSaveUserId(request)) {
                log.warn("成功拦截到请求，URI为：{}. 检测到用户未登录，将跳转至登录页面", requestURI);
                return R.fail(ResponseCode.NEED_LOGIN);
            }
            调用自定义方法 checkAndSaveUserId(request)，检查并保存用户的登录状态信息。
            如果用户未登录，则返回一个表示需要登录的响应信息。并打印日志，返回异常码
        7. return proceedingJoinPoint.proceed();：
            调用 proceed() 方法执行目标方法，即执行被拦截的方法。最终返回目标方法的执行结果。
        这段代码的作用是在方法执行前进行拦截和处理，其中包括检查用户登录状态、记录日志信息等。
     */
    @Around("loginAuth()")
    public Object loginAuthAround(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        if (checkNeedCheckLoginInfo(proceedingJoinPoint)) {
            ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = servletRequestAttributes.getRequest();
            String requestURI = request.getRequestURI();
            log.info("成功拦截到请求，URI为：{}", requestURI);
            if (!checkAndSaveUserId(request)) {
                log.warn("成功拦截到请求，URI为：{}. 检测到用户未登录，将跳转至登录页面", requestURI);
                return R.fail(ResponseCode.NEED_LOGIN);
            }

            log.info("成功拦截到请求，URI为：{}，请求通过", requestURI);
        }
        return proceedingJoinPoint.proceed();
    }

    /**
     * 1. 校验token并提取userId
     *      a、获取token 从请求头或者参数
     *      b、从缓存中获取token，进行比对
     *      c、解析token
     *      d、解析的userId存入线程上下文，供下游使用
     * 2. 去缓存中查找、比对包含userId的token，判断该用户是否已经登陆
     *
     * @param request
     * @return
     */
    /*
    参数：
        1. HttpServletRequest request
            HttpServletRequest 是 Java Servlet 中的一个接口，它提供了用于处理 HTTP 请求的方法和属性。
            具体来说，它允许您获取有关客户端请求的各种信息，例如请求的 URL、请求参数、请求头、请求方法等等。
            具体用法：
                1. request.getRequestURI()：获取请求的 URI，即请求的路径部分。
                2. request.getHeader(headerName)：获取指定请求头的值。
                3. request.getParameter(paramName)：获取指定请求参数的值。
                4. request.getMethod()：获取请求的方法，如 GET、POST 等。
    返回值：
        boolean类型值，表示是否已经登录
    执行逻辑：
        这段代码是一个检查用户登录状态并保存用户ID的方法。让我们逐步解释它：
        1. String accessToken = request.getHeader(LOGIN_AUTH_REQUEST_HEADER_NAME);
            这里LOGIN_AUTH_REQUEST_HEADER_NAME是类中自定义的字段，值为"Authorization"
            所以这句代码等同于request.getHeader("Authorization") ，是Java Servlet中获取HTTP请求头中Authorization字段的方法。
            在 HTTP 请求中，Authorization 字段通常用于携带认证信息，例如用户凭证或令牌，以便服务器验证请求的合法性和身份。
        2. if (StringUtils.isBlank(accessToken)) {
                accessToken = request.getParameter(LOGIN_AUTH_PARAM_NAME);
            }
            if (StringUtils.isBlank(accessToken)) {
                return false;
            }
            如果请求头中获取的访问令牌为空，则尝试从请求参数中获取名为 LOGIN_AUTH_PARAM_NAME 的值作为访问令牌。
            这里的LOGIN_AUTH_PARAM_NAME是类中自定义的字段，值为"authorization"
            也就是说，获取不到"Authorization"字段的值，就试一试"authorization"
            如果仍然是null，则返回false，表示用户未登录。
        3. Object userId = JwtUtil.analyzeToken(accessToken, UserConstants.LOGIN_USER_ID);
            if (Objects.isNull(userId)) {
                return false;
            }
            使用访问令牌（accessToken）和登录用户ID常量调用 JwtUtil.analyzeToken 方法解析出用户ID。
            如果解析出的用户ID为空，则返回 false，表示解析失败。
        4. Cache cache = cacheManager.getCache(CacheConstants.R_PAN_CACHE_NAME);
            String redisAccessToken = cache.get(UserConstants.USER_LOGIN_PREFIX + userId, String.class);
            if (StringUtils.isBlank(redisAccessToken)) {
                return false;
            }
            获取缓存管理器中名为 R_PAN_CACHE_NAME 的缓存对象。这里R_PAN_CACHE_NAME常量值为"R_PAN_CACHE"
            从缓存中获取以用户登录前缀加上用户ID为键的值，这个值应该是用户的访问令牌。
            如果令牌为null，则返回false，表示用户未登录或登录已过期。
        5. if (Objects.equals(accessToken, redisAccessToken)) {
                saveUserId(userId);
                return true;
            }
            如果请求中的访问令牌与缓存中的访问令牌相同，则保存用户ID，并返回 true，表示用户已登录且验证通过。
        6. 最后，如果以上条件都不符合，则返回 false，表示用户登录验证未通过。
        这段代码主要用于检查用户的登录状态，并在登录验证通过时保存用户ID。
     */
    private boolean checkAndSaveUserId(HttpServletRequest request) {
        String accessToken = request.getHeader(LOGIN_AUTH_REQUEST_HEADER_NAME);
        if (StringUtils.isBlank(accessToken)) {
            accessToken = request.getParameter(LOGIN_AUTH_PARAM_NAME);
        }
        if (StringUtils.isBlank(accessToken)) {
            return false;
        }
        Object userId = JwtUtil.analyzeToken(accessToken, UserConstants.LOGIN_USER_ID);
        if (Objects.isNull(userId)) {
            return false;
        }

        Cache cache = cacheManager.getCache(CacheConstants.R_PAN_CACHE_NAME);
        String redisAccessToken = cache.get(UserConstants.USER_LOGIN_PREFIX + userId, String.class);

        if (StringUtils.isBlank(redisAccessToken)) {
            return false;
        }

        if (Objects.equals(accessToken, redisAccessToken)) {
            saveUserId(userId);
            return true;
        }

        return false;
    }

    /**
     * 保存用户ID到线程上下文中
     *
     * @param userId
     */
    private void saveUserId(Object userId) {
        UserIdUtil.set(Long.valueOf(String.valueOf(userId)));
    }

    /**
     * 校验是否需要校验登录信息
     * 主要逻辑是校验目标方法是否被LoginIgnore注解标记，如果标记则跳过校验
     *
     * @param proceedingJoinPoint
     * @return true 需要校验登录信息 false 不需要
     */
    /*
    参数：
        1. ProceedingJoinPoint proceedingJoinPoint
            什么是ProceedingJoinPoint？
                是 Spring AOP 中的一个接口，用于表示连接点（Join Point）
                并提供了一些方法来操作连接点和目标方法。
                在 Spring AOP 中，连接点是程序执行的点，例如方法调用、异常处理等。
                ProceedingJoinPoint 是连接点的一种具体实现，它用于表示可以执行目标方法的连接点。
            ProceedingJoinPoint 接口提供了以下常用方法：
                1. Object proceed() throws Throwable：执行目标方法。
                    在环绕通知中，可以调用此方法来执行目标方法，同时可以控制目标方法的执行过程。
                2. Object proceed(Object[] args) throws Throwable：带参数的执行目标方法。
                    与上述方法类似，但可以传入方法的参数数组。
                3. Signature getSignature()：获取连接点的签名信息。
                    可以通过签名信息获取连接点所在的类、方法名等信息。
                4. Object getThis()：获取代理对象。
                    在 Spring AOP 中，连接点通常是代理对象，通过此方法可以获取代理对象的引用。
                5. Object[] getArgs()：获取方法的参数数组。
                    可以通过此方法获取目标方法的参数列表。
            ProceedingJoinPoint 接口通常在环绕通知（@Around）中使用，用于控制目标方法的执行流程，包括是否执行目标方法、如何处理目标方法的返回值等。
     执行逻辑：
        1. Signature signature = proceedingJoinPoint.getSignature();
            通过 proceedingJoinPoint 对象的 getSignature() 方法获取目标方法的签名信息。
        2. MethodSignature methodSignature = (MethodSignature) signature;
            将目标方法的签名信息转换为 MethodSignature 类型，以便获取方法的详细信息。
        3. Method method = methodSignature.getMethod();
            通过 methodSignature 对象的 getMethod() 方法获取目标方法的 Method 对象，即目标方法的具体信息。
        4. return !method.isAnnotationPresent(LoginIgnore.class);
            使用 isAnnotationPresent() 方法检查目标方法是否被 LoginIgnore 注解标记。
            如果目标方法没有被 LoginIgnore 注解标记，则返回 true，表示需要进行登录信息的校验；否则返回 false，表示不需要进行校验。
        总体来说，这段代码是一个用于检查目标方法是否需要进行登录信息校验的方法，
        它通过检查目标方法是否被 LoginIgnore 注解标记来确定是否需要进行校验。
     */
    private boolean checkNeedCheckLoginInfo(ProceedingJoinPoint proceedingJoinPoint) {
        Signature signature = proceedingJoinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();
        //判断该方法是否被LoginIgnore注解标记
        return !method.isAnnotationPresent(LoginIgnore.class);
    }


}
