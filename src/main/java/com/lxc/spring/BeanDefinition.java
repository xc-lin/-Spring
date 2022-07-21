package com.lxc.spring;

/**
 * @author Frank_lin
 * @date 2022/7/21
 */
// bean的定义
public class BeanDefinition {
    private Class type;
    // 单例还是多例
    private String scope;

    public Class getType() {
        return type;
    }

    public void setType(Class type) {
        this.type = type;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }
}
