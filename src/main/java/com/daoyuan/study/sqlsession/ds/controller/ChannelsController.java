package com.daoyuan.study.sqlsession.ds.controller;

import com.daoyuan.study.sqlsession.ds.dbs.SqlSessionFactoryHolder;
import com.daoyuan.study.sqlsession.ds.service.ChannelsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChannelsController {

    @Autowired
    private ChannelsService channelsService;

    @GetMapping("/save")
    public void save(){
        try {
            //使用默认数据源
            channelsService.saveA();
        }catch (Exception e){
            e.printStackTrace();
        }

        try {
            //切换数据源
            SqlSessionFactoryHolder.setType("db2");
            channelsService.saveB();
        }catch (Exception e){
            e.printStackTrace();
        }

        try{

            //切换数据源
            SqlSessionFactoryHolder.setType("db3");
            channelsService.saveC();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @GetMapping(value = "/test")
    public void test(){
        SqlSessionFactoryHolder.setType("db3");
        channelsService.test();
    }

    @GetMapping(value = "/test2")
    public void test2(){
        SqlSessionFactoryHolder.setType("db3");
        channelsService.test2();
    }

    @GetMapping(value = "/insert")
    public void insert(){
        SqlSessionFactoryHolder.setType("db2");
        channelsService.insert();
    }

    @GetMapping(value = "/insert2")
    public void insert2(){
        SqlSessionFactoryHolder.setType("db2");
        channelsService.insert2();
    }
}
