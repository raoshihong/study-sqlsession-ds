package com.daoyuan.study.sqlsession.ds.dbs;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement(proxyTargetClass = true)
public class DBSConfig {

    @Autowired
    private CustomSqlSessionFactoryBuilder customSqlSessionFactoryBuilder;

//    //注入数据源给事务管理器
//    @Bean(name = "defaultDataSource")
//    public DataSource dynamicDataSource(){
//        return customSqlSessionFactoryBuilder.buildDynamicDataSource();
//    }

    @Bean(name="sqlSessionTemplate")
    public SqlSessionTemplate sqlSessionTemplate() throws Exception{
        return customSqlSessionFactoryBuilder.buildSqlSessionTemplate();
    }

}
