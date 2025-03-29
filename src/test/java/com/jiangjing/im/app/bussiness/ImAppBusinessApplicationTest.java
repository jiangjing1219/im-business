package com.jiangjing.im.app.bussiness;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.exceptions.ValidateException;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.jwt.JWTValidator;
import cn.hutool.jwt.signers.JWTSignerUtil;
import com.jiangjing.im.app.bussiness.dao.UserEntity;
import com.jiangjing.im.app.bussiness.security.ImUserDetails;
import com.jiangjing.im.common.utils.JWTUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class ImAppBusinessApplicationTest {

    @Test
    void contextLoads() {
        UserEntity userEntity = new UserEntity();
        userEntity.setUserId("1000");
        userEntity.setMobile("11111");
        String accessToken = JWTUtil.create("1000", "test", JSONUtil.toJsonStr(new ImUserDetails(userEntity)), 20);



        JSONObject payload = JWTUtil.getPayload(accessToken);
        String o = (String) JWTUtil.getPayload(accessToken).get("principal");
        System.out.println(accessToken);


    }
}
