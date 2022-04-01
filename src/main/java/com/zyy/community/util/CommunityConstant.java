package com.zyy.community.util;

public interface CommunityConstant {
    /**
     * 激活成功
     */
    int ACTIVATION_SUCCESS = 0;

    /**
     * 重复激活
     */
    int ACTIVATION_REPEAT = 1;

    /**
     * 激活失败
     */
    int ACTIVATION_FAILURE = 2;

    /**
     * 默认状态的登录凭证的超时时间
     */
    int DEFAULT_EXPIRED_SECONDS = 3600 * 12;

    /**
     * 记住状态的登录凭证超时时间
     */
    int REMEMBER_EXPIRED_SECONDS = 3600 * 24 * 100;


    /**
     * 帖子
     */
    int ENTITY_TYPE_POST = 1;

    /**
     * 回复
     */
    int ENTITY_TYPE_COMMENT = 2;

    /**
     * 用户
     */
    int ENTITY_TYPE_USER = 3;

    /**
     * 主题：点赞
     */
    String EVENT_LIKE = "like";

    /**
     * 主题：评论
     */
    String EVENT_COMMENT = "comment";

    /**
     * 主题：关注
     */
    String EVENT_FOLLOW = "follow";

    /**
     * 主题:发帖
     */
    String EVENT_ADD_DISPOST = "publish";

    /**
     * 系统用户
     */
    int SYSTEM_USER_ID = 1;

    /**
     * 普通用户
     */
    String AUTHORITY_UESER = "user";

    /**
     * 管理员
     */
    String AUTHORITY_ADMIN = "admin";

    /**
     * 版主
     */
    String AUTHORITY_MODERTY = "moderity";




}
