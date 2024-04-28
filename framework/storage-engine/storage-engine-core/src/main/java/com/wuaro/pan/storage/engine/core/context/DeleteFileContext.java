package com.wuaro.pan.storage.engine.core.context;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 删除物理文件的上下文试实体信息
 */
@Data
public class DeleteFileContext implements Serializable {


    private static final long serialVersionUID = 8489120098563636345L;
    /**
     * 要删除的物理文件路径的集合
     */
    private List<String> realFilePathList;

}