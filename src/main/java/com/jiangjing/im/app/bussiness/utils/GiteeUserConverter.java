package com.jiangjing.im.app.bussiness.utils;

import com.jiangjing.im.app.bussiness.dao.UserEntity;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

/**
 * gitee OAuth2 用户信息转换器
 */
public class GiteeUserConverter {

    public static UserEntity convert(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        UserEntity userEntity = new UserEntity();
        Map<String, Object> attributes = oAuth2User.getAttributes();
        userEntity.setUniqueId(String.valueOf(attributes.get("id")));
        userEntity.setLogin((String) attributes.get("login"));
        userEntity.setName((String) attributes.get("name"));
        userEntity.setAvatarUrl((String) attributes.get("avatar_url"));
        userEntity.setRegistrationId("gitee");
        userEntity.setUserName((String) attributes.get("remark"));
        OAuth2AccessToken accessToken = oAuth2UserRequest.getAccessToken();
        userEntity.setCredentials(accessToken.getTokenValue());

        //expiresAt默认采用isO8601标准时间格式（使用零时区）
        ZonedDateTime zonedDateTime = Objects.requireNonNull(accessToken.getExpiresAt()).atZone(ZoneId.systemDefault());
        userEntity.setCredentialsExpiresAt(Date.from(zonedDateTime.toInstant()).getTime());
        userEntity.setCreateTime(new Date().getTime());
        userEntity.setUpdateTime(new Date().getTime());
        return userEntity;
    }
}
