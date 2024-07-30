package com.jiangjing.im.app.bussiness.model.req;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author: Chackylee
 * @description:
 **/
@Data
public class RegisterReq {

    @NotBlank(message = "用户名不能为空")
    private String userName;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotNull(message = "请选择注册方式")
    //注册方式 2 手机号注册、 1 用户名
    private Integer registerType;

    private String proto;

}
