package com.dxh;

import com.dxh.annotation.DrpcService;
import com.dxh.proxy.DrpcProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
@Component
public class DrpcProxyBeanPostProcessor implements BeanPostProcessor {
    //拦截所有的bean的创建，在bean初始化后调用
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        //生成代理
        Field[] declaredFields = bean.getClass().getDeclaredFields();
        for (Field field : declaredFields) {
            DrpcService drpcService = field.getAnnotation(DrpcService.class);
            if(drpcService!=null){
                Class<?> type = field.getType();
                field.setAccessible(true);
                Object proxy = DrpcProxyFactory.getProxy(type);
                try {
                    field.set(bean,proxy);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }

            }
        }
        return bean;
    }
}
