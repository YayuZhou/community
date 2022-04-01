package com.zyy.community.Controller;


import com.zyy.community.entity.DiscussPost;
import com.zyy.community.service.ElasticSearchService;
import com.zyy.community.service.LikeService;
import com.zyy.community.service.UserService;
import com.zyy.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import com.zyy.community.entity.Page;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Controller
public class ElasticSearchController implements CommunityConstant {

    @Autowired
    private ElasticSearchService elasticSearchService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    @RequestMapping(path = "/search", method = RequestMethod.GET)
    public String search(String keyword, Page page, Model model) throws IOException  {

        List<DiscussPost> list = elasticSearchService.searchQueryKeyWord(
                keyword, page.getCurrent()-1, page.getLimit()
        );

        //聚合数据
        List<Map<String, Object>> discussPostLists = new LinkedList<>();
        if(list != null){
            for(DiscussPost post : list){
                Map<String, Object> map = new HashMap<>();
                map.put("post", post);
                map.put("user", userService.findUserById(post.getUserId()));
                map.put("likeCount", likeService.entityLikeCount(ENTITY_TYPE_POST, post.getId()));
                discussPostLists.add(map);
            }
        }
        model.addAttribute("discussPosts", discussPostLists);
        model.addAttribute("keyword", keyword);

        //设置分页信息
        page.setPath("/search?keyword="+ keyword);
        page.setRows(list == null ? 0 : list.size());

        return "/site/search";
    }
}
