package com.zyy.community.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/hello")
public class HelloController {

    @RequestMapping("/spring")
    @ResponseBody
    public String sayHello(){
        return "hello, spring boot!";
    }
}
