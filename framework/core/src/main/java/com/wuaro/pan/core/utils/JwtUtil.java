package com.wuaro.pan.core.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;

/**
 * Jwt工具类
 */
public class JwtUtil {

    public static final Long TWO_LONG = 2L;

    /**
     * 秘钥
     */
    private final static String JWT_PRIVATE_KEY = "0CB16040A41140E48F2F93A7BE222C46";

    /**
     * 刷新时间
     */
    private final static String RENEWAL_TIME = "RENEWAL_TIME";

    /**
     * 生成JWT（JSON Web Token）
     *
     * @param subject
     * @param claimKey
     * @param claimValue
     * @param expire
     * @return
     */
    /*
        执行逻辑：
            1. subject:
                设置JWT的主题，通常是用户的唯一标识符或其他相关信息。
            2. claim(claimKey, claimValue):
                在JWT中添加自定义的声明，可以存储任何需要在令牌中传递的信息。这里使用了一个键值对来表示声明的名称和值。
            3. claim(RENEWAL_TIME, new Date(System.currentTimeMillis() + expire / TWO_LONG)):
                添加一个名为 RENEWAL_TIME 的声明，其值是当前时间加上过期时间的一半。这个声明可能用于表示令牌的更新时间。
            4. setExpiration(new Date(System.currentTimeMillis() + expire)):
                设置JWT的过期时间，即令牌的有效期。过期时间是当前时间加上指定的过期时间长度。
            5. signWith(SignatureAlgorithm.HS256, JWT_PRIVATE_KEY):
                使用HS256算法对JWT进行签名，签名密钥为 JWT_PRIVATE_KEY。签名是为了确保JWT在传输过程中不被篡改或伪造。
            6. compact():
                将JWT转换为紧凑的字符串表示形式，以便于在网络中传输和存储。
            最终，该方法返回生成的JWT字符串，其中包含了设置的主题、声明、过期时间等信息，并经过签名保证安全性。
     */
    public static String generateToken(String subject, String claimKey, Object claimValue, Long expire) {
        String token = Jwts.builder()
                .setSubject(subject)
                .claim(claimKey, claimValue)
                .claim(RENEWAL_TIME, new Date(System.currentTimeMillis() + expire / TWO_LONG))
                .setExpiration(new Date(System.currentTimeMillis() + expire))
                .signWith(SignatureAlgorithm.HS256, JWT_PRIVATE_KEY)
                .compact();
        return token;
    }

    /**
     * 解析token
     *
     * @param token
     * @return
     */
    public static Object analyzeToken(String token, String claimKey) {
        if (StringUtils.isBlank(token)) {
            return null;
        }
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(JWT_PRIVATE_KEY)
                    .parseClaimsJws(token)
                    .getBody();
            return claims.get(claimKey);
        } catch (Exception e) {
            return null;
        }
    }

}
