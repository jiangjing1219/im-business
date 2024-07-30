package com.jiangjing.im.app.bussiness.service.impl;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.jiangjing.im.app.bussiness.common.ResponseVO;
import com.jiangjing.im.app.bussiness.config.AppConfig;
import com.jiangjing.im.app.bussiness.dao.UserEntity;
import com.jiangjing.im.app.bussiness.enums.ErrorCode;
import com.jiangjing.im.app.bussiness.enums.LoginTypeEnum;
import com.jiangjing.im.app.bussiness.enums.RegisterTypeEnum;
import com.jiangjing.im.app.bussiness.model.req.LoginReq;
import com.jiangjing.im.app.bussiness.model.req.RegisterReq;
import com.jiangjing.im.app.bussiness.model.resp.LoginResp;
import com.jiangjing.im.app.bussiness.service.LoginService;
import com.jiangjing.im.app.bussiness.service.UserService;
import com.jiangjing.im.app.bussiness.utils.SigAPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
    public ResponseVO login(LoginReq req) {
        LoginResp loginResp = new LoginResp();
        // 1、判断登录类型，根据登录类型处理
        if (req.getLoginType() == LoginTypeEnum.USERNAME_PASSWORD.getCode()) {
            // 2、查询该用户的信息
            UserEntity user = userService.getUserByUserName(req.getUserName());
            if (user == null) {
                return ResponseVO.errorResponse(ErrorCode.USER_NOT_EXIST);
            }
            if (!user.getPassword().equals(req.getPassword())) {
                return ResponseVO.errorResponse(ErrorCode.USERNAME_OR_PASSWORD_ERROR);
            }

            //satoken登录认证
            StpUtil.login(10001);

            // 3、生成 Im Server 的鉴权票据
            SigAPI sigAPI = new SigAPI(appConfig.getAppId(), appConfig.getPrivateKey());
            String imUserSig = sigAPI.genUserSig(user.getUserId(), 500000);
            loginResp.setImUserSign(imUserSig);
            loginResp.setUserId(user.getUserId());
            loginResp.setAppId(appConfig.getAppId());
            SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
            loginResp.setTokenInfo(tokenInfo);
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
