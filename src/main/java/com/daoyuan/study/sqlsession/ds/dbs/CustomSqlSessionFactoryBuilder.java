package com.daoyuan.study.sqlsession.ds.dbs;

import com.alibaba.druid.pool.DruidDataSource;
import com.daoyuan.study.sqlsession.ds.constants.DataSourceConstants;
import com.daoyuan.study.sqlsession.ds.entity.DataSourceConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

/**
 * 通过DynamicDataSource进行多数据源切换
 * 通过自定义SqlSessionFactory进行不同加密方式的支持
 */
@Slf4j
@Component
public class CustomSqlSessionFactoryBuilder{

    @Value(value = "${mybatis.mapper-locations}")
    private String mapperLocations;

    @Value(value = "${mybatis.type-aliases-package}")
    private String typeAliasesPackage;

    @Autowired
    private AbstractEnvironment environment;

    /**
     * db1 - properties:{username:root,password:123456}
     * db2 - properties:{username:root,password:11111}
     */
    private Map<String, Properties> propertiesMap = new HashMap<>();

    private Map<String, DataSource> dataSourceMap = new HashMap<>();

    @PostConstruct
    public void init(){
        //获取数据源配置,转换为DataSourceConfig对象
        RelaxedPropertyResolver resolver = new RelaxedPropertyResolver(environment);

        //获取默认数据源配置

        /**
         * spring.datasource.druid.username   -> druid.username
         * spring.datasource.druid.password   -> druid.password
         * spring.datasource.d2.druid.username -> d2.druid.username
         * spring.datasource.d2.druid.password -> d2.druid.password
         */
        Map<String,Object> map = resolver.getSubProperties(DataSourceConstants.DBS_PREFIX);

        map.forEach((key, value) -> {
            //获取不同数据源标识
            String alias;
            String subKey = key;
            if (key.startsWith(DataSourceConstants.SUB_PREFIX)) {
                alias = DataSourceConstants.DEFAULT_DBS_ALIAS;
            }else {
                int index = key.indexOf(DataSourceConstants.SUB_PREFIX);
                alias = key.substring(0, index-1);
                subKey = key.substring(index);
            }
            Properties properties = propertiesMap.get(alias);
            if (Objects.isNull(properties)) {
                properties = new Properties();
                propertiesMap.put(alias,properties);
            }

            //处理key,转换为以druid开头的key  比如:   druid.testWhileIdle
            properties.put(subKey,value);
        });

        dataSourceMap = buildDataSources();
    }

    /**
     * 根据用户定义的数据源信息创建数据源对象
     * @param properties
     * @return
     */
    private DataSource buildDataSource(Properties properties){
        DruidDataSource druidDataSource = new DruidDataSource();
        druidDataSource.configFromPropety(properties);
        return druidDataSource;
    }

    /**
     * 构建数据源
     * @return
     */
    public Map<String,DataSource> buildDataSources(){
        Map<String,DataSource> dataSources = new HashMap<>();
        for (String key : propertiesMap.keySet()){//key = db1,db2
            Properties properties = propertiesMap.get(key);
            //在这里取出一个默认数据源
            dataSources.put(key,buildDataSource(properties));
        }

        return dataSources;
    }

    public Map<String,DataSource> getDataSourceMap(){
        return this.dataSourceMap;
    }

    public DynamicDataSource buildDynamicDataSource(){
        Map<Object,Object> targetDataSources = new HashMap<>();
        targetDataSources.putAll(dataSourceMap);

        //创建动态数据源对象
        DynamicDataSource dynamicDataSource = new DynamicDataSource();
        dynamicDataSource.addTargetDataSources(targetDataSources);
        dynamicDataSource.setDefaultTargetDataSource(targetDataSources.get(DataSourceConstants.DEFAULT_DBS_ALIAS));

        return dynamicDataSource;
    }


    /**
     * 根据单个数据源创建SqlSessionFactory
     * @param dataSource
     * @return
     * @throws Exception
     */
    private SqlSessionFactory buildSqlSessionFactory(DataSource dataSource) throws Exception{
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        PathMatchingResourcePatternResolver resourceLoader = new PathMatchingResourcePatternResolver();
        sqlSessionFactoryBean.setMapperLocations(resourceLoader.getResources(mapperLocations));
        sqlSessionFactoryBean.setDataSource(dataSource);
        sqlSessionFactoryBean.setTypeAliasesPackage(typeAliasesPackage);
        SqlSessionFactory sqlSessionFactory = sqlSessionFactoryBean.getObject();
        return sqlSessionFactory;
    }

    /**
     * 根据多个数据源信息创建多个数据源,并创建多个SqlSessionFactory,用于支持多个加密
     * @return
     */
    public Map<Object, SqlSessionFactory> buildSqlSessionFactories() {

        Map<Object,SqlSessionFactory> targetSqlSessionFactories = new HashMap<Object, SqlSessionFactory>();

        dataSourceMap.forEach((key, dataSource) -> {
            try {
                //每个sqlSessionFactory都持有对应的数据源
                targetSqlSessionFactories.put(key, buildSqlSessionFactory(dataSource));
            } catch (Exception e) {
                log.info("创建sqlSessionFactory失败:{}", e);
            }
        });

        return targetSqlSessionFactories;
    }

    /**
     * 根据sqlSessionFactory构建SqlSessionTemplate
     * @return
     */
    public SqlSessionTemplate buildSqlSessionTemplate(){
        Map<Object, SqlSessionFactory> sqlSessionFactoryMap = buildSqlSessionFactories();
        CustomSqlSessionTemplate customSqlSessionTemplate = new CustomSqlSessionTemplate(sqlSessionFactoryMap.get(DataSourceConstants.DEFAULT_DBS_ALIAS));
        customSqlSessionTemplate.setDefaultTargetSqlSessionFactory(sqlSessionFactoryMap.get(DataSourceConstants.DEFAULT_DBS_ALIAS));
        customSqlSessionTemplate.setTargetSqlSessionFactorys(sqlSessionFactoryMap);
        return customSqlSessionTemplate;
    }

}
