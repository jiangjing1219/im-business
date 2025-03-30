package com.jiangjing.im.app.bussiness.controller;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.ResultCallback;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.utils.JsonUtils;
import com.jiangjing.im.app.bussiness.common.ResponseVO;
import com.jiangjing.im.app.bussiness.dao.UserEntity;
import com.jiangjing.im.app.bussiness.model.req.LoginReq;
import com.jiangjing.im.app.bussiness.model.req.RegisterReq;
import com.jiangjing.im.app.bussiness.service.LoginService;
import com.jiangjing.im.app.bussiness.service.UserService;
import io.reactivex.Flowable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.concurrent.Semaphore;

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

    @Autowired
    UserService userService;

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
     * 注册，在业务完成注册，同时也需要同步到 Im 服务端，问题
     *
     * @param req
     * @return
     */
    @RequestMapping("/register")
    public ResponseVO register(@RequestBody @Validated RegisterReq req) {
        return loginService.register(req);
    }


    @RequestMapping("/getUser")
    public UserEntity getUser(@RequestParam String username) {
        return userService.getUserByUserName(username);
    }

    public static void streamCallWithMessage()
            throws NoApiKeyException, ApiException, InputRequiredException {
        Generation gen = new Generation();
        Message userMsg =
                Message.builder().role(Role.USER.getValue()).content("Introduce the capital of China").build();
        GenerationParam param = GenerationParam.builder()
                .model("qwen-max")
                .messages(Arrays.asList(userMsg))
                .resultFormat(GenerationParam.ResultFormat.MESSAGE) // the result if message format.
                .topP(0.8).enableSearch(true) // set streaming output
                .incrementalOutput(true) // get streaming output incrementally
                .build();
        Flowable<GenerationResult> result = gen.streamCall(param);
        StringBuilder fullContent = new StringBuilder();
        result.blockingForEach(message -> {
            fullContent.append(message.getOutput().getChoices().get(0).getMessage().getContent());
            System.out.println(JsonUtils.toJson(message));
        });
        System.out.println("Full content: \n" + fullContent.toString());
    }

    public static void streamCallWithCallback()
            throws NoApiKeyException, ApiException, InputRequiredException, InterruptedException {
        Generation gen = new Generation();
        Message userMsg =
                Message.builder().role(Role.USER.getValue()).content("Introduce the capital of China").build();
        GenerationParam param = GenerationParam.builder()
                .model("${modelCode}")
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)  //set result format message
                .messages(Arrays.asList(userMsg)) // set messages
                .topP(0.8)
                .incrementalOutput(true) // set streaming output incrementally
                .build();
        Semaphore semaphore = new Semaphore(0);
        StringBuilder fullContent = new StringBuilder();
        gen.streamCall(param, new ResultCallback<GenerationResult>() {

            @Override
            public void onEvent(GenerationResult message) {
                fullContent.append(message.getOutput().getChoices().get(0).getMessage().getContent());
                System.out.println(message);
            }

            @Override
            public void onError(Exception err) {
                System.out.println(String.format("Exception: %s", err.getMessage()));
                semaphore.release();
            }

            @Override
            public void onComplete() {
                System.out.println("Completed");
                semaphore.release();
            }

        });
        semaphore.acquire();
        System.out.println("Full content: \n" + fullContent.toString());
    }

    @RequestMapping("/test")
    public GenerationResult test(String problem) {
        try {
            streamCallWithMessage();
        } catch (ApiException | NoApiKeyException | InputRequiredException e) {
            System.out.println(e.getMessage());
        }
        try {
            streamCallWithCallback();
        } catch (ApiException | NoApiKeyException | InputRequiredException
                 | InterruptedException e) {
            System.out.println(e.getMessage());
        }
        System.exit(0);
        return null;
    }
}
