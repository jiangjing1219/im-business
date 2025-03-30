package com.jiangjing.im.app.bussiness.dao;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * @author: Chackylee
 * @description:
 **/
@Data
@TableName("app_user")
public class UserEntity {

    @TableId
    private String userId;
    //用户名
    private String userName;
    //密码
    private String password;
    //手机号
    private String mobile;
    //创建时间
    private Long createTime;
    //更新时间
    private Long updateTime;

    @TableField(exist = false)
    private Set<String> roles = new HashSet<>();

    // 三方登录id
    private String uniqueId;
    // 三方登录账号
    private String login;
    // 三方登录昵称
    private String name;
    // 三方登录头像
    private String avatarUrl;
    // 三方登录凭证
    private String credentials;
    // 三方登录凭证过期时间
    private Long credentialsExpiresAt;
    // 三方登录注册渠道
    private String registrationId;

}
