package com.jiangjing.im.app.bussiness.controller;

import com.jiangjing.im.app.bussiness.common.ResponseVO;
import com.jiangjing.im.app.bussiness.model.resp.LoginResp;
import com.jiangjing.im.app.bussiness.security.ImUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/v1/login/oauth2")
public class OAuth2Controller {



    @GetMapping("/success")
    public ResponseVO<LoginResp> oauth2LoginSuccess(@AuthenticationPrincipal ImUserDetails userDetails) {
        try {
            // 生成token
            //String accessToken = jwtService.generateAccessToken(userDetails);
            // String refreshToken = jwtService.generateRefreshToken(userDetails);

            // 构建登录响应
            LoginResp loginResp = new LoginResp();
            //loginResp.setUserId(userDetails.getUserId());
            //loginResp.setAccessToken(accessToken);
            //loginResp.setRefreshToken(refreshToken);
            loginResp.setRefreshToken("用户授权成功");
            return ResponseVO.successResponse(loginResp);
        } catch (Exception e) {
            log.error("Failed to process OAuth2 login success", e);
            return ResponseVO.errorResponse(500, "Failed to process OAuth2 login");
        }
    }

    @GetMapping("/failure")
    public ResponseVO<LoginResp> oauth2LoginFailure(@AuthenticationPrincipal ImUserDetails userDetails) {
        try {
            // 生成token
            //String accessToken = jwtService.generateAccessToken(userDetails);
            // String refreshToken = jwtService.generateRefreshToken(userDetails);

            // 构建登录响应
            LoginResp loginResp = new LoginResp();
            //loginResp.setUserId(userDetails.getUserId());
            //loginResp.setAccessToken(accessToken);
            //loginResp.setRefreshToken(refreshToken);
            loginResp.setRefreshToken("用户拒绝授权");
            return ResponseVO.successResponse(loginResp);
        } catch (Exception e) {
            log.error("Failed to process OAuth2 login success", e);
            return ResponseVO.errorResponse(500, "Failed to process OAuth2 login");
        }
    }
}
