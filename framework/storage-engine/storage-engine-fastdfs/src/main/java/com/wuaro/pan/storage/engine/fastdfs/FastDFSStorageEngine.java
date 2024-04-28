package com.wuaro.pan.storage.engine.fastdfs;

import com.wuaro.pan.storage.engine.core.AbstractStorageEngine;
import com.wuaro.pan.storage.engine.core.context.DeleteFileContext;
import com.wuaro.pan.storage.engine.core.context.StoreFileContext;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 基于FastDFS实现的文件存储引擎
 */
@Component
public class FastDFSStorageEngine extends AbstractStorageEngine {


    @Override
    protected void doStore(StoreFileContext context) throws IOException {

    }

    @Override
    protected void doDelete(DeleteFileContext context) throws IOException {

    }
}
