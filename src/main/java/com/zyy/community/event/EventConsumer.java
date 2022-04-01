package com.zyy.community.event;

import com.alibaba.fastjson.JSONObject;
import com.zyy.community.entity.DiscussPost;
import com.zyy.community.entity.Event;
import com.zyy.community.entity.Message;
import com.zyy.community.service.DiscussPostServicec;
import com.zyy.community.service.ElasticSearchService;
import com.zyy.community.service.MessageService;
import com.zyy.community.util.CommunityConstant;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class EventConsumer implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    @Autowired
    private MessageService messageService;

    @Autowired
    private DiscussPostServicec discussPostServicec;

    @Autowired
    private ElasticSearchService elasticSearchService;

    @KafkaListener(topics = {EVENT_LIKE, EVENT_COMMENT, EVENT_FOLLOW})
    public void handleMessage(ConsumerRecord record){
        if(record == null || record.value()== null){
            logger.error("消息的内容为空");
            return;
        }
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);

        if(event == null){
            logger.error("消息格式错误");
            return;
        }

        //发站内通知
        Message  message = new Message();
        message.setFromId(SYSTEM_USER_ID);
        message.setToId(event.getEntityUserId());
        message.setConversationId(event.getTopic());
        message.setCreateTime(new Date());

        Map<String, Object> content = new HashMap<>();
        content.put("userId", event.getUserId());
        content.put("entityType", event.getEntityType());
        content.put("entityId", event.getEntityId());

        if(!event.getData().isEmpty()){
            for(Map.Entry<String, Object> entry : content.entrySet()){
                content.put(entry.getKey(), entry.getValue());
            }
        }
        message.setContent(JSONObject.toJSONString(content));
        messageService.addMessage(message);
    }

    @KafkaListener(topics = {EVENT_ADD_DISPOST})
    public void handPublishMessage(ConsumerRecord record){
        if(record == null || record.value()== null){
            logger.error("消息的内容为空");
            return;
        }
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);

        if(event == null){
            logger.error("消息格式错误");
            return;
        }
        DiscussPost discussPost = discussPostServicec.findDiscussPostById(event.getEntityId());
        elasticSearchService.save(discussPost);
    }
}
