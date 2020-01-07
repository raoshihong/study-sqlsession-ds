
启动项目调用实例化事务管理器
org.springframework.jdbc.datasource.DataSourceTransactionManager.DataSourceTransactionManager(javax.sql.DataSource)
    此时使用的就是默认的数据源
    

调用进入@Transaction方法，被事务拦截器拦截
org.springframework.transaction.interceptor.TransactionAspectSupport.invokeWithinTransaction
    获取事务管理器
    final PlatformTransactionManager tm = determineTransactionManager(txAttr);
    
    开始事务：
    org.springframework.jdbc.datasource.DataSourceTransactionManager.doGetTransaction
        在这里调用datasource.getConnection()获取连接,这里是关键点，如果我们注入的是DynamicDatasource则可以进行数据源切换
        因为在DynamicDatasource中重写了getConnection()方法
    
    org.springframework.jdbc.datasource.DataSourceTransactionManager.doBegin
    开启事务
    
    接着进入目标方法
    
    
上面的事物管理器只会创建一个实例，所以在此进入@Transaction方法时,此时的DataSource仍然为默认的数据源

