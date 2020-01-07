package com.daoyuan.study.sqlsession.ds.dbs;

import com.alibaba.druid.pool.DruidDataSource;
import com.daoyuan.study.sqlsession.ds.entity.DataSourceConfig;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Component
public class CustomSqlSessionFactoryBuilder {

    @Value(value = "${mybatis.mapper-locations}")
    private String mapperLocations;

    @Value(value = "${mybatis.type-aliases-package}")
    private String typeAliasesPackage;

    @Autowired
    private AbstractEnvironment environment;

    private Map<String,String> appCodeAliasMappings = new HashMap<>();

    public Map<Object, SqlSessionFactory> buildTargetSqlSessionFactories(List<DataSourceConfig> dataSourceConfigs) {

        Map<Object,SqlSessionFactory> targetSqlSessionFactories = new HashMap<Object, SqlSessionFactory>();
        try {
            for(DataSourceConfig dataSourceConfig: dataSourceConfigs){
                String url = dataSourceConfig.getDbUrl();
                String username = dataSourceConfig.getDbUsername();
                String password = dataSourceConfig.getDbPassword();
                String driverClassName = dataSourceConfig.getDbDriverClass();
                DataSource dataSource = buildTargetDataSource(url, username, password, driverClassName);

                targetSqlSessionFactories.put(dataSourceConfig.getAlias(), buildSqlSessionFactory(dataSource));

                appCodeAliasMappings.put(dataSourceConfig.getAppCode(), dataSourceConfig.getAlias());
            }
        }catch (Exception e){

        }

        return targetSqlSessionFactories;
    }

    public SqlSessionFactory buildSqlSessionFactory(DataSource dataSource) throws Exception{
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        PathMatchingResourcePatternResolver resourceLoader = new PathMatchingResourcePatternResolver();
        sqlSessionFactoryBean.setMapperLocations(resourceLoader.getResources(mapperLocations));
        sqlSessionFactoryBean.setDataSource(dataSource);
        sqlSessionFactoryBean.setTypeAliasesPackage(typeAliasesPackage);

        return sqlSessionFactoryBean.getObject();
    }

    private Properties getProperties(){

        RelaxedPropertyResolver resolver = new RelaxedPropertyResolver(environment);
        Map<String,Object> map = resolver.getSubProperties("spring.datasource.");

        Properties properties = new Properties();

        map.forEach((key, value) -> {
            properties.put(key,value);
        });
        return properties;
    }

    public DataSource buildDefaultDataSource(){
        DataSourceProxy dataSourceProxy = new DataSourceProxy();

        DruidDataSource druidDataSource =  (DruidDataSource) dataSourceProxy.newInstance(new DruidDataSource());
        druidDataSource.configFromPropety(getProperties());
        return druidDataSource;
    }

    private DataSource buildTargetDataSource(String url,String username,String password,String driverClassName){

        DruidDataSource druidDataSource = new DruidDataSource();

        druidDataSource.configFromPropety(getProperties());

        druidDataSource.setDriverClassName(driverClassName);
        druidDataSource.setUrl(url);
        druidDataSource.setUsername(username);
        druidDataSource.setPassword(password);

        return druidDataSource;
    }

    public Map<String, String> getAppCodeAliasMapping() {
        return appCodeAliasMappings;
    }

}
