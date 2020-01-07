package com.daoyuan.study.sqlsession.ds.dbs;

import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MapperConfig {
    //MapperScannerConfigurer实现了BeanDefinitionRegistryPostProcessor 会导致MapperScannerConfigurer优先于Properties的加载,所以需要单独放开
    @Bean(name="mapperScannerConfigurer")
    public MapperScannerConfigurer mapperScannerConfigurer(){
        MapperScannerConfigurer mapperScannerConfigurer = new MapperScannerConfigurer();
        mapperScannerConfigurer.setBasePackage("com.daoyuan.study.sqlsession.ds.mapper");
        mapperScannerConfigurer.setSqlSessionTemplateBeanName("sqlSessionTemplate");
        return mapperScannerConfigurer;
    }
}
