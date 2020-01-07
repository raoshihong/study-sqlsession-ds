package com.daoyuan.study.sqlsession.ds.controller;

import com.daoyuan.study.sqlsession.ds.dbs.CustomSqlSessionFactoryBuilder;
import com.daoyuan.study.sqlsession.ds.dbs.CustomSqlSessionTemplate;
import com.daoyuan.study.sqlsession.ds.dbs.SqlSessionFactoryHolder;
import com.daoyuan.study.sqlsession.ds.entity.DataSourceConfig;
import com.daoyuan.study.sqlsession.ds.mapper.DataSourceConfigMapper;
import com.daoyuan.study.sqlsession.ds.service.ChannelsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RestController
public class ChannelsController {

    @Autowired
    private ChannelsService channelsService;

    @Autowired
    private CustomSqlSessionTemplate customSqlSessionTemplate;

    @Autowired
    private CustomSqlSessionFactoryBuilder customSqlSessionFactoryBuilder;

    @GetMapping("/save")
    public void save(){
        try {
            //使用默认数据源
            channelsService.saveA();
        }catch (Exception e){
            e.printStackTrace();
        }

        //动态添加数据源

        String url = "jdbc:mysql://localhost:3306/db1";
        String username = "root";
        String password = "123456";
        String driverClass = "org.gjt.mm.mysql.Driver";

        List<DataSourceConfig> dataSourceConfigs = new ArrayList<>();
        DataSourceConfig dataSourceConfig = new DataSourceConfig();
        dataSourceConfig.setAlias("db1");
        dataSourceConfig.setDbUrl(url);
        dataSourceConfig.setDbUsername(username);
        dataSourceConfig.setDbPassword(password);
        dataSourceConfig.setDbDriverClass(driverClass);

        dataSourceConfigs.add(dataSourceConfig);


        url = "jdbc:mysql://localhost:3306/db2";
        username = "root";
        password = "123456";
        driverClass = "org.gjt.mm.mysql.Driver";

        DataSourceConfig dataSourceConfig1 = new DataSourceConfig();
        dataSourceConfig1.setAlias("db2");
        dataSourceConfig1.setDbUrl(url);
        dataSourceConfig1.setDbUsername(username);
        dataSourceConfig1.setDbPassword(password);
        dataSourceConfig1.setDbDriverClass(driverClass);

        dataSourceConfigs.add(dataSourceConfig1);

        url = "jdbc:mysql://localhost:3306/db3";
        username = "root";
        password = "123456";
        driverClass = "org.gjt.mm.mysql.Driver";

        DataSourceConfig dataSourceConfig2 = new DataSourceConfig();
        dataSourceConfig2.setAlias("db3");
        dataSourceConfig2.setDbUrl(url);
        dataSourceConfig2.setDbUsername(username);
        dataSourceConfig2.setDbPassword(password);
        dataSourceConfig2.setDbDriverClass(driverClass);

        dataSourceConfigs.add(dataSourceConfig2);


        customSqlSessionTemplate.setTargetSqlSessionFactorys(customSqlSessionFactoryBuilder.buildTargetSqlSessionFactories(dataSourceConfigs));
        try {
            //切换数据源
            SqlSessionFactoryHolder.setType("db2");
            channelsService.saveB();
        }catch (Exception e){
            e.printStackTrace();
        }

//        try{
//
//            //切换数据源
//            SqlSessionFactoryHolder.setType("db3");
//            channelsService.saveC();
//        }catch (Exception e){
//            e.printStackTrace();
//        }
    }
}
