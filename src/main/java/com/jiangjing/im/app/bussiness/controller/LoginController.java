package com.jiangjing.im.app.bussiness.controller;

import com.jiangjing.im.app.bussiness.common.ResponseVO;
import com.jiangjing.im.app.bussiness.model.req.LoginReq;
import com.jiangjing.im.app.bussiness.model.req.RegisterReq;
import com.jiangjing.im.app.bussiness.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 在该业务服务实现注册和登录
 *
 * @author
 */
@RestController
@RequestMapping("v1")
public class LoginController {

    @Autowired
    LoginService loginService;

    /**
     * app 业务系统的登录接口，最重要的就是获取
     * 1、 im Server 的 UserSig （鉴权票据）, 使用和Im系统一样的规则生成鉴权票据
     * 2、自身的业务系统 token
     *
     * @param req
     * @return
     */
    @RequestMapping("/login")
    public ResponseVO login(@RequestBody @Validated LoginReq req) {
        return loginService.login(req);
    }

    /**
     * 注册，在业务完成注册，同时也需要同步到 Im 服务端
     *
     * @param req
     * @return
     */
    @RequestMapping("/register")
    public ResponseVO register(@RequestBody @Validated RegisterReq req) {
        return loginService.register(req);
    }

}
