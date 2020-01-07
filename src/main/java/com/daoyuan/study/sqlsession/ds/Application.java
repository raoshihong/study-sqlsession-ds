package com.daoyuan.study.sqlsession.ds;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy
public class Application {

    public static void main(String[] args){
//        SpringApplication springApplication = new SpringApplication(Application.class);
//        springApplication.addListeners(new ApplicationStartListener());
//        springApplication.run(args);
        SpringApplication.run(Application.class,args);
    }

}
