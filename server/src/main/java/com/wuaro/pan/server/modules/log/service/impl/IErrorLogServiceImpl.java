package com.wuaro.pan.server.modules.log.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wuaro.pan.server.modules.log.entity.RPanErrorLog;
import com.wuaro.pan.server.modules.log.mapper.RPanErrorLogMapper;
import com.wuaro.pan.server.modules.log.service.IErrorLogService;
import org.springframework.stereotype.Service;

/**
 * @author wuaro
 * @description 针对表【r_pan_error_log(错误日志表)】的数据库操作Service实现
 * @createDate 2022-11-09 18:37:48
 */
@Service
public class IErrorLogServiceImpl extends ServiceImpl<RPanErrorLogMapper, RPanErrorLog>
        implements IErrorLogService {

}




