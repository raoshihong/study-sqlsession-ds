package com.daoyuan.study.sqlsession.ds.dbs;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class DBSConfig {

    @Value(value = "${mybatis.mapper-locations}")
    private String mapperLocations;

    @Value(value = "${mybatis.type-aliases-package}")
    private String typeAliasesPackage;

    @Autowired
    private CustomSqlSessionFactoryBuilder customSqlSessionFactoryBuilder;

    //注入数据源给事务管理器
    @Bean(name = "defaultDataSource")
    public DataSource dynamicDataSource(){
        return customSqlSessionFactoryBuilder.buildDataSource();
    }

    //将数据源注入到sqlSessionFactory中
    @Bean(name = "sqlSessionFactory")
    public SqlSessionFactory sqlSessionFactory(@Qualifier("defaultDataSource") DataSource dataSource) throws Exception{
        return customSqlSessionFactoryBuilder.buildSqlSessionFactory(dataSource);
    }

    @Bean(name="sqlSessionTemplate")
    public SqlSessionTemplate sqlSessionTemplate(@Qualifier("sqlSessionFactory") SqlSessionFactory sqlSessionFactory){
        return customSqlSessionFactoryBuilder.buildSqlSessionTemplate(sqlSessionFactory);
    }

}
