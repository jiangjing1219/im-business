package com.jiangjing.im.app.bussiness;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.function.Consumer;

@SpringBootTest
class ImAppBussinessApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void test() {
        this.testInterfaceMethod("123", new Consumer<String>() {
            @Override
            public void accept(String param) {
                System.out.println("执行函数接口的私有逻辑，"+param);
            }
        });
    }

    public void testInterfaceMethod(String param, Consumer<String> consumer) {
        System.out.println("执行方法的公共逻辑:" + param);
        consumer.accept("函数接口参数");
    }
}
