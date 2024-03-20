package com.wuaro.pan.core.utils;

/**
 * 密码工具类
 */
public class PasswordUtil {

    /**
     * 随机生成盐值
     * 盐值用于password加密
     *
     * @return
     */
    public static String getSalt() {
        return MessageDigestUtil.md5(UUIDUtil.getUUID());
    }

    /**
     * 密码加密
     * 是一个简单的密码加密方法，采用了盐值和多次哈希加密来增强密码的安全性
     * 过程：password经过SHA-1哈希加密，将SHA-1哈希值与salt值拼接，并再次进行SHA-256哈希加密
     *
     * @param salt
     * @param inputPassword
     * @return
     */
    /*
        参数：
            1. String salt
                随机生成的盐值
            2. String inputPassword
                输入的密码
        执行逻辑：
            1. MessageDigestUtil.sha1(inputPassword):
                首先对用户输入的密码进行SHA-1哈希加密。SHA-1（安全哈希算法 1）是一种单向哈希函数，将输入转换为固定长度的哈希值。
            2. + salt:
                盐值与SHA-1哈希值拼接，增加了密码的熵，使得密码更加复杂和难以被破解。
            3. MessageDigestUtil.sha256(...):
                然后将上一步得到的SHA-1哈希值与盐值拼接，并再次进行SHA-256哈希加密。
                SHA-256是SHA-1的升级版，提供更高的安全性和哈希值长度。
                返回加密后的密码字符串。
            需要注意的是，虽然这种方法增加了密码的安全性，但是在实际应用中，密码的存储和加密要考虑更多因素，
            比如使用更强大的哈希算法（如SHA-512）、加盐长度、盐值生成方法等。
            此外，密码的传输也需要考虑使用安全的通信协议（如HTTPS）来保护密码的安全。
     */
    public static String encryptPassword(String salt, String inputPassword) {
        return MessageDigestUtil.sha256(MessageDigestUtil.sha1(inputPassword) + salt);
    }

}
