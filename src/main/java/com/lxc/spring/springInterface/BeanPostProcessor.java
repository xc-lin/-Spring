package com.lxc.spring.springInterface;

/**
 * @author Frank_lin
 * @date 2022/7/21
 */
public interface BeanPostProcessor {
    /**
     *
     * @param beanName
     * @param bean
     */
    public Object postProcessBeforeInitialization(String beanName,Object bean);

    public Object postProcessAfterInitialization(String beanName,Object bean);
}
