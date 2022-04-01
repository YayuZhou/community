package com.zyy.community.Controller;

import com.zyy.community.entity.User;
import com.zyy.community.service.FollowService;
import com.zyy.community.service.LikeService;
import com.zyy.community.service.UserService;
import com.zyy.community.util.CommunityConstant;
import com.zyy.community.util.CommunityUtil;
import com.zyy.community.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;

@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    @Autowired
    UserService userService;

    @Value("${community.path.domain}")
    String domin;

    @Value("${community.path.upload}")
    String savepath;

    @Value("${server.servlet.context-path}")
    String contextPath;

    @Autowired
    HostHolder userHolder;

    @Autowired
    LikeService likeService;

    @Autowired
    FollowService followService;

    @RequestMapping(path = "/setting", method = RequestMethod.GET)
    public String getSetting(){
        return "/site/setting";
    }

    @RequestMapping(path="/upload", method = RequestMethod.POST)
    public String uploadHeader(MultipartFile file, Model model){
        //
        String fileName = file.getOriginalFilename();
        String sufix = fileName.substring(fileName.lastIndexOf('.'));
        if(Strings.isBlank(sufix)){
            model.addAttribute("error", "文件的格式不正确");
            return "/site/setting";
        }
        //生成随机文件名
        fileName = CommunityUtil.generateUUID() + sufix;
        //确定文件存放的路径
        File dest = new File(savepath + "/" + fileName);
        //存储图片
        try {
            file.transferTo(dest);
        } catch (IOException e) {
            logger.error("上传图片失败，服务器异常：" + e.getMessage());
            throw new RuntimeException("上传文件失败，服务器异常", e);
        }

        //更新当前用户的图片路径
        User user = userHolder.getUser();
        String headerUrl = domin + contextPath + "/user/header/" + fileName;
        userService.updateHeader(user.getId(), headerUrl);
        return "redirect:/index";
    }

    @RequestMapping(path="/header/{fileName}", method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response){
        fileName = savepath + "/" + fileName;
        String suffix = fileName.substring(fileName.lastIndexOf("."));

        //响应图片
        response.setContentType("image/" + suffix);
        try (
                FileInputStream fileInputStream = new FileInputStream(fileName);
                OutputStream os = response.getOutputStream();
                ){
            byte[] buffer = new byte[1024];
            int index = 0;
            while((index = fileInputStream.read(buffer)) != -1){
                os.write(buffer, 0, index);
            }

        } catch (IOException e){
            logger.error("读取头像失败：" + e.getMessage());
        }
    }

    //修改密码
    @RequestMapping(path = "/updatepassword", method = RequestMethod.POST)
    public String updatePassword(String oldPassword, String newPassword, String comfirmPassword,
                                  Model model){
        //验证旧密码
        User user = userHolder.getUser();
        String password = user.getPassword();
        String inputpassword = CommunityUtil.md5(oldPassword + user.getSalt());
        if(!inputpassword.equals(password)){
            model.addAttribute("errorpassword","密码错误");
            return "/site/setting";
        }
        if(!newPassword.equals(comfirmPassword)){
            model.addAttribute("diffpassword", "两次密码不一致");
            return "/site/setting";
        }
        //更新密码
        newPassword = CommunityUtil.md5(newPassword + user.getSalt());
        userService.updatePassword(user.getId(), newPassword);

        //重定向到登录界面
        return "redirect:/login";
    }

    //个人主页
    @RequestMapping(path = "/profile/{userId}", method = RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId") int userId, Model model){
        User user = userService.findUserById(userId);
        if(user == null){
            throw new RuntimeException("该用户不存在");
        }
        model.addAttribute("user", user);

        int userLikeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("userLikeCount", userLikeCount);
        //关注数量
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        System.out.println(followeeCount);
        model.addAttribute("followeeCount", followeeCount);

        //粉丝数量
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        System.out.println(followerCount);
        model.addAttribute("followerCount", followerCount);

        //是否关注
        boolean hasFollowed = false;
        if(userHolder.getUser() != null){
            hasFollowed = followService.hasFollow(userHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
        }
        model.addAttribute("hasFollowed", hasFollowed);
        return "/site/profile";
    }



}
