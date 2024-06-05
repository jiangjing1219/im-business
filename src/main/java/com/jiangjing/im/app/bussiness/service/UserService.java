package com.jiangjing.im.app.bussiness.service;

import com.jiangjing.im.app.bussiness.common.ResponseVO;
import com.jiangjing.im.app.bussiness.dao.UserEntity;
import com.jiangjing.im.app.bussiness.model.req.RegisterReq;

/**
 * @author Admin
 */
public interface UserService {
    /**
     * 根据用户名和密码查询用户
     *
     * @param userName
     * @param password
     * @return
     */
    UserEntity getUserByUserName(String userName);

    /**
     * 用户注册
     *
     * @param req
     * @return
     */
    ResponseVO<UserEntity> registerUser(RegisterReq req);


    void updateNameById(String name);
}
