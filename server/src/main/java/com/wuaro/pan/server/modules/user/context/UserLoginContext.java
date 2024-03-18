package com.wuaro.pan.server.modules.user.context;

import com.wuaro.pan.server.modules.user.entity.RPanUser;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户登录业务的上下文实体对象
 */
@Data
public class UserLoginContext implements Serializable {

    /**
     *  serialVersionUID 是 Java 中用于序列化和反序列化的一个字段，它是一个长整型数字，用于确保序列化和反序列化过程中类的版本一致性。
     *  在 Java 序列化机制中，当一个对象需要被序列化时，会将对象转换为字节流并保存到文件或网络传输中；
     *  当需要从字节流中恢复对象时，会将字节流反序列化为对象。
     *  这个过程中，如果对象的类发生了变化（比如添加了新的字段、删除了旧的字段、修改了类的结构等），就会导致反序列化失败或出现数据不一致的问题。
     *
     *  为了解决这个问题，Java 提供了 serialVersionUID 字段，
     *  它是一个静态常量，在类的序列化过程中会被序列化到字节流中，用于表示类的版本号。
     *  在反序列化时，会比较字节流中的 serialVersionUID 和当前类的 serialVersionUID，
     *  如果两者不一致，则会抛出 InvalidClassException 异常，从而防止反序列化失败或数据不一致的问题。
     *
     *  通常情况下，可以通过工具自动生成 serialVersionUID，也可以手动指定一个固定的值。
     */
    private static final long serialVersionUID = -3754570303177237029L;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 用户实体对象
     */
    private RPanUser entity;

    /**
     * 登陆成功之后的凭证信息
     */
    private String accessToken;

}
