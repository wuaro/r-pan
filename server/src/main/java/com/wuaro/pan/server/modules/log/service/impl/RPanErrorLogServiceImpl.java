package com.wuaro.pan.server.modules.log.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wuaro.pan.server.modules.log.entity.RPanErrorLog;
import com.wuaro.pan.server.modules.log.service.RPanErrorLogService;
import com.wuaro.pan.server.modules.log.mapper.RPanErrorLogMapper;
import org.springframework.stereotype.Service;

/**
* @author 11391
* @description 针对表【r_pan_error_log(错误日志表)】的数据库操作Service实现
* @createDate 2024-03-06 09:02:39
*/
@Service
public class RPanErrorLogServiceImpl extends ServiceImpl<RPanErrorLogMapper, RPanErrorLog>
    implements RPanErrorLogService{

}




