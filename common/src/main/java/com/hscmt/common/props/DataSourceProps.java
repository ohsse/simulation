package com.hscmt.common.props;

import lombok.Data;

@Data
public class DataSourceProps {
    private String jdbcUrl;
    private String username;
    private String password;
    private String driverClassName;
    private int maximumPoolSize;
    private int minimumIdle;
    private long connectionTimeout;
    private long validationTimeout;
    private long idleTimeout;
    private long maxLifetime;
    private long keepaliveTime;
    private long initializationFailTimeout;
}
