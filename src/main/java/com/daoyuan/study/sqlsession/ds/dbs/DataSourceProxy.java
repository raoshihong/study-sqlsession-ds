package com.daoyuan.study.sqlsession.ds.dbs;

import com.daoyuan.study.sqlsession.ds.utils.SpringContext;

import javax.sql.DataSource;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;

public class DataSourceProxy implements InvocationHandler {
    private static final String INVOKE_METHOD = "getConnection";
    private DataSource target;

    public DataSourceProxy() {
    }

    public DataSource newInstance(DataSource target) {
        this.target = target;
        return (DataSource) Proxy.newProxyInstance(this.target.getClass().getClassLoader(), new Class[]{DataSource.class}, this);
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        CustomSqlSessionTemplate sessionTemplate = SpringContext.getBean(CustomSqlSessionTemplate.class);
        if (Objects.isNull(sessionTemplate)) {
            return method.invoke(this.target, args);
        }
        return sessionTemplate.getSqlSessionFactory().getConfiguration().getEnvironment().getDataSource().getConnection();
    }
}