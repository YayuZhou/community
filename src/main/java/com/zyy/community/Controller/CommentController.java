package com.zyy.community.Controller;

import com.zyy.community.entity.Comment;
import com.zyy.community.entity.DiscussPost;
import com.zyy.community.entity.Event;
import com.zyy.community.event.EventProducer;
import com.zyy.community.service.CommentService;
import com.zyy.community.service.DiscussPostServicec;
import com.zyy.community.util.CommunityConstant;
import com.zyy.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@Controller
@RequestMapping("/comment")
public class CommentController implements CommunityConstant {

    @Autowired
    HostHolder hostHolder;

    @Autowired
    CommentService commentService;

    @Autowired
    DiscussPostServicec discussPostServicec;
    @Autowired
    private EventProducer eventProducer;

    @RequestMapping(path="/add/{discussPostId}", method = RequestMethod.POST)
    public String addComment(@PathVariable("discussPostId") int discussPostId, Comment comment){
        comment.setUserId(hostHolder.getUser().getId());
        comment.setStatus(0);
        comment.setCreateTime(new Date());

        commentService.addComment(comment);

        //触发评论事件
        Event event = new Event()
                .setTopic(EVENT_COMMENT)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(comment.getEntityType())
                .setEntityId(comment.getEntityId())
                .setData("postId", discussPostId);

        if(comment.getEntityType() == ENTITY_TYPE_POST){
            DiscussPost target = discussPostServicec.findDiscussPostById(comment.getEntityId());
            event.setEntityUserId(target.getUserId());
        }else{
            Comment target = commentService.findCommentById(comment.getEntityId());
            event.setEntityUserId(target.getUserId());
        }

        eventProducer.fireEvent(event);

        //触发发帖事件
        if(comment.getEntityType()== ENTITY_TYPE_POST){
            event = new Event()
                    .setTopic(EVENT_ADD_DISPOST)
                    .setUserId(comment.getUserId())
                    .setEntityType(ENTITY_TYPE_POST)
                    .setEntityId(discussPostId);
            eventProducer.fireEvent(event);
        }

        return "redirect:/discuss/detail/" + discussPostId;
    }
}
