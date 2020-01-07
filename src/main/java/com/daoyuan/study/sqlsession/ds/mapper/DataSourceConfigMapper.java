package com.daoyuan.study.sqlsession.ds.mapper;


import com.daoyuan.study.sqlsession.ds.entity.DataSourceConfig;

import java.util.List;

public interface DataSourceConfigMapper {
    List<DataSourceConfig> selectList();
}
