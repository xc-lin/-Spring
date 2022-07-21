package com.lxc.service;

import com.lxc.spring.springInterface.InitializingBean;
import com.lxc.spring.annotation.Autowired;
import com.lxc.spring.annotation.Component;
import com.lxc.spring.springInterface.BeanNameAware;

/**
 * @author Frank_lin
 * @date 2022/7/21
 */
@Component
// @Scope("prototype")
public class UserService implements BeanNameAware, InitializingBean ,ServiceInterface{
    @Autowired
    private OrderService orderService;

    private String beanName;



    public void test(){
        System.out.println(orderService);
    }

    @Override
    public void setBeanName(String beanName) {
        this.beanName= beanName;
    }

    public String getBeanName() {
        return beanName;
    }

    @Override
    public void afterPropertiesSet() {
        // 做我们想做的事情
        System.out.println("afterPropertiesSet。。。。。。");
    }
}
