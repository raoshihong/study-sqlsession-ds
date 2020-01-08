package com.daoyuan.study.sqlsession.ds.entity;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class DataSourceConfig {
    private Long id;
    private String dbUrl;
    private String dbUsername;
    private String dbPassword;
    private String dbType;
    private String dbDriverClass;
    private String alias;
}
