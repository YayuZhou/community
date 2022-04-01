package com.zyy.community.Controller;

import com.zyy.community.entity.DiscussPost;
import com.zyy.community.entity.Page;
import com.zyy.community.entity.User;
import com.zyy.community.service.DiscussPostServicec;
import com.zyy.community.service.LikeService;
import com.zyy.community.service.UserService;
import com.zyy.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController implements CommunityConstant {

    @Autowired
    private DiscussPostServicec discussPostServicec;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    @RequestMapping(path = "/index", method = RequestMethod.GET)
    public String index(Model model, Page page){
        page.setRows(discussPostServicec.findDiscussPostsRows(0));
        page.setPath("/index");

        List<DiscussPost> list = discussPostServicec.findDiscussPosts(0, page.getOffset(), page.getLimit());
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if(list != null){
            for(DiscussPost post: list){
                Map<String, Object> map = new HashMap<>();
                map.put("post", post);
                User user = userService.findUserById(post.getUserId());
                map.put("user", user);
                long likeCount = likeService.entityLikeCount(ENTITY_TYPE_POST, post.getId());
                map.put("likeCount", likeCount);
                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts", discussPosts);
        return "/index";
    }

    @RequestMapping(path = "error", method = RequestMethod.GET)
    public String getErrorPage(){
        return "/error/500";
    }

}
