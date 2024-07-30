package com.jiangjing.im.app.bussiness;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.ResultCallback;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Collections;
import java.util.concurrent.Semaphore;

@SpringBootTest
class ImAppBusinessApplicationTests {
    public static void streamCallWithCallback()
            throws NoApiKeyException, ApiException, InputRequiredException, InterruptedException {
        Generation gen = new Generation();
        Message userMsg =
                Message.builder().role(Role.USER.getValue()).content("AQS具备什么特性？").build();
        GenerationParam param = GenerationParam.builder()
                .model("qwen-max")
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)  //set result format message
                .messages(Collections.singletonList(userMsg)) // set messages
                .topP(0.8)
                .apiKey("sk-ce8a909da6e74b74809d7f005d46ddab")
                .incrementalOutput(true) // set streaming output incrementally
                .build();
        Semaphore semaphore = new Semaphore(0);
        StringBuilder fullContent = new StringBuilder();
        gen.streamCall(param, new ResultCallback<GenerationResult>() {
            @Override
            public void onEvent(GenerationResult message) {
                /* 获取到的消息内容 */
                fullContent.append(message.getOutput().getChoices().get(0).getMessage().getContent());
                System.out.println(message.getOutput().getChoices().get(0).getMessage().getContent());
            }

            @Override
            public void onError(Exception err) {
                /* 回复异常需要回复 */
                semaphore.release();
            }

            @Override
            public void onComplete() {
                /* 回复完成，需要发送回复完成的标识 */
                semaphore.release();
            }

        });
        semaphore.acquire();
        System.out.println("Full content: \n" + fullContent);
    }


    @Test
    void contextLoads() {
    }

    @Test
    void test() {
        try {
            streamCallWithCallback();
        } catch (ApiException | NoApiKeyException | InputRequiredException | InterruptedException e) {
            System.out.println(e.getMessage());
        }
        System.exit(0);
    }
}
