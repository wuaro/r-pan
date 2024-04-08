package com.wuaro.pan.server.modules.file.enums;

import com.wuaro.pan.core.exception.RPanBusinessException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * 文件类型枚举类
 * 文件类型（1 普通文件 2 压缩文件 3 excel 4 word 5 pdf 6 txt 7 图片 8 音频 9 视频 10 ppt 11 源码文件 12 csv）
 */
@AllArgsConstructor
@Getter
public enum FileTypeEnum {

    /*
    1.  1：文件类型的代码，用于标识这种类型的文件。
    2.  "NORMAL_FILE"：文件类型的描述，说明这种文件类型是一般的普通文件。
    3.  1：排序字段，用于对文件类型进行排序。在这里，它的值为1，表示这种文件类型在排序时的优先级是1，即最高优先级。
    4.  fileSuffix -> true：文件类型匹配器，是一个 Predicate<String> 类型的Lambda表达式，表示无论传入的文件后缀是什么，
        都会返回true，即表示任何文件后缀都属于这种普通文件类型。
     */
    NORMAL_FILE(1, "NORMAL_FILE", 1, fileSuffix -> true),
    /*
    这段代码定义了一个名为 `ARCHIVE_FILE` 的枚举值，表示压缩文件类型。让我们逐个解析这个枚举值的含义：
    1.  `2`：文件类型的代码，用于标识这种类型的文件。
    2.  `"ARCHIVE_FILE"`：文件类型的描述，说明这种文件类型是压缩文件。
    3.  `2`：排序字段，用于对文件类型进行排序。在这里，它的值为2，表示这种文件类型在排序时的优先级是2。
    4.  `fileSuffix -> { ... }`：文件类型匹配器，是一个 `Predicate<String>` 类型的Lambda表达式，用于判断传入的文件后缀是否属于压缩文件类型。匹配器的具体逻辑如下：
       - 创建一个包含各种压缩文件后缀的列表 `matchFileSuffixes`。
       - 使用 `StringUtils.isNotBlank(fileSuffix)` 来确保传入的文件后缀不为空或空白字符。
       - 使用 `matchFileSuffixes.contains(fileSuffix)` 来判断传入的文件后缀是否在压缩文件后缀列表中，如果是则返回true，表示匹配成功，否则返回false，表示不匹配。
    因此，这个枚举值表示了一种压缩文件类型，匹配一系列常见的压缩文件后缀。
     */
    ARCHIVE_FILE(2, "ARCHIVE_FILE", 2, fileSuffix -> {
        List<String> matchFileSuffixes = Arrays.asList(".rar", ".zip", ".cab", ".iso", ".jar", ".ace", ".7z", ".tar", ".gz", ".arj", ".lah", ".uue", ".bz2", ".z", ".war");
        return StringUtils.isNotBlank(fileSuffix) && matchFileSuffixes.contains(fileSuffix);
    }),
    EXCEL_FILE(3, "EXCEL", 3, fileSuffix -> {
        List<String> matchFileSuffixes = Arrays.asList(".xlsx", ".xls");
        return StringUtils.isNotBlank(fileSuffix) && matchFileSuffixes.contains(fileSuffix);
    }),
    WORD_FILE(4, "WORD_FILE", 4, fileSuffix -> {
        List<String> matchFileSuffixes = Arrays.asList(".docx", ".doc");
        return StringUtils.isNotBlank(fileSuffix) && matchFileSuffixes.contains(fileSuffix);
    }),
    PDF_FILE(5, "PDF_FILE", 5, fileSuffix -> {
        List<String> matchFileSuffixes = Arrays.asList(".pdf");
        return StringUtils.isNotBlank(fileSuffix) && matchFileSuffixes.contains(fileSuffix);
    }),
    TXT_FILE(6, "TXT_FILE", 6, fileSuffix -> {
        List<String> matchFileSuffixes = Arrays.asList(".txt");
        return StringUtils.isNotBlank(fileSuffix) && matchFileSuffixes.contains(fileSuffix);
    }),
    IMAGE_FILE(7, "IMAGE_FILE", 7, fileSuffix -> {
        List<String> matchFileSuffixes = Arrays.asList(".bmp", ".gif", ".png", ".ico", ".eps", ".psd", ".tga", ".tiff", ".jpg", ".jpeg");
        return StringUtils.isNotBlank(fileSuffix) && matchFileSuffixes.contains(fileSuffix);
    }),
    AUDIO_FILE(8, "AUDIO_FILE", 8, fileSuffix -> {
        List<String> matchFileSuffixes = Arrays.asList(".mp3", ".mkv", ".mpg", ".rm", ".wma");
        return StringUtils.isNotBlank(fileSuffix) && matchFileSuffixes.contains(fileSuffix);
    }),
    VIDEO_FILE(9, "VIDEO_FILE", 9, fileSuffix -> {
        List<String> matchFileSuffixes = Arrays.asList(".avi", ".3gp", ".mp4", ".flv", ".rmvb", ".mov");
        return StringUtils.isNotBlank(fileSuffix) && matchFileSuffixes.contains(fileSuffix);
    }),
    POWER_POINT_FILE(10, "POWER_POINT_FILE", 10, fileSuffix -> {
        List<String> matchFileSuffixes = Arrays.asList(".ppt", ".pptx");
        return StringUtils.isNotBlank(fileSuffix) && matchFileSuffixes.contains(fileSuffix);
    }),
    SOURCE_CODE_FILE(11, "SOURCE_CODE_FILE", 11, fileSuffix -> {
        List<String> matchFileSuffixes = Arrays.asList(".java", ".obj", ".h", ".c", ".html", ".net", ".php", ".css", ".js", ".ftl", ".jsp", ".asp");
        return StringUtils.isNotBlank(fileSuffix) && matchFileSuffixes.contains(fileSuffix);
    }),
    CSV_FILE(12, "CSV_FILE", 12, fileSuffix -> {
        List<String> matchFileSuffixes = Arrays.asList(".csv");
        return StringUtils.isNotBlank(fileSuffix) && matchFileSuffixes.contains(fileSuffix);
    });

