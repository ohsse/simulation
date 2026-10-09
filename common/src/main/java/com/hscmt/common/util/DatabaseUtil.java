package com.hscmt.common.util;


import com.hscmt.common.props.DataSourceProps;
import com.p6spy.engine.spy.P6DataSource;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

public class DatabaseUtil {

    public static DataSource createP6DataSource (DataSourceProps dataSourceProps) {
        return new P6DataSource(createHikariDataSource(dataSourceProps));
    }

    public static DataSource createHikariDataSource (DataSourceProps dataSourceProps) {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(dataSourceProps.getJdbcUrl());
        dataSource.setUsername(dataSourceProps.getUsername());
        dataSource.setPassword(dataSourceProps.getPassword());
        dataSource.setDriverClassName(dataSourceProps.getDriverClassName());
        dataSource.setMaximumPoolSize(dataSourceProps.getMaximumPoolSize());
        dataSource.setMinimumIdle(dataSourceProps.getMinimumIdle());
        dataSource.setConnectionTimeout(dataSourceProps.getConnectionTimeout());
        dataSource.setValidationTimeout(dataSourceProps.getValidationTimeout());
        dataSource.setIdleTimeout(dataSourceProps.getIdleTimeout());
        dataSource.setMaxLifetime(dataSourceProps.getMaxLifetime());
        dataSource.setKeepaliveTime(dataSourceProps.getKeepaliveTime());
        dataSource.setInitializationFailTimeout(dataSourceProps.getInitializationFailTimeout());

        return dataSource;
    }
}
