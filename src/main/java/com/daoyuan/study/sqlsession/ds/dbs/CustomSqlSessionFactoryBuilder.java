package com.daoyuan.study.sqlsession.ds.dbs;

import com.alibaba.druid.pool.DruidDataSource;
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

    private Map<String,DataSourceConfig> dataSourceCaches = new HashMap<>();

    //保存默认数据源
    private DynamicDataSource defaultDynamicDataSource;

    @PostConstruct
    public void init(){
        //获取数据源配置,转换为DataSourceConfig对象
        RelaxedPropertyResolver resolver = new RelaxedPropertyResolver(environment);

        //获取默认数据源配置

        /**
         * d1.druid.username
         * d1.druid.password
         * d2.druid.username
         * d2.druid.password
         */
        Map<String,Object> map = resolver.getSubProperties("spring.datasource.");

        getDataSourceConfigs(map);

    }

    public void getDataSourceConfigs(Map<String,Object> map){

        map.forEach((key, value) -> {
            //获取不同数据源标识
            String alias = "db1";
            if (key.startsWith("druid.")) {
                alias = "db1";
            }else {
                int index = key.indexOf(".druid");
                alias = key.substring(0, index);
            }

            DataSourceConfig dataSourceConfig = dataSourceCaches.get(alias);
            if (Objects.isNull(dataSourceConfig)) {
                dataSourceConfig = new DataSourceConfig();
                dataSourceCaches.put(alias, dataSourceConfig);
            }

            String val = Objects.toString(value, null);
            dataSourceConfig.setAlias(alias);
            if (key.endsWith("druid.url")) {
                dataSourceConfig.setDbUrl(val);
            }
            if (key.endsWith("druid.username")) {
                dataSourceConfig.setDbUsername(val);
            }
            if (key.endsWith("druid.password")) {
                dataSourceConfig.setDbPassword(val);
            }
            if (key.endsWith("druid.driverClassName")) {
                dataSourceConfig.setDbDriverClass(val);
            }
        });
    }

    /**
     * 构建数据源
     * @return
     */
    public DataSource buildDataSource(){

        Map<Object,Object> targetDataSources = new HashMap<>();
        DataSource defaultDataSource = null;

        for (String key : dataSourceCaches.keySet()){
            DataSourceConfig dataSourceConfig = dataSourceCaches.get(key);
            String alias = dataSourceConfig.getAlias();
            String url = dataSourceConfig.getDbUrl();
            String username = dataSourceConfig.getDbUsername();
            String password = dataSourceConfig.getDbPassword();
            String driverClassName = dataSourceConfig.getDbDriverClass();
            DataSource dataSource = buildTargetDataSource(url, username, password, driverClassName);

            //在这里取出一个默认数据源
            if (alias.equals("db1")) {
                defaultDataSource = dataSource;
            }else{
                targetDataSources.put(alias,dataSource);
            }
        }

        //创建动态数据源对象
        DynamicDataSource dynamicDataSource = new DynamicDataSource();
        dynamicDataSource.addTargetDataSources(targetDataSources);
        dynamicDataSource.setDefaultTargetDataSource(defaultDataSource);

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
//        druidDataSource.configFromPropety(getProperties());

        druidDataSource.setDriverClassName(driverClassName);
        druidDataSource.setUrl(url);
        druidDataSource.setUsername(username);
        druidDataSource.setPassword(password);

        return druidDataSource;
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

    /**
     * 根据多个数据源信息创建多个数据源,并创建多个SqlSessionFactory,用于支持多个加密
     * @return
     */
    public Map<Object, SqlSessionFactory> buildTargetSqlSessionFactories() {

        Map<Object,SqlSessionFactory> targetSqlSessionFactories = new HashMap<Object, SqlSessionFactory>();

        dataSourceCaches.forEach((key, dataSourceConfig) -> {
            try {
                //每个sqlSessionFactory都是持有相同的多数据源对象
                targetSqlSessionFactories.put(key, buildSqlSessionFactory(defaultDynamicDataSource));
            } catch (Exception e) {
                log.info("创建sqlSessionFactory失败:{}", e);
            }
        });

        return targetSqlSessionFactories;
    }

    /**
     * 根据sqlSessionFactory构建SqlSessionTemplate
     * @param defaultSqlSessionFactory
     * @return
     */
    public SqlSessionTemplate buildSqlSessionTemplate(SqlSessionFactory defaultSqlSessionFactory){

        CustomSqlSessionTemplate customSqlSessionTemplate = new CustomSqlSessionTemplate(defaultSqlSessionFactory);
        customSqlSessionTemplate.setDefaultTargetSqlSessionFactory(defaultSqlSessionFactory);
        customSqlSessionTemplate.setTargetSqlSessionFactorys(buildTargetSqlSessionFactories());
        return customSqlSessionTemplate;
    }

}
