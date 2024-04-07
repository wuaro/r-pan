package com.wuaro.pan.server.common.event.file;//package com.imooc.pan.server.common.event.file;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.context.ApplicationEvent;

import java.util.List;

/**
 * 文件删除事件
 */
/*
注解：
    1.  @Getter
        @Setter
        自动提供get set方法
    2.  @EqualsAndHashCode
        @EqualsAndHashCode 注解在类级别表示自动生成 equals 和 hashCode 方法，它会根据类中所有非静态和非瞬态的属性生成 equals 和 hashCode 方法。
    3.  @ToString
        @ToString 注解在类级别表示自动生成 toString 方法，它会生成包含类中所有属性的字符串表示形式。
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class DeleteFileEvent extends ApplicationEvent {

    private List<Long> fileIdList;

    /*
    Object source 表示事件源，即触发该事件的对象。
    List<Long> fileIdList 表示文件 ID 列表，用于在事件中传递文件删除的相关信息。
     */
    public DeleteFileEvent(Object source, List<Long> fileIdList) {
        super(source);
        this.fileIdList = fileIdList;
    }

}
