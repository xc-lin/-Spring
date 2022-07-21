package com.lxc.service;

import com.lxc.spring.FrankApplicationContext;

/**
 * @author Frank_lin
 * @date 2022/7/21
 */
public class Test {
    public static void main(String[] args) throws Exception {
        FrankApplicationContext frankApplicationContext = new FrankApplicationContext(AppConfig.class);
        ServiceInterface userService = (ServiceInterface) frankApplicationContext.getBean("userService");
        userService.test();


    }
}
