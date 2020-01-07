package com.daoyuan.study.sqlsession.ds.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class SpringContext implements ApplicationContextAware {
    private static ApplicationContext ctxt;
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ctxt = applicationContext;
    }

    public static  <T> T getBean(Class<T> clazz){
        try{
           return ctxt.getBean(clazz);
        }catch (Exception e){

        }
        return null;
    }
}
