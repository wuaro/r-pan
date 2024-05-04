package com.wuaro.pan.storage.engine.oss;

import com.wuaro.pan.storage.engine.core.AbstractStorageEngine;
import com.wuaro.pan.storage.engine.core.context.DeleteFileContext;
import com.wuaro.pan.storage.engine.core.context.MergeFileContext;
import com.wuaro.pan.storage.engine.core.context.StoreFileChunkContext;
import com.wuaro.pan.storage.engine.core.context.StoreFileContext;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 基于阿里云OSS的文件存储引擎实现
 */
@Component
public class OSSStorageEngine extends AbstractStorageEngine {


    @Override
    protected void doStore(StoreFileContext context) throws IOException {

    }

    @Override
    protected void doDelete(DeleteFileContext context) throws IOException {

    }

    @Override
    protected void doStoreChunk(StoreFileChunkContext context) throws IOException {

    }

    @Override
    protected void doMergeFile(MergeFileContext context) throws IOException {

    }
}
