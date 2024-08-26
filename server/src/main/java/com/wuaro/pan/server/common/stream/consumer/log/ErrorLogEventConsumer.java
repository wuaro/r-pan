package com.wuaro.pan.server.common.stream.consumer.log;
//
//import com.wuaro.pan.core.utils.IdUtil;
//import com.wuaro.pan.server.common.stream.channel.PanChannels;
//import com.wuaro.pan.server.common.stream.event.log.ErrorLogEvent;
//import com.wuaro.pan.server.modules.log.entity.RPanErrorLog;
//import com.wuaro.pan.server.modules.log.service.IErrorLogService;
//import com.wuaro.pan.stream.core.AbstractConsumer;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.cloud.stream.annotation.StreamListener;
//import org.springframework.messaging.Message;
//import org.springframework.stereotype.Component;
//
//import java.util.Date;
//
///**
// * 系统错误日志监听器
// */
//@Component
//public class ErrorLogEventConsumer extends AbstractConsumer {
//
//    @Autowired
//    private IErrorLogService iErrorLogService;
//
//    /**
//     * 监听系统错误日志事件，并保存到数据库中
//     *
//     * @param message
//     */
//    @StreamListener(PanChannels.ERROR_LOG_INPUT)
//    public void saveErrorLog(Message<ErrorLogEvent> message) {
//        if (isEmptyMessage(message)) {
//            return;
//        }
//        printLog(message);
//        ErrorLogEvent event = message.getPayload();
//        RPanErrorLog record = new RPanErrorLog();
//        record.setId(IdUtil.get());
//        record.setLogContent(event.getErrorMsg());
//        record.setLogStatus(0);
//        record.setCreateUser(event.getUserId());
//        record.setCreateTime(new Date());
//        record.setUpdateUser(event.getUserId());
//        record.setUpdateTime(new Date());
//        iErrorLogService.save(record);
//    }
//
//}
