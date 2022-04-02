package com.zyy.community.Controller;

import com.zyy.community.entity.Message;
import com.zyy.community.entity.Page;
import com.zyy.community.entity.User;
import com.zyy.community.service.MessageService;
import com.zyy.community.service.UserService;
import com.zyy.community.util.CommunityUtil;
import com.zyy.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

@Controller
public class MessageController {

    @Autowired
    MessageService messageService;

    @Autowired
    HostHolder hostHolder;

    @Autowired
    UserService userService;

    @RequestMapping(path = "/letter/list", method = RequestMethod.GET)
    public String getLetters(Model model, Page page){
        User user = hostHolder.getUser();

        page.setLimit(5);
        page.setPath("/letter/list");
        page.setRows(messageService.getMessage(user.getId()));

        //会话列表
        List<Message> conversationList = messageService.getMessageList(
                user.getId(), page.getOffset(), page.getLimit()
        );

        List<Map<String, Object>> conversations = new ArrayList<>();
        if(conversationList != null){
            for(Message message : conversationList){
                Map<String, Object> map = new HashMap<>();
                map.put("conversation", message);
                map.put("letterCount", messageService.getLetterCount(message.getConversationId()));
                map.put("UnreadCount", messageService.getUnreadCount(user.getId(), message.getConversationId()));
                int targetId = user.getId() == message.getFromId() ? message.getToId() : message.getFromId();
                map.put("targetUser", userService.findUserById(targetId));

                conversations.add(map);
            }
        }
        model.addAttribute("conversations", conversations);

        //查询未读消息数
        int letterUnreadCount = messageService.getUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount", letterUnreadCount);

        return "/site/letter";

    }

    //私信详情
    @RequestMapping(path = "/letter/detail/{conversationId}", method = RequestMethod.GET)
    public String getletterDetail(@PathVariable("conversationId") String conversationId, Model model, Page page){
        //设置分页信息
        page.setLimit(5);
        page.setPath("/letter/detail" + conversationId);
        page.setRows(messageService.getLetterCount(conversationId));

        //私信列表
        List<Message> letterList = messageService.getMessageByConversationId(conversationId, page.getOffset(),
                page.getLimit());

        List<Map<String, Object>> letters = new ArrayList<>();
        if(letterList != null){
            for(Message message : letterList){
                Map<String, Object> map = new HashMap<>();
                map.put("letter", message);
                map.put("fromUser", userService.findUserById(message.getFromId()));
                letters.add(map);
            }
        }
        model.addAttribute("letters", letters);
        model.addAttribute("target", getTargetUser(conversationId));

        List<Integer> ids = getUnreadIds(letterList);
        if(!ids.isEmpty()){
            messageService.readMessage(ids);
        }

        return "/site/letter-detail";

    }

    public List<Integer> getUnreadIds(List<Message> letterList){
        List<Integer> ids = new ArrayList<>();
        if(letterList != null){
            for(Message letter : letterList){
                if(letter.getToId() == hostHolder.getUser().getId() && letter.getStatus() == 0){
                    ids.add(letter.getId());
                }
            }
        }
        return ids;
    }

    private User getTargetUser(String conversationId){
        String[] ids = conversationId.split("_");
        int id0 = Integer.parseInt(ids[0]);
        int id1 = Integer.parseInt(ids[1]);

        if(hostHolder.getUser().getId() != id0){
            return userService.findUserById(id0);
        }else{
            return userService.findUserById(id1);
        }
    }

    @RequestMapping(value = "/letter/add", method = RequestMethod.POST)
    @ResponseBody
    public String sendMessage(String username, String content){
        User target = userService.findUserByUsername(username);
        if(target == null){
            return CommunityUtil.getJSONString(1, "目标用户不存在");
        }
        Message message = new Message();
        message.setFromId(hostHolder.getUser().getId());
        if(hostHolder.getUser().getId() < target.getId()){
            message.setConversationId(hostHolder.getUser().getId() + "_" + target.getId());
        }else{
            message.setConversationId(target.getId() + "_" + hostHolder.getUser().getId());
        }
        message.setContent(content);
        message.setToId(target.getId());
        message.setCreateTime(new Date());
        messageService.addMessage(message);
        return CommunityUtil.getJSONString(0);

    }
}
