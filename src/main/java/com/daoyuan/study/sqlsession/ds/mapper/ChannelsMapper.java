package com.daoyuan.study.sqlsession.ds.mapper;


import com.daoyuan.study.sqlsession.ds.entity.Channels;

import java.util.List;

public interface ChannelsMapper {
    int insert(Channels channels);
    List<Channels> select();
}
