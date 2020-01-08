package com.daoyuan.study.sqlsession.ds.service;

import com.daoyuan.study.sqlsession.ds.dbs.SqlSessionFactoryHolder;
import com.daoyuan.study.sqlsession.ds.entity.Channels;
import com.daoyuan.study.sqlsession.ds.mapper.ChannelsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChannelsService {
    @Autowired
    private ChannelsMapper channelsMapper;

    /**
     * 这种没有指定事务的话,是可以动态切换不同数据源的,但是如果发生错误，无法进行事务回滚
     */
//    public void save(){
//        //使用默认的数据源
//        Channels channels = new Channels();
//        channels.setName("aa");
//        channels.setAppCode("1");
//        channels.setLevelType("a");
//        channelsMapper.insert(channels);
//
//        DataSourceContextHolder.clear();
//        DataSourceContextHolder.setDBAlais("db2");
//
//        channels.setName("bb");
//        channels.setAppCode("2");
//        channels.setLevelType("b");
//        channelsMapper.insert(channels);
//    }

//    @NeedDataSource(alias = "db1")
    @Transactional
    public void saveA(){
        //使用默认的数据源
        Channels channels = new Channels();
        channels.setName("aa");
        channels.setAppCode("1");
        channels.setLevelType("a");
        channelsMapper.insert(channels);
//        int i=10/0;
    }

//    @NeedDataSource(alias = "db2")
    @Transactional
    public void saveB(){
        Channels channels = new Channels();

        channels.setName("bb");
        channels.setAppCode("2");
        channels.setLevelType("b");
        channelsMapper.insert(channels);

        System.out.println("aa");

//        int i=10/0;
    }

//    @NeedDataSource(alias = "db3")
    @Transactional
    public void saveC(){
        Channels channels = new Channels();

        channels.setName("cc");
        channels.setAppCode("3");
        channels.setLevelType("c");
        channelsMapper.insert(channels);
//        int i=10/0;

        //同一个事务中,无法做到多数据源的切换
        SqlSessionFactoryHolder.setType("db1");
        channels.setName("ff");
        channels.setAppCode("4");
        channels.setLevelType("f");
        channelsMapper.insert(channels);

//        int i = 10/0;

    }
}
