package com.hscmt.simulation.common.config.jdbc;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
public class SimulationJdbcConfig {

    @Bean(name = "simulationJdbcTemplate")
    public JdbcTemplate simulationJdbcTemplate (@Qualifier("simulationDataSource") DataSource dataSource) throws Exception
    {
        return new JdbcTemplate(dataSource);
    }

    @Bean(name = "simulationJdbcTxManager")
    public PlatformTransactionManager simulationJdbcTxManager (@Qualifier("simulationDataSource") DataSource dataSource) throws Exception{
        return new DataSourceTransactionManager(dataSource);
    }
}
