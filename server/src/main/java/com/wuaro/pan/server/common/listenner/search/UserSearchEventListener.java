package com.wuaro.pan.server.common.listenner.search;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.wuaro.pan.core.utils.IdUtil;
import com.wuaro.pan.server.modules.user.entity.RPanUserSearchHistory;
import com.wuaro.pan.server.modules.user.service.IUserSearchHistoryService;
import org.apache.ibatis.annotations.Update;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.DuplicateFormatFlagsException;

@Component
public class UserSearchEventListener {
    @Autowired
    private IUserSearchHistoryService iUserSearchHistoryService;

    /**
     * 监听用户搜索事件，将其保存到用户的搜索历史记录当中
     * @param event
     */
    @EventListener(classes = UserSearchEvent.class)
    public void saveSearchHistory(UserSearchEvent event){

        RPanUserSearchHistory record = new RPanUserSearchHistory();

        record.setId(IdUtil.get());
        record.setUserId(event.getUserId());
        record.setSearchContent(event.getKeyword());
        record.setCreateTime(new Date());
        record.setUpdateTime(new Date());

        try {
            iUserSearchHistoryService.save(record);
        }catch (DuplicateKeyException e){
            UpdateWrapper updateWrapper = Wrappers.update();
            updateWrapper.eq("user_id",event.getUserId());
            updateWrapper.eq("search_content",event.getKeyword());
            updateWrapper.set("update_time",new Date());
            iUserSearchHistoryService.update(updateWrapper);
        }

    }
}
