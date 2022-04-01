package com.zyy.community.util;


import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

public class CommunityUtil {

    //生成随机字符串
    public static String generateUUID(){
        return UUID.randomUUID().toString().replaceAll("-","");
    }

    //MD5加密
    public static String md5(String key){
        if(StringUtils.isBlank(key)){
            return null;
        }
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }

    //封装服务器返回结果
    public static String getJSONString(int code, String meg, Map<String, Object> data){
        JSONObject json = new JSONObject();
        json.put("code", code);
        json.put("msg", meg);
        if(data != null){
            for(String key: data.keySet()){
                json.put(key, data.get(key));
            }
        }
        return json.toString();
    }

    public static String getJSONString(int code, String msg){
        return getJSONString(code, msg, null);
    }

    public  static String getJSONString(int code){
        return getJSONString(code, null, null);
    }
}
