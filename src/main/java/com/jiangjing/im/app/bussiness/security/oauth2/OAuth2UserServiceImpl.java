package com.jiangjing.im.app.bussiness.security.oauth2;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jiangjing.im.app.bussiness.dao.UserEntity;
import com.jiangjing.im.app.bussiness.dao.mapper.UserMapper;
import com.jiangjing.im.app.bussiness.service.ImService;
import com.jiangjing.im.app.bussiness.utils.GiteeUserConverter;
import com.jiangjing.im.app.bussiness.utils.SnowflakeIdWorker;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Objects;

@Service
@Transactional
public class OAuth2UserServiceImpl extends DefaultOAuth2UserService {

    @Autowired
    UserMapper userMapper;

    @Autowired
    SnowflakeIdWorker snowflakeIdWorker;

    @Autowired
    ImService imService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        // 判断并保存用户
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        if ("gitee".equals(registrationId)) {
            UserEntity convert = GiteeUserConverter.convert(userRequest, oAuth2User);
            // 查询当前数据是否存在，如果不存在直接保存，如果存在刷新 token
            QueryWrapper<UserEntity> userEntityQueryWrapper = new QueryWrapper<>();
            userEntityQueryWrapper.eq("unique_id", convert.getUniqueId());
            UserEntity userEntity = userMapper.selectOne(userEntityQueryWrapper);
            if (Objects.isNull(userEntity)) {
                if (StringUtils.isEmpty(convert.getUserName())) {
                    convert.setUserName(convert.getName());
                }
                // 设置用户id
                convert.setUserId(String.valueOf(snowflakeIdWorker.nextId()));
                // 设置默认密码
                convert.setPassword("{noop}123456");
                userMapper.insert(convert);

                // IM-SERVER 注册用户
                imService.importUser(Collections.singletonList(convert));
            } else {
                // 重新授权，更新凭证
                UserEntity update = new UserEntity();
                update.setCredentials(convert.getCredentials());
                update.setCredentialsExpiresAt(convert.getCredentialsExpiresAt());
                userMapper.update(update, userEntityQueryWrapper);
            }
            System.out.println(convert);
        }
        return oAuth2User;
    }
}
