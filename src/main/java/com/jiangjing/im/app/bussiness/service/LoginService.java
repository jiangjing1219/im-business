package com.jiangjing.im.app.bussiness.service;

import com.jiangjing.im.app.bussiness.common.ResponseVO;
import com.jiangjing.im.app.bussiness.model.req.LoginReq;
import com.jiangjing.im.app.bussiness.model.req.RegisterReq;

/**
 * @author Admin
 */
public interface LoginService {
    /**
     * 用户登录接口
     *
     * @param req
     * @return
     */
    ResponseVO login(LoginReq req);

    /**
     * 用户注册接口
     *
     * @param req
     * @return
     */
    ResponseVO register(RegisterReq req);
}
