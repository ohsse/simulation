package com.hscmt.waternet.config;

import com.hscmt.common.props.DataSourceProps;
import com.hscmt.common.props.JpaProps;
import com.hscmt.common.util.DatabaseUtil;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import javax.sql.DataSource;
import java.util.Objects;

@Configuration
@EnableConfigurationProperties
@EnableJpaRepositories(
        basePackages = {"com.hscmt.waternet"},
        entityManagerFactoryRef = "waternetEntityManagerFactory",
        transactionManagerRef = "waternetTransactionManager"
)
public class WaternetJpaConfig {

//
    @Bean(name = "waternetDataSourceProps")
    @ConfigurationProperties(prefix = "spring.datasource.waternet")
    public DataSourceProps dataSourceProps() {
        return new DataSourceProps();
    }

    @Bean(name = "waternetJpaProps")
    @ConfigurationProperties(prefix = "spring.jpa.waternet")
    public JpaProps jpaProps() {
        return new JpaProps();
    }

    @Bean(name = "waternetDataSource")
    public DataSource waternetDataSource(@Qualifier("waternetDataSourceProps") DataSourceProps props) {
        props.setDriverClassName("com.tmax.tibero.jdbc.TbDriver");
        return DatabaseUtil.createP6DataSource(props);
    }

    @Bean(name = "waternetEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactoryBean(
            @Qualifier("waternetDataSource") DataSource dataSource,
            @Qualifier("waternetJpaProps") JpaProps props,
            EntityManagerFactoryBuilder builder
    ) {
        return builder
                .dataSource(dataSource)
                .packages("com.hscmt.waternet")
                .persistenceUnit("waternetEntityManager")
                .properties(props.getProperties())
                .build();
    }

    @Bean(name = "waternetTransactionManager")
    public JpaTransactionManager transactionManager(
            @Qualifier("waternetEntityManagerFactory") LocalContainerEntityManagerFactoryBean entityManagerFactoryBean
    ) {
        return new JpaTransactionManager(Objects.requireNonNull(entityManagerFactoryBean.getObject()));
    }
}