    /**
     * 文件类型的code
     */
    private Integer code;

    /**
     * 文件类型描述
     */
    private String desc;

    /**
     * 排序字段
     * 按照降序顺序排序
     */
    private Integer order;

    /**
     * 文件类型匹配器
     */
    private Predicate<String> tester;

    /**
     * 根据文件名称的后缀获取对应的文件类型映射code
     *
     * @param fileSuffix
     * @return
     */
    /*
    执行逻辑：
        这段代码是一个静态方法，用于根据文件后缀获取文件类型代码。下面是代码的功能解析：
        1. 首先，使用`Arrays.stream(values())`将`FileTypeEnum`枚举类的所有值转换为流。
        2. 使用`.sorted(Comparator.comparingInt(FileTypeEnum::getOrder).reversed())`对流进行排序，
            排序规则是按照`FileTypeEnum`枚举类中定义的`getOrder`方法返回的顺序进行排序，并使用`reversed()`方法将排序结果反转，
            以便后续从高优先级到低优先级进行匹配。
        3. 使用`.filter(value -> value.getTester().test(fileSuffix))`过滤流，
            根据给定的文件后缀通过`FileTypeEnum`枚举类中的`getTester`方法获取相应的`Predicate<String>`，
            然后使用`test`方法进行匹配过滤，保留符合条件的元素。
        4. 使用`.findFirst()`获取第一个符合条件的元素，这里使用`Optional<FileTypeEnum>`包装结果，表示可能存在也可能不存在。
        5. 如果存在符合条件的结果（即`result.isPresent()`返回true），则返回该文件类型代码（`result.get().getCode()`）。
        6. 如果不存在符合条件的结果，则抛出一个自定义的异常`RPanBusinessException`，表示获取文件类型失败。
        总体来说，这个方法的作用是根据文件后缀获取对应的文件类型代码，如果找不到匹配的文件类型，则抛出异常。
     */
    public static Integer getFileTypeCode(String fileSuffix) {
        Optional<FileTypeEnum> result = Arrays.stream(values())
                .sorted(Comparator.comparingInt(FileTypeEnum::getOrder).reversed())
                .filter(value -> value.getTester().test(fileSuffix))
                .findFirst();
        if (result.isPresent()) {
            return result.get().getCode();
        }
        throw new RPanBusinessException("获取文件类型失败");
    }

}
