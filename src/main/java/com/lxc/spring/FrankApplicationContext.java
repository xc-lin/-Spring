package com.lxc.spring;

import com.lxc.spring.annotation.Autowired;
import com.lxc.spring.annotation.Component;
import com.lxc.spring.annotation.ComponentScan;
import com.lxc.spring.annotation.Scope;
import com.lxc.spring.springInterface.BeanNameAware;
import com.lxc.spring.springInterface.BeanPostProcessor;
import com.lxc.spring.springInterface.InitializingBean;

import java.beans.Introspector;
import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Frank_lin
 * @date 2022/7/21
 */

/**
 * Ioc容器
 */
public class FrankApplicationContext {
    private Class configClass;
    private ConcurrentHashMap<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Object> singletonObjects = new ConcurrentHashMap<>();
    private List<BeanPostProcessor> beanPostProcessorList =new ArrayList<>();


    /**
     * 构造方法
     * 初始化ioc容器
     *
     * @param configClass
     * @throws ClassNotFoundException
     */
    public FrankApplicationContext(Class configClass) throws Exception {
        this.configClass = configClass;
        // 判断配置类上是否有ComponentScan注解
        if (configClass.isAnnotationPresent(ComponentScan.class)) {
            ComponentScan componentScanAnnotation = (ComponentScan) configClass.getAnnotation(ComponentScan.class);
            // 拿到扫描路径 com.lxc.path
            String path = componentScanAnnotation.value();
            // 取到class文件的文件夹位置
            path = path.replace(".", File.separator);
            ClassLoader classLoader = FrankApplicationContext.class.getClassLoader();
            URL resource = classLoader.getResource(path);
            File file = new File(resource.getFile());
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                // 遍历所有.class文件 并将其加载，生成beanDefinition对象
                for (File f : files) {
                    String fileName = f.getAbsolutePath();
                    if (fileName.endsWith(".class")) {
                        // 暴力方法 先简单做一下
                        String className = fileName.substring(fileName.indexOf("com"), fileName.indexOf(".class"));
                        className = className.replace(File.separator, ".");
                        // 加载.class文件
                        Class<?> clazz = classLoader.loadClass(className);
                        // 判断这个类是否是bean
                        if (clazz.isAnnotationPresent(Component.class)) {

                            if (BeanPostProcessor.class.isAssignableFrom(clazz)){
                                BeanPostProcessor o = (BeanPostProcessor) clazz.newInstance();
                                beanPostProcessorList.add(o);
                            }


                            // 拿到bean的名字
                            String beanName = Introspector.decapitalize(clazz.getSimpleName());
                            Component annotation = clazz.getAnnotation(Component.class);
                            String value = annotation.value();
                            if (value != null && !"".equals(value)) {
                                beanName = value;
                            }


                            // 生成BeanDefinition对象并保存起来
                            BeanDefinition beanDefinition = new BeanDefinition();
                            beanDefinition.setType(clazz);
                            // 查看是否有Scope注解
                            if (clazz.isAnnotationPresent(Scope.class)) {
                                Scope scopeAnnotation = clazz.getAnnotation(Scope.class);

                                beanDefinition.setScope(scopeAnnotation.value());
                            } else {
                                // 默认单例
                                beanDefinition.setScope("singleton");
                            }
                            // 保存beanDefinition对象
                            beanDefinitionMap.put(beanName, beanDefinition);

                        }
                    }


                }
            }

        }

        // 创建单例bean
        for (Map.Entry<String, BeanDefinition> stringBeanDefinitionEntry : beanDefinitionMap.entrySet()) {
            BeanDefinition beanDefinition = stringBeanDefinitionEntry.getValue();
            String beanName = stringBeanDefinitionEntry.getKey();
            // 实例化单例bean
            if (beanDefinition.getScope().equals("singleton")) {
                if (singletonObjects.containsKey(beanName)){
                    continue;
                }
                Object bean = createBean(beanName, beanDefinition);
                singletonObjects.put(beanName, bean);
            }
        }
    }

    /**
     * 执行这一步之前 所有的需要实例化的BeanDefinition都已经存储在beanDefinitionMap中了
     * @param beanName
     * @param beanDefinition
     * @return
     * @throws Exception
     */
    private Object createBean(String beanName, BeanDefinition beanDefinition) throws Exception {
        Class clazz = beanDefinition.getType();

        Object instance = clazz.getConstructor().newInstance();

        // 实现依赖注入
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Autowired.class)) {
                field.setAccessible(true);
                field.set(instance, getBean(field.getName()));
            }
        }

        // 回调 会给你值
        if (instance instanceof BeanNameAware){
            ((BeanNameAware) instance).setBeanName(beanName);
        }

        // BeanPostProcessor 初始化前执行
        for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
            instance = beanPostProcessor.postProcessBeforeInitialization(beanName,instance);
        }
        // 初始化
        if (instance instanceof InitializingBean){
            ((InitializingBean) instance).afterPropertiesSet();
        }

        // BeanPostProcessor 初始化后执行
        for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
            instance =  beanPostProcessor.postProcessAfterInitialization(beanName,instance);

        }



        // 初始化 aop
        // bean的后置处理器
        return instance;
    }

    public Object getBean(String name) throws Exception {
        // 根据beanName取出 beanDefinition对象
        BeanDefinition beanDefinition = beanDefinitionMap.get(name);
        // 如果为空，则说明名字错误，抛出异常
        if (beanDefinition == null) {
            throw new NullPointerException();
        }

        String scope = beanDefinition.getScope();
        if (scope.equals("singleton")) {
            // 单例 从容器中取出
            Object bean = singletonObjects.get(name);
            if (bean == null) {
                bean = createBean(name, beanDefinition);
                singletonObjects.put(name, bean);
            }
            return bean;
        } else {
            // 多例 不考虑其他情况
            // 创建一个对象
            return createBean(name, beanDefinition);
        }
    }
}
