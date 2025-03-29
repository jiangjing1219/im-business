package com.jiangjing.im.app.bussiness.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jiangjing.im.app.bussiness.common.ResponseVO;
import com.jiangjing.im.app.bussiness.dao.UserEntity;
import com.jiangjing.im.app.bussiness.dao.mapper.UserMapper;
import com.jiangjing.im.app.bussiness.model.req.RegisterReq;
import com.jiangjing.im.app.bussiness.security.ImUserDetails;
import com.jiangjing.im.app.bussiness.service.ImService;
import com.jiangjing.im.app.bussiness.service.UserService;
import com.jiangjing.im.app.bussiness.utils.SnowflakeIdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Objects;

/**
 * @author Admin
 */
@Transactional
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, UserEntity> implements UserService, UserDetailsService, UserDetailsPasswordService {

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
        return imService.importUser(Collections.singletonList(user));
    }

    @Override
    public void updateNameById(String name) {
        UserEntity userEntity = new UserEntity();
        userEntity.setUserId("324431782084609");
        userEntity.setUserName(name);
        userMapper.updateById(userEntity);
        test();
    }

    public void test() {
        System.out.println(1 / 0);
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = this.getUserByUserName(username);;
        if (Objects.isNull(user)) {
            //说明用户名不存在
            throw new UsernameNotFoundException("账户不存在");
        }
        user.getRoles().add("admin");
        return new ImUserDetails(user);
    }

    @Transactional(propagation = Propagation.NEVER)
    @Override
    public UserDetails updatePassword(UserDetails user, String newPassword) {
        QueryWrapper<UserEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_name", user.getUsername());
        UserEntity userEntity = new UserEntity();
        userEntity.setPassword(newPassword);
        userMapper.update(userEntity, queryWrapper);
        return user;
    }
}
