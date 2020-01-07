package com.daoyuan.study.sqlsession.ds.dbs;

import com.alibaba.druid.pool.DruidDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class DBSConfig {

    @Value(value = "${mybatis.mapper-locations}")
    private String mapperLocations;

    @Value(value = "${mybatis.type-aliases-package}")
    private String typeAliasesPackage;

    @Autowired
    private CustomSqlSessionFactoryBuilder customSqlSessionFactoryBuilder;

    @Bean(name = "defaultDataSource")
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource dynamicDataSource(){
        return customSqlSessionFactoryBuilder.buildDefaultDataSource();
    }

    //将数据源注入到sqlSessionFactory中
    @Bean(name = "sqlSessionFactory")
    public SqlSessionFactory sqlSessionFactory(@Qualifier("defaultDataSource") DataSource dataSource) throws Exception{
        return customSqlSessionFactoryBuilder.buildSqlSessionFactory(dataSource);
    }

    @Bean(name="sqlSessionTemplate")
    public SqlSessionTemplate sqlSessionTemplate(@Qualifier("sqlSessionFactory") SqlSessionFactory sqlSessionFactory){
        Map<Object,SqlSessionFactory> targetSqlSessionFactories = new HashMap<>();
        targetSqlSessionFactories.put("db1",sqlSessionFactory);
        CustomSqlSessionTemplate customSqlSessionTemplate = new CustomSqlSessionTemplate(sqlSessionFactory);
        customSqlSessionTemplate.setDefaultTargetSqlSessionFactory(sqlSessionFactory);
        customSqlSessionTemplate.setTargetSqlSessionFactorys(targetSqlSessionFactories);
        return customSqlSessionTemplate;
    }

}
