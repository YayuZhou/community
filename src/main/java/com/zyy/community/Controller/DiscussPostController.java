package com.zyy.community.Controller;

import com.zyy.community.dao.DiscussRepository;
import com.zyy.community.entity.*;
import com.zyy.community.event.EventProducer;
import com.zyy.community.service.CommentService;
import com.zyy.community.service.DiscussPostServicec;
import com.zyy.community.service.LikeService;
import com.zyy.community.service.UserService;
import com.zyy.community.util.CommunityConstant;
import com.zyy.community.util.CommunityUtil;
import com.zyy.community.util.HostHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {
    private static final Logger logger = LoggerFactory.getLogger(DiscussPostController.class);

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private DiscussPostServicec discussPostServicec;

    @Autowired
    private CommentService  commentService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private DiscussRepository discussRepository;

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(String title, String content){
        User user = hostHolder.getUser();
        if(user == null){
            return CommunityUtil.getJSONString(403, "您还没有登录");
        }

        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle(title);
        post.setContent(content);
        post.setCreateTime(new Date());
        discussPostServicec.addDiscussPost(post);

        //触发发帖事件
        Event event = new Event()
                .setTopic(EVENT_ADD_DISPOST)
                .setUserId(user.getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(post.getId());
        eventProducer.fireEvent(event);

        // TODO: 2022/3/9 报错的情况以后统一处理
        return CommunityUtil.getJSONString(0, "发布成功!");
    }

    @RequestMapping(value="/detail/{discussPostId}", method = RequestMethod.GET)
    public String getDiscussPostDetail(@PathVariable("discussPostId") int discussPostId,
                                       Page page,Model model){
        DiscussPost discussPost = discussPostServicec.findDiscussPostById(discussPostId);
        model.addAttribute("post", discussPost);
        User user = userService.findUserById(discussPost.getUserId());
        model.addAttribute("user", user);

        //点赞数量
        long likeCount = likeService.entityLikeCount(ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("likeCount", likeCount);

        //点赞状态
        int likestatus = hostHolder.getUser() == null ? 0 :
                likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("likeSatus", likestatus);

        //设置分页信息
        page.setLimit(5);
        page.setPath("/discuss/detail/" + discussPostId);
        page.setRows(discussPost.getCommentCount());

        //获取评论
        List<Comment> commentList = commentService.findCommentByEnitty(ENTITY_TYPE_POST, discussPostId,
                page.getOffset(), page.getLimit());
        //评论列表
       List<HashMap<String, Object >> commentVoList = new ArrayList<>();
        if(commentList != null){
            for(Comment comment :commentList){
                HashMap<String, Object> commentVo = new HashMap<>();
                commentVo.put("comment", comment);
                commentVo.put("user", userService.findUserById(comment.getUserId()));

                //评论赞数
                likeCount = likeService.entityLikeCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeCount", likeCount);

                //点赞状态
                likestatus = hostHolder.getUser() == null ? 0 :
                        likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likestatus", likestatus);

                //回复列表
                List<Comment> replayList = commentService.findCommentByEnitty(ENTITY_TYPE_COMMENT, comment.getId(),
                        0, Integer.MAX_VALUE);

                List<Map<String, Object>> replayVolist = new ArrayList<>();

                for(Comment replay : replayList){
                    HashMap<String, Object> replayVo = new HashMap<>();
                    //回复
                    replayVo.put("replay", replay);
                    //作者
                    replayVo.put("user", userService.findUserById(replay.getUserId()));

                    //回复目标
                    User target= replay.getTargetId() == 0 ? null :  userService.findUserById(replay.getTargetId());
                    replayVo.put("target", target);

                    //回复点赞数量
                    likeCount = likeService.entityLikeCount(ENTITY_TYPE_COMMENT, replay.getId());
                    replayVo.put("likeCount", likeCount);

                    likestatus = hostHolder.getUser() == null? 0:
                            likeService.findEntityLikeStatus(hostHolder.getUser().getId(),ENTITY_TYPE_COMMENT, replay.getId());
                    replayVo.put("likestatus", likestatus);


                    replayVolist.add(replayVo);
                }
                commentVo.put("replays", replayVolist);
                //回复数量
                int replayCount = commentService.findCommentCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("replayCount", replayCount);
                commentVoList.add(commentVo);
            }
        }
        model.addAttribute("comments", commentVoList);
        return "/site/discuss-detail";

    }
}
