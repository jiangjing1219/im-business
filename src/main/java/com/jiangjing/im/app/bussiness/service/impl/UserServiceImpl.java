package com.jiangjing.im.app.bussiness.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jiangjing.im.app.bussiness.common.ResponseVO;
import com.jiangjing.im.app.bussiness.dao.UserEntity;
import com.jiangjing.im.app.bussiness.dao.mapper.UserMapper;
import com.jiangjing.im.app.bussiness.model.req.RegisterReq;
import com.jiangjing.im.app.bussiness.service.ImService;
import com.jiangjing.im.app.bussiness.service.UserService;
import com.jiangjing.im.app.bussiness.utils.SnowflakeIdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;

/**
 * @author Admin
 */
@Transactional
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserMapper userMapper;

    @Autowired
    ImService imService;

    @Autowired
    SnowflakeIdWorker snowflakeIdWorker;

    /**
     * 根据用户名和密码查询用户信息
     *
     * @param userName
     * @return
     */
    @Override
    public UserEntity getUserByUserName(String userName) {
        QueryWrapper<UserEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_name", userName);
        return userMapper.selectOne(queryWrapper);
    }

    /**
     * 用户注册
     *
     * @param req
     * @return
     */
    @Override
    public ResponseVO<UserEntity> registerUser(RegisterReq req) {
        // 1、业务端插入新用户信息
        UserEntity user = new UserEntity();
        user.setCreateTime(System.currentTimeMillis());
        user.setPassword(req.getPassword());
        user.setUserName(req.getUserName());
        user.setUserId(String.valueOf(snowflakeIdWorker.nextId()));
        userMapper.insert(user);
        // 2、调用 Im 服务
        ResponseVO responseVO = imService.importUser(Collections.singletonList(user));
        return responseVO;
    }
}
