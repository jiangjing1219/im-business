package com.jiangjing.im.app.bussiness.model.resp;

import cn.dev33.satoken.stp.SaTokenInfo;
import lombok.Data;

/**
 * @author: Chackylee
 * @description:
 **/
@Data
public class LoginResp {

    //im的token
    private String imUserSign;

    //登录的token信息
    private SaTokenInfo tokenInfo;

    private String userId;

    private Integer appId;

}
