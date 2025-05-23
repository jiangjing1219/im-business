package com.jiangjing.im.app.bussiness.controller;

import com.alibaba.fastjson.JSONObject;
import com.jiangjing.im.app.bussiness.common.ResponseVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Admin
 */
@RestController
public class CallbackController {

    private static Logger logger = LoggerFactory.getLogger(CallbackController.class);

    @RequestMapping("/callback")
    public ResponseVO callback(@RequestBody Object req, String command, Integer appId) {
        logger.info("{}收到{}回调数据：{}", appId, command, JSONObject.toJSONString(req));
        return ResponseVO.successResponse();
    }
}
