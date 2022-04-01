package com.zyy.community.util;

public class RedisUtil {

    private static final String PREFIX = "like:entity";
    private static final String PREFIX_USER_LIKE =  "like:user";
    private static final String PREFIX_FOLLOWEE = "followee:entity";
    private static final String PREFIX_FOLLOWER = "follower:entity";

    private static final String PREFIX_KAPTCHAR = "kaptcha";
    private static final String PREFIX_TICKET = "ticket";
    private static final String PREFIX_USER = "user";

    private static final String SPLIT = ":";

    public static String getEntityLikeKey(int entityType, int entityId){
        return PREFIX + SPLIT + entityType + SPLIT + entityId;
    }

    public static String getUserLikeKey(int userId){
        return PREFIX_USER_LIKE + SPLIT + userId;
    }

    //某个用户关注的实体
    //followee: userId:entityType-> zset(entityId, now)
    public static String getFolloweeKey(int userId, int entityType){
        return PREFIX_FOLLOWEE + SPLIT + userId + SPLIT + entityType;
    }

    //某个实体拥有的粉丝
    //follower:entityType:entityId->zset(userId, now)
    public static String getFollowerKey(int entityType, int entityId){
        return PREFIX_FOLLOWER + SPLIT + entityType + SPLIT + entityId;
    }

    //登录验证码
    public static String getKaptchakey(String owner) {
        return PREFIX_KAPTCHAR + SPLIT + owner;
    }

    //获取登录凭证
    public static String getTicketkey(String ticket){
        return PREFIX_TICKET + SPLIT + ticket;
    }

    public static String getUserKey(int userId){
        return PREFIX_USER + SPLIT + userId;
    }




}
