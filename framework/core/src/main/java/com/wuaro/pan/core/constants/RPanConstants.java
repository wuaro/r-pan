package com.wuaro.pan.core.constants;

import org.apache.commons.lang3.StringUtils;

/**
 * RPan公共基础常量类
 */
public interface RPanConstants {

    /**
     * 公用分隔符
     */
    String COMMON_SEPARATOR = "__,__";

    /**
     * 公用空串
     */
    String EMPTY_STR = StringUtils.EMPTY;

    /**
     * 公用点字符
     */
    String POINT_STR = ".";

    /**
     * 公用斜杠字符
     */
    String SLASH_STR = "/";

    /**
     * 公用0L
     */
    long ZERO_LONG = 0L;

    /**
     * 公用0数字
     */
    int ZERO_INT = 0;

    /**
     * 公用1数字
     */
    int ONE_INT = 1;

    /**
     * 公用2数字
     */
    int TWO_INT = 2;

    /**
     * 公用-1数字
     */
    int MINUS_ONE_INT = -1;

    /**
     * 公用true字符串
     */
    String TRUE_STR = "true";

    /**
     * 公用false字符串
     */
    String FALSE_STR = "false";

    /**
     * 组件扫描的基础路径
     */
    String BASE_COMPONENT_SCAN_PATH = "com.wuaro.pan";

    /**
     * 问号常量
     */
    String QUESTION_MARK_STR = "?";

    /**
     * 等号常量
     */
    String EQUALS_MARK_STR = "=";

    /**
     * 逻辑与常量
     */
    String AND_MARK_STR = "&";

    /**
     * 左中括号常量
     */
    String LEFT_BRACKET_STR = "[";

    /**
     * 右中括号常量
     */
    String RIGHT_BRACKET_STR = "]";

    /**
     * 公用加密字符串
     */
    String COMMON_ENCRYPT_STR = "****";

}
