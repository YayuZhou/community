package com.zyy.community.service;

import com.zyy.community.dao.MessageMapper;
import com.zyy.community.entity.Message;
import com.zyy.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class MessageService {
    @Autowired
    MessageMapper messageMapper;

    @Autowired
    SensitiveFilter sensitiveFilter;

    //獲取會話列表
    public List<Message> getMessageList(int userId, int offset, int limit){
        return messageMapper.selectConversation(userId, offset, limit);
    }

    //获取当前用户会话数量
    public int getMessage(int userId){
        return messageMapper.selectConversationCount(userId);
    }

    //获取未读私信数量
    public int getUnreadCount(int userId, String conversationId){
        return messageMapper.selectUnreadCount(userId, conversationId);
    }

    //获取某个会话包含的私信列表
    public List<Message> getMessageByConversationId(String converesationId, int offset, int limit){
        return messageMapper.selectLetters(converesationId, offset, limit);
    }

    //获取某个会话的私信数量
    public int getLetterCount(String conversationId){
        return messageMapper.selectLetterCount(conversationId);
    }

    //发送私信
    public int addMessage(Message message){
        message.setContent(HtmlUtils.htmlEscape(message.getContent()));
        message.setContent(sensitiveFilter.filter(message.getContent()));

        return messageMapper.insertMessage(message);
    }

    public int readMessage(List<Integer> ids){
        return messageMapper.updateStatus(ids, 1);
    }

    public Message findLatestNotice(int userId, String topic){
        return messageMapper.selectlatestedNotice(userId, topic);
    }

    public int getNoticeCount(int userId, String topic){
        return messageMapper.selectNoticeCount(userId, topic);
    }

    public int getUnreadNoticeCount(int userId, String topic){
        return messageMapper.selectNoticeUnreadCount(userId, topic);
    }




}
