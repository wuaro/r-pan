package com.wuaro.pan.server.modules.share.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wuaro.pan.server.modules.share.context.SaveShareFilesContext;
import com.wuaro.pan.server.modules.share.entity.RPanShareFile;

/**
 * @author wuaro
 * @description 针对表【r_pan_share_file(用户分享文件表)】的数据库操作Service
 * @createDate 2022-11-09 18:38:38
 */
public interface IShareFileService extends IService<RPanShareFile> {

    /**
     * 保存分享的文件的对应关系
     *
     * @param context
     */
    void saveShareFiles(SaveShareFilesContext context);

}
