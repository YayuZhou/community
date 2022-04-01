package com.zyy.community;

import com.zyy.community.dao.DiscussPostMapper;
import com.zyy.community.dao.MessageMapper;
import com.zyy.community.dao.UserMapper;
import com.zyy.community.entity.DiscussPost;
import com.zyy.community.entity.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MapperTests {

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MessageMapper messageMapper;

    @Test
    public void selectDiscussPostsTest(){
        List<DiscussPost> list= discussPostMapper.selectDiscussPosts(101,0, 10);
        for(DiscussPost dis : list){
            System.out.println(dis);
        }

        int count = discussPostMapper.selectDiscussPostsRows(101);
        System.out.println(count);

    }

    @Test
    public void selectByIdTest(){
        User user = userMapper.selectById(12);
        System.out.println(user);
    }

    @Test
    public void selectLetterCountTest(){
        int userId = 111;
        int count = messageMapper.selectConversationCount(userId);
        System.out.println(count);
    }



}
