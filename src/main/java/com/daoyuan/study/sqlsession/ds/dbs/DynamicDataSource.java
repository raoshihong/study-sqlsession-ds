package com.daoyuan.study.sqlsession.ds.dbs;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import java.util.HashMap;
import java.util.Map;

/**
 * 动态数据源的添加和获取的类(因为在进入@Transaction方法前会被事务拦截器拦截到,此时会去获取当前数据源)
 *
 * 实际上就是代理了数据源
 */
public class DynamicDataSource extends AbstractRoutingDataSource {

    /**
     * 用于保存已加载的所有动态数据源,供动态添加使用
     */
    private Map<Object,Object> dataSources = new HashMap<Object, Object>();

    /**
     * 增量添加添加用户自定义的数据源
     * @param targetDataSources
     */
    public void addTargetDataSources(Map<Object, Object> targetDataSources) {
        dataSources.putAll(targetDataSources);
        this.setTargetDataSources(dataSources);
        //这个方法必须调用,才能让spring知道数据源有变更
        this.afterPropertiesSet();//需要更新resolvedDataSources或者resolvedDefaultDataSource
    }

    /**
     * 返回用户自定义的数据源
     *  这个方法的调用流程
     *  AbstractRoutingDataSource.getConnection() -> determineTargetDataSource() -> determineCurrentLookupKey()
     *  所以在获取连接时返回所需要的数据源
     * @return
     */
    @Override
    protected Object determineCurrentLookupKey() {
        //每次都从当前线程中获取对应的数据源对象
//        return DataSourceContextHolder.getDBAlias();
        return null;
    }

}
