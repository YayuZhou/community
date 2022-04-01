package com.zyy.community.dao;

import com.zyy.community.entity.Message;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MessageMapper {
    //查詢當前用戶的繪畫列表
    List<Message>  selectConversation(int userId, int offset, int limit);
    //查詢當前用戶的會話數量
    int selectConversationCount(int userId);

    //查詢某個會話所包含的私信列表
    List<Message> selectLetters(String conversationId, int offset, int limit);

    //查詢某個會話包含的私信數量
    int selectLetterCount(String conversationId);

    //查詢未讀私信的數量
    int selectUnreadCount(int userId, String conversationId);

    int insertMessage(Message message);

    int updateStatus(List<Integer> ids, int status);

    //查询某个主题下的最新通知
    Message selectlatestedNotice(int userId, String topic);

    //查询某个主题下包含的通知数量
    int selectNoticeCount(int userId, String topic);

    //查询某个主题下的未读通知数量
    int selectNoticeUnreadCount(int userId, String topic);
}
