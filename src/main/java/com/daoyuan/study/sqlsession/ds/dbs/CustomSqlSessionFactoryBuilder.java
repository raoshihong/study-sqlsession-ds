package com.daoyuan.study.sqlsession.ds.dbs;

import com.alibaba.druid.pool.DruidDataSource;
import com.daoyuan.study.sqlsession.ds.entity.DataSourceConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.*;

@Slf4j
@Component
public class CustomSqlSessionFactoryBuilder {

    @Value(value = "${mybatis.mapper-locations}")
    private String mapperLocations;

    @Value(value = "${mybatis.type-aliases-package}")
    private String typeAliasesPackage;

    @Autowired
    private AbstractEnvironment environment;

    //将数据源别名和appCode进行缓存
    private Map<String,String> appCodeAliasCaches = new HashMap<>();

    //将所有的数据源都缓存起来,(可以供事物管理器使用)
    private Map<Object,Object> dataSourceCaches = new HashMap<>();

    //保存默认数据源
    private DynamicDataSource defaultDynamicDataSource;

    /**
     * 根据多个数据源信息创建多个数据源,并创建多个SqlSessionFactory
     * @param dataSourceConfigs
     * @return
     */
    public Map<Object, SqlSessionFactory> buildTargetSqlSessionFactories(List<DataSourceConfig> dataSourceConfigs) {

        Map<Object,SqlSessionFactory> targetSqlSessionFactories = new HashMap<Object, SqlSessionFactory>();
        try {

            for (DataSourceConfig dataSourceConfig:dataSourceConfigs){
                String alias = dataSourceConfig.getAlias();
                String url = dataSourceConfig.getDbUrl();
                String username = dataSourceConfig.getDbUsername();
                String password = dataSourceConfig.getDbPassword();
                String driverClassName = dataSourceConfig.getDbDriverClass();
                String appCode = dataSourceConfig.getAppCode();
                DataSource dataSource = buildTargetDataSource(url, username, password, driverClassName);

                //构建SqlSessionFactory
                Map<Object,Object> targetDataSources = new HashMap<>();
                targetDataSources.put(alias,dataSource);
                DynamicDataSource dynamicDataSource = buildDynamicDataSource(dataSource,targetDataSources);
                targetSqlSessionFactories.put(dataSourceConfig.getAlias(), buildSqlSessionFactory(dynamicDataSource));

                //缓存
                appCodeAliasCaches.put(appCode, alias);
                dataSourceCaches.put(alias,dataSource);
            }

            //将所有的数据源都设置到默认的动态数据源对象中
            defaultDynamicDataSource.addTargetDataSources(dataSourceCaches);

        }catch (Exception e){
            log.info("创建sqlSessionFactory失败:{}",e);
        }

        return targetSqlSessionFactories;
    }

    /**
     * 根据单个数据源创建SqlSessionFactory
     * @param dataSource
     * @return
     * @throws Exception
     */
    public SqlSessionFactory buildSqlSessionFactory(DataSource dataSource) throws Exception{
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        PathMatchingResourcePatternResolver resourceLoader = new PathMatchingResourcePatternResolver();
        sqlSessionFactoryBean.setMapperLocations(resourceLoader.getResources(mapperLocations));
        sqlSessionFactoryBean.setDataSource(dataSource);
        sqlSessionFactoryBean.setTypeAliasesPackage(typeAliasesPackage);
        SqlSessionFactory sqlSessionFactory = sqlSessionFactoryBean.getObject();
        return sqlSessionFactory;
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


    private DynamicDataSource buildDynamicDataSource(DataSource defaultDataSource,Map<Object,Object> targetDataSources){
        DynamicDataSource dynamicDataSource = new DynamicDataSource();
        dynamicDataSource.addTargetDataSources(targetDataSources);
        dynamicDataSource.setDefaultTargetDataSource(defaultDataSource);
        return dynamicDataSource;
    }

    /**
     * 构建默认的数据源
     * @return
     */
    public DataSource buildDefaultDataSource(){
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

        Map<Object,Object> targetDataSources = new HashMap<>();


        for (DataSourceConfig dataSourceConfig3:dataSourceConfigs){
            String alias = dataSourceConfig3.getAlias();
            url = dataSourceConfig3.getDbUrl();
            username = dataSourceConfig3.getDbUsername();
            password = dataSourceConfig3.getDbPassword();
            String driverClassName = dataSourceConfig3.getDbDriverClass();
            String appCode = dataSourceConfig3.getAppCode();
            DataSource dataSource = buildTargetDataSource(url, username, password, driverClassName);

            targetDataSources.put(alias,dataSource);
        }


        DruidDataSource druidDataSource = new DruidDataSource();
        druidDataSource.configFromPropety(getProperties());

        DynamicDataSource dynamicDataSource = buildDynamicDataSource(druidDataSource,targetDataSources);
        defaultDynamicDataSource = dynamicDataSource;
        return dynamicDataSource;
    }

    /**
     * 根据用户定义的数据源信息创建数据源对象
     * @param url
     * @param username
     * @param password
     * @param driverClassName
     * @return
     */
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
        return appCodeAliasCaches;
    }

}
