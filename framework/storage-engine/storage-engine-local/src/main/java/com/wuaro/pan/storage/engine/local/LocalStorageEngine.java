package com.wuaro.pan.storage.engine.local;

import com.wuaro.pan.core.utils.FileUtils;
import com.wuaro.pan.storage.engine.core.AbstractStorageEngine;
import com.wuaro.pan.storage.engine.core.context.DeleteFileContext;
import com.wuaro.pan.storage.engine.core.context.StoreFileChunkContext;
import com.wuaro.pan.storage.engine.core.context.StoreFileContext;
import com.wuaro.pan.storage.engine.local.config.LocalStorageEngineConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

/**
 * 本地文件存储引擎实现类
 */
@Component
public class LocalStorageEngine extends AbstractStorageEngine {

    @Autowired
    private LocalStorageEngineConfig config;
    /**
     * 执行保存物理文件的动作
     *
     * @param context
     */
    /*
    执行逻辑：
        1. 获取基础路径
        2. 生成文件的存储路径
        3. 存储文件（零拷贝）
        4. 将文件的存储路径存入StoreFileContext对象中
     */
    @Override
    protected void doStore(StoreFileContext context) throws IOException {
        String basePath = config.getRootFilePath();
        String realFilePath = FileUtils.generateStoreFileRealPath(basePath, context.getFilename());
        FileUtils.writeStream2File(context.getInputStream(), new File(realFilePath), context.getTotalSize());
        context.setRealPath(realFilePath);
    }

    /**
     * 执行删除物理文件的动作
     *
     * @param context
     * @throws IOException
     */
    @Override
    protected void doDelete(DeleteFileContext context) throws IOException {
        FileUtils.deleteFiles(context.getRealFilePathList());
    }

    /**
     * 执行保存文件分片
     * 下沉到底层去实现
     *
     * @param context
     * @throws IOException
     */
    @Override
    protected void doStoreChunk(StoreFileChunkContext context) throws IOException {
        String basePath = config.getRootFileChunkPath();
        String realFilePath = FileUtils.generateStoreFileChunkRealPath(basePath, context.getIdentifier(), context.getChunkNumber());
        FileUtils.writeStream2File(context.getInputStream(), new File(realFilePath), context.getTotalSize());
        context.setRealPath(realFilePath);
    }
}
