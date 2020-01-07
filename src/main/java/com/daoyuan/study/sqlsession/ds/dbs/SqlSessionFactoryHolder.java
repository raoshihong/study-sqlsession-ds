package com.daoyuan.study.sqlsession.ds.dbs;

/**
 * 用来记录当前线程中使用的是哪个数据源，不过有时候同一线程可能需要切换不同数据源（多个方法调用,不同方法中使用不同数据,此时需要先清除再设置当前数据源）
 */
public class SqlSessionFactoryHolder {
    private static final ThreadLocal<String> contextHold = new ThreadLocal<String>();

    //返回当前线程的数据源
    public static String getType(){
        return contextHold.get();
    }

    //设置当前线程中的数据源
    public static void setType(String type){
        contextHold.set(type);
    }

    //清除当前线程中的数据
    public static void clearType(){
        contextHold.remove();
    }
}
