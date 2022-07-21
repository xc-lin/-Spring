package com.lxc.service;

import com.lxc.spring.annotation.Component;
import com.lxc.spring.springInterface.BeanPostProcessor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author Frank_lin
 * @date 2022/7/21
 */
@Component
public class MyBeanPostProcessor implements BeanPostProcessor {
    @Override
    public Object postProcessBeforeInitialization(String beanName, Object bean) {
        if ("userService".equals(beanName)) {
            System.out.println("postProcessBeforeInitialization......");
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(String beanName, Object bean) {
        if ("userService".equals(beanName)) {
            System.out.println("postProcessAfterInitialization....start..");
            Object finalBean = bean;
            bean = Proxy.newProxyInstance(bean.getClass().getClassLoader(), bean.getClass().getInterfaces(), new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    System.out.println("aop逻辑 end,,,,");
                    Object invoke = method.invoke(finalBean, args);
                    System.out.println("aop逻辑 end,,,,");
                    return invoke;
                }
            });
            System.out.println("postProcessAfterInitialization....end..");
        }


        return bean;
    }
}
