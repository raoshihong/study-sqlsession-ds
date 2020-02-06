package com.daoyuan.study.sqlsession.ds.dbs;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.xa.DruidXADataSource;
import com.atomikos.jdbc.AtomikosDataSourceBean;
import com.daoyuan.study.sqlsession.ds.constants.DataSourceConstants;
import com.mysql.jdbc.jdbc2.optional.MysqlXADataSource;
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
import javax.sql.XADataSource;
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
//        DruidDataSource druidDataSource = new DruidDataSource();
        DruidXADataSource dataSource = new DruidXADataSource();
        dataSource.configFromPropety(properties);
//        dataSource.setValidationQuery("select 1");
//        MysqlXADataSource dataSource = new MysqlXADataSource();
//        dataSource.setUrl(properties.getProperty("druid.url"));
//        dataSource.setUsername(properties.getProperty("druid.username"));
//        dataSource.setPassword(properties.getProperty("druid.password"));
//        dataSource.setPinGlobalTxToPhysicalConnection(true);
        return dataSource;
    }

    /**
     * 使用XA
     * @return
     */
    private AtomikosDataSourceBean buildAtomikosDataSourceBean(XADataSource xaDataSource) throws Exception{
        AtomikosDataSourceBean atomikosDataSourceBean = new AtomikosDataSourceBean();
        atomikosDataSourceBean.setPoolSize(10);
        atomikosDataSourceBean.setMinPoolSize(10);
        atomikosDataSourceBean.setMaxPoolSize(100);
        atomikosDataSourceBean.setBorrowConnectionTimeout(60);
        atomikosDataSourceBean.setReapTimeout(10);
        atomikosDataSourceBean.setMaxIdleTime(60);// 最大空闲时间
        atomikosDataSourceBean.setMaintenanceInterval(60);
        atomikosDataSourceBean.setLoginTimeout(60);
        atomikosDataSourceBean.setTestQuery("select 1");
        atomikosDataSourceBean.setXaDataSource(xaDataSource);

        return atomikosDataSourceBean;
    }

    /**
     * 构建数据源
     * @return
     */
    public Map<String,DataSource> buildDataSources(){
        Map<String, DataSource> dataSources = new HashMap<>();
        try {
            for (String key : propertiesMap.keySet()) {//key = db1,db2
                Properties properties = propertiesMap.get(key);

                DataSource dataSource = buildDataSource(properties);

                //包装成XADataSource
                AtomikosDataSourceBean atomikosDataSourceBean = buildAtomikosDataSourceBean((XADataSource) dataSource);
                atomikosDataSourceBean.setUniqueResourceName(key);

                dataSources.put(key, atomikosDataSourceBean);
            }
        }catch (Exception e){
            log.info("构建数据源失败:{}",e);
        }
        return dataSources;
    }

//    public Map<String,DataSource> getDataSourceMap(){
//        return this.dataSourceMap;
//    }

//    public DynamicDataSource buildDynamicDataSource(){
//        Map<Object,Object> targetDataSources = new HashMap<>();
//        targetDataSources.putAll(dataSourceMap);
//
//        //创建动态数据源对象
//        DynamicDataSource dynamicDataSource = new DynamicDataSource();
//        dynamicDataSource.addTargetDataSources(targetDataSources);
//        dynamicDataSource.setDefaultTargetDataSource(targetDataSources.get(DataSourceConstants.DEFAULT_DBS_ALIAS));
//
//        return dynamicDataSource;
//    }


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
     * 根据多个数据源信息创建多个数据源,并创建多个SqlSessionFactory,用于支持多个加密和多个数据源
     * @return
     */
    public Map<String, SqlSessionFactory> buildSqlSessionFactories() throws Exception{

        Map<String,SqlSessionFactory> sqlSessionFactoryMap = new HashMap<String, SqlSessionFactory>();

        for (String key: dataSourceMap.keySet()){
            DataSource dataSource = dataSourceMap.get(key);
            sqlSessionFactoryMap.put(key, buildSqlSessionFactory(dataSource));
        }

        return sqlSessionFactoryMap;
    }

    /**
     * 根据sqlSessionFactory构建SqlSessionTemplate
     * @return
     */
    public SqlSessionTemplate buildSqlSessionTemplate() throws Exception{
        Map<String, SqlSessionFactory> sqlSessionFactoryMap = buildSqlSessionFactories();

        Map<Object,SqlSessionFactory> targetSqlSessionFactorys = new HashMap<>();
        targetSqlSessionFactorys.putAll(sqlSessionFactoryMap);

        SqlSessionFactory defaultSqlSessionFactory = targetSqlSessionFactorys.get(DataSourceConstants.DEFAULT_DBS_ALIAS);
        CustomSqlSessionTemplate customSqlSessionTemplate = new CustomSqlSessionTemplate(defaultSqlSessionFactory);
        customSqlSessionTemplate.setDefaultTargetSqlSessionFactory(defaultSqlSessionFactory);
        customSqlSessionTemplate.setTargetSqlSessionFactorys(targetSqlSessionFactorys);
        return customSqlSessionTemplate;
    }

}
