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
            model.addAttribute("error", "????????????????????????");
            return "/site/setting";
        }
        //?????????????????????
        fileName = CommunityUtil.generateUUID() + sufix;
        //???????????????????????????
        File dest = new File(savepath + "/" + fileName);
        //????????????
        try {
            file.transferTo(dest);
        } catch (IOException e) {
            logger.error("???????????????????????????????????????" + e.getMessage());
            throw new RuntimeException("????????????????????????????????????", e);
        }

        //?????????????????????????????????
        User user = userHolder.getUser();
        String headerUrl = domin + contextPath + "/user/header/" + fileName;
        userService.updateHeader(user.getId(), headerUrl);
        return "redirect:/index";
    }

    @RequestMapping(path="/header/{fileName}", method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response){
        fileName = savepath + "/" + fileName;
        String suffix = fileName.substring(fileName.lastIndexOf("."));

        //????????????
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
            logger.error("?????????????????????" + e.getMessage());
        }
    }

    //????????????
    @RequestMapping(path = "/updatepassword", method = RequestMethod.POST)
    public String updatePassword(String oldPassword, String newPassword, String comfirmPassword,
                                  Model model){
        //???????????????
        User user = userHolder.getUser();
        String password = user.getPassword();
        String inputpassword = CommunityUtil.md5(oldPassword + user.getSalt());
        if(!inputpassword.equals(password)){
            model.addAttribute("errorpassword","????????????");
            return "/site/setting";
        }
        if(!newPassword.equals(comfirmPassword)){
            model.addAttribute("diffpassword", "?????????????????????");
            return "/site/setting";
        }
        //????????????
        newPassword = CommunityUtil.md5(newPassword + user.getSalt());
        userService.updatePassword(user.getId(), newPassword);

        //????????????????????????
        return "redirect:/login";
    }

    //????????????
    @RequestMapping(path = "/profile/{userId}", method = RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId") int userId, Model model){
        User user = userService.findUserById(userId);
        if(user == null){
            throw new RuntimeException("??????????????????");
        }
        model.addAttribute("user", user);

        int userLikeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("userLikeCount", userLikeCount);
        //????????????
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        System.out.println(followeeCount);
        model.addAttribute("followeeCount", followeeCount);

        //????????????
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        System.out.println(followerCount);
        model.addAttribute("followerCount", followerCount);

        //????????????
        boolean hasFollowed = false;
        if(userHolder.getUser() != null){
            hasFollowed = followService.hasFollow(userHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
        }
        model.addAttribute("hasFollowed", hasFollowed);
        return "/site/profile";
    }



}
