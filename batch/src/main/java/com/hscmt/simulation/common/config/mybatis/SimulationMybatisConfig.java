package com.hscmt.simulation.common.config.mybatis;

import com.hscmt.common.props.DataSourceProps;
import com.hscmt.common.util.DatabaseUtil;
import com.hscmt.common.util.ResourceUtil;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@MapperScan(
        basePackages = {"com.hscmt.simulation"},
        sqlSessionTemplateRef = "simulationSqlSessionTemplate",
        annotationClass = SimulationMapper.class
)
public class SimulationMybatisConfig {

//    @Bean(name = "simulationMybatisDataSource")
//    public DataSource dataSource(@Qualifier("simulationDataSourceProps") DataSourceProps dataSourceProps) {
//        return DatabaseUtil.createHikariDataSource(dataSourceProps);
//    }

    @Bean(name = "simulationSqlSessionFactory")
    public SqlSessionFactoryBean sqlSessionFactory (@Qualifier("simulationDataSource") DataSource dataSource) throws Exception{
        final SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSource);
        ResourceLoader loader = new DefaultResourceLoader();
        sqlSessionFactoryBean.setConfigLocation(loader.getResource("classpath:sqlmap/mybatis-config.xml"));
        sqlSessionFactoryBean.setMapperLocations(ResourceUtil.resolveMapperLocation("classpath*:sqlmap/mapper/**/*.xml", "classpath*:sqlmap/mapper/*.xml"));
        return sqlSessionFactoryBean;
    }

    @Bean(name = "simulationSqlSessionTemplate")
    public SqlSessionTemplate sqlSessionTemplate (@Qualifier("simulationSqlSessionFactory") SqlSessionFactory sqlSessionFactory) throws Exception{
        return new SqlSessionTemplate(sqlSessionFactory, ExecutorType.BATCH);
    }

    @Bean(name = "simulationMyBatisTxManager")
    public PlatformTransactionManager simulationMyBatisTxManager (@Qualifier("simulationDataSource") DataSource dataSource) throws Exception{
        return new DataSourceTransactionManager(dataSource);
    }

}
