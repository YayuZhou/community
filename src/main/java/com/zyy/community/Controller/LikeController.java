package com.zyy.community.Controller;


import com.zyy.community.entity.Event;
import com.zyy.community.entity.User;
import com.zyy.community.event.EventProducer;
import com.zyy.community.service.LikeService;
import com.zyy.community.util.CommunityConstant;
import com.zyy.community.util.CommunityUtil;
import com.zyy.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class LikeController implements CommunityConstant {

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;

    @Autowired
    private EventProducer eventProducer;

    @RequestMapping(path = "/like", method = RequestMethod.POST)
    @ResponseBody
    public String like(int entityType, int entityId, int entityUserId, int postId){

        User user = hostHolder.getUser();
        //点赞
        likeService.like(user.getId(), entityId, entityType, entityUserId);

        //数量
        long likeEntityCount = likeService.entityLikeCount(entityType, entityId);

        int status = likeService.findEntityLikeStatus(user.getId(), entityType, entityId);

        Map<String, Object> map =new HashMap<>();
        map.put("likeCount", likeEntityCount);
        map.put("likestatus", status);

        if(status == 1){
            Event event = new Event().setUserId(hostHolder.getUser().getId())
                    .setTopic(EVENT_LIKE)
                    .setEntityType(entityType)
                    .setEntityId(entityId)
                    .setEntityUserId(entityUserId)
                    .setData("postId", postId);
            eventProducer.fireEvent(event);
        }
        return CommunityUtil.getJSONString(0, null, map);
    }

}
