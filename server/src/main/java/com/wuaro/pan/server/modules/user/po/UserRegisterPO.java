package com.wuaro.pan.server.modules.user.po;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * 注册用户参数实体对象
 *
 * 注解：
 *      1. @Data
 *          自动提供get、set、toString等方法
 *      2. @ApiModel(value = "用户注册参数")
 *          是 Swagger API 文档生成工具中的注解，用于描述一个 Java 类或接口在 Swagger 文档中的模型信息。
 *          value = "用户注册参数" 表示该模型的名称或描述信息，用于在 Swagger 文档中标识这个模型。
 *          注解@ApiModel 通常与 @ApiModelProperty 注解一起使用，用于描述 API 接口的请求参数或响应数据的结构和字段信息。
 */
@Data
@ApiModel(value = "用户注册参数")
public class UserRegisterPO implements Serializable {

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
    private static final long serialVersionUID = -5521427813609988931L;

    /**
     * 注解：
     *      1. @ApiModelProperty(value = "用户名", required = true)
     *          是一个用于 Swagger 文档生成的注解，通常用于描述 API 接口中的参数或返回值的相关信息。
     *          在这个注解中，value = "用户名" 表示该参数或返回值的描述信息，用于说明该属性的作用或含义。
     *          required = true 则表示该属性是必填的，即在调用该 API 接口时必须提供该参数或返回值。
     *      2. @NotBlank(message = "用户名不能为空")
     *          是 Java Bean Validation（JSR 380）中的注解，用于验证属性值是否符合指定的规则。
     *          表示要求属性值不能为空格，并且不能为空，
     *          如果属性值为 null、空字符串或只包含空格的字符串，则验证不通过，同时会返回指定的错误消息 "用户名不能为空"。
     *      3. @Pattern(regexp = "^[0-9A-Za-z]{6,16}$", message = "请输入6-16位只包含数字和字母的用户名")
     *          表示要求属性值必须匹配指定的正则表达式，如果匹配成功则验证通过，否则验证不通过，
     *          同时会返回指定的错误消息 "请输入6-16位只包含数字和字母的用户名"。
     */
    @ApiModelProperty(value = "用户名", required = true)
    @NotBlank(message = "用户名不能为空")
    @Pattern(regexp = "^[0-9A-Za-z]{6,16}$", message = "请输入6-16位只包含数字和字母的用户名")
    private String username;

    /**
     * 注解：
     *      1. @ApiModelProperty(value = "密码", required = true)
     *          同上
     *      2. @NotBlank(message = "密码不能为空")
     *          同上
     *      3. @Length(min = 8, max = 16, message = "请输入8-16位的密码")
     *          是 Java Bean Validation（JSR 380）中的注解，用于验证字符串属性的长度是否在指定的范围内。
     *          min = 8 表示要求字符串的最小长度为 8。
     *          max = 16 表示要求字符串的最大长度为 16。
     *          message = "请输入8-16位的密码" 表示如果验证失败，将返回指定的错误消息。
     */
    @ApiModelProperty(value = "密码", required = true)
    @NotBlank(message = "密码不能为空")
    @Length(min = 8, max = 16, message = "请输入8-16位的密码")
    private String password;

    /**
     * 注解：
     *      1. @ApiModelProperty(value = "密码", required = true)
     *          同上
     *      2. @NotBlank(message = "密码不能为空")
     *          同上
     *      3. @Length(min = 8, max = 16, message = "请输入8-16位的密码")
     *          同上
     */
    @ApiModelProperty(value = "密码问题", required = true)
    @NotBlank(message = "密保问题不能为空")
    @Length(max = 100, message = "密保问题不能超过100个字符")
    private String question;

    /**
     * 注解：
     *      1. @ApiModelProperty(value = "密码", required = true)
     *          同上
     *      2. @NotBlank(message = "密码不能为空")
     *          同上
     *      3. @Length(min = 8, max = 16, message = "请输入8-16位的密码")
     *          同上
     */
    @ApiModelProperty(value = "密码答案", required = true)
    @NotBlank(message = "密保答案不能为空")
    @Length(max = 100, message = "密保答案不能超过100个字符")
    private String answer;

}
