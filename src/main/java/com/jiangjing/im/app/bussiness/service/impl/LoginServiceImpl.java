package com.jiangjing.im.app.bussiness.service.impl;

import cn.hutool.json.JSONUtil;
import com.jiangjing.im.app.bussiness.common.ResponseVO;
import com.jiangjing.im.app.bussiness.config.AppConfig;
import com.jiangjing.im.app.bussiness.dao.UserEntity;
import com.jiangjing.im.app.bussiness.enums.ErrorCode;
import com.jiangjing.im.app.bussiness.enums.LoginTypeEnum;
import com.jiangjing.im.app.bussiness.enums.RegisterTypeEnum;
import com.jiangjing.im.app.bussiness.model.req.LoginReq;
import com.jiangjing.im.app.bussiness.model.req.RegisterReq;
import com.jiangjing.im.app.bussiness.model.resp.LoginResp;
import com.jiangjing.im.app.bussiness.security.ImUserDetails;
import com.jiangjing.im.app.bussiness.service.LoginService;
import com.jiangjing.im.app.bussiness.service.UserService;
import com.jiangjing.im.common.utils.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Admin
 */
@Service
@Transactional
public class LoginServiceImpl implements LoginService {

    @Autowired
    UserService userService;

    @Autowired
    AppConfig appConfig;

    @Autowired
    AuthenticationManager authenticationManager;

    /**
     * 用户登录接口，登录类型：
     * 1、username  - password
     * 2、手机验证码
     * 3、username + 手机验证码
     *
     * @param req
     * @return
     */
    @Override
    @Transactional(propagation = Propagation.NEVER)
    public ResponseVO login(LoginReq req) {
        LoginResp loginResp = new LoginResp();
        // 1、判断登录类型，根据登录类型处理
        if (req.getLoginType() == LoginTypeEnum.USERNAME_PASSWORD.getCode()) {
            // 使用 Spring Security 验证
            UsernamePasswordAuthenticationToken authRequest = UsernamePasswordAuthenticationToken.unauthenticated(req.getUserName(),
                    req.getPassword());
            try {
                Authentication authentication = authenticationManager.authenticate(authRequest);
                if (authentication.isAuthenticated()) {
                    // 认证成功，返回认证结果，放入安全上下文中
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    UserEntity user = userService.getUserByUserName(req.getUserName());
                    // jwt 生成 token 刷新
                    ImUserDetails principal = (ImUserDetails) authentication.getPrincipal();
                    String accessToken = JWTUtil.create(user.getUserId(), user.getUserName(), JSONUtil.toJsonStr(principal),5);
                    String refreshToken = JWTUtil.create(user.getUserId(), user.getUserName(), JSONUtil.toJsonStr(principal),60 * 15);
                    loginResp.setUserId(user.getUserId());
                    loginResp.setAppId(appConfig.getAppId());
                    loginResp.setAccessToken(accessToken);
                    loginResp.setRefreshToken(refreshToken);
                }else {
                    return ResponseVO.errorResponse(403, "认证失败");
                }
            } catch (AuthenticationException e) {
                return ResponseVO.errorResponse(403, e.getLocalizedMessage());
            }
        } else {
            return ResponseVO.errorResponse(ErrorCode.USERNAME_OR_PASSWORD_ERROR);
        }
        return ResponseVO.successResponse(loginResp);
    }

    /**
     * 用户注册， 使用 httpClient 调用 Im 接口导入用户信息
     *
     * @param req
     * @return
     */
    @Override
    public ResponseVO register(RegisterReq req) {
        // 1、根据注册类型完成不同的注册逻辑
        if (RegisterTypeEnum.USERNAME.getCode() == req.getRegisterType()) {
            UserEntity user = userService.getUserByUserName(req.getUserName());
            if (user != null) {
                return ResponseVO.errorResponse(ErrorCode.REGISTER_ERROR);
            }
            ResponseVO<UserEntity> userResponseVO = userService.registerUser(req);
            return userResponseVO;
        }
        return ResponseVO.successResponse();
    }
}
