package com.jiangjing.im.app.bussiness.service;

import com.alibaba.fastjson.JSONObject;
import com.jiangjing.im.app.bussiness.common.ResponseVO;
import com.jiangjing.im.app.bussiness.config.AppConfig;
import com.jiangjing.im.app.bussiness.dao.UserEntity;
import com.jiangjing.im.app.bussiness.model.proto.ImportUserProto;
import com.jiangjing.im.app.bussiness.utils.HttpRequestUtils;
import com.jiangjing.im.app.bussiness.utils.SigAPI;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author
 */
@Service
@Transactional
public class ImService implements InitializingBean {

    @Autowired
    HttpRequestUtils httpRequestUtils;

    @Autowired
    AppConfig appConfig;

    private SigAPI sigAPI;

    public volatile static Map<String, Object> parameter;

    public static final Object lock = new Object();

    public ResponseVO importUser(List<UserEntity> userEntities) {
        ImportUserProto proto = new ImportUserProto();
        List<ImportUserProto.UserData> userData = new ArrayList<>();
        userEntities.forEach(userEntity -> {
            ImportUserProto.UserData user = new ImportUserProto.UserData();
            user.setUserId(userEntity.getUserId());
            user.setNickName(userEntity.getUserName());
            user.setUserType(1);
            userData.add(user);
        });
        String uri = "/user/importUser";
        try {
            proto.setUserData(userData);
            ResponseVO responseVO = httpRequestUtils.doPost(getUrl(uri), ResponseVO.class, getParameter(), null, JSONObject.toJSONString(proto), "");
            return responseVO;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseVO.errorResponse();
    }

    private Map<String, Object> getParameter() {
        if (parameter == null) {
            synchronized (lock) {
                if(parameter == null){
                    parameter = new ConcurrentHashMap<>();
                    parameter.put("appId",appConfig.getAppId());
                    parameter.put("userSign",sigAPI.genUserSig(appConfig.getAdminId(),500000));
                    parameter.put("identifier",appConfig.getAdminId());
                }
            }
        }
        return parameter;
    }

    private String getUrl(String uri) {
        return appConfig.getImUrl() + "/" + appConfig.getImVersion() + uri;
    }

    @Override
    public void afterPropertiesSet() {
        sigAPI = new SigAPI(appConfig.getAppId(), appConfig.getPrivateKey());
    }
}
