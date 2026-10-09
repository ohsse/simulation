package com.hscmt.simulation.common.config.jpa;

import com.hscmt.common.props.DataSourceProps;
import com.hscmt.common.props.JpaProps;
import com.hscmt.common.util.DatabaseUtil;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import javax.sql.DataSource;
import java.util.Objects;

@Configuration
@EnableConfigurationProperties
@EnableJpaRepositories(
        basePackages = {"com.hscmt.simulation"},
        entityManagerFactoryRef = "simulationEntityManagerFactory",
        transactionManagerRef = "simulationTransactionManager"
)
public class SimulationJpaConfig {
    @Primary
    @Bean(name = "simulationDataSourceProps")
    @ConfigurationProperties(prefix = "spring.datasource.simulation")
    public DataSourceProps dataSourceProps() {
        return new DataSourceProps();
    }

    @Primary
    @Bean(name = "simulationJpaProps")
    @ConfigurationProperties(prefix = "spring.jpa.simulation")
    public JpaProps jpaProps() {
        return new JpaProps();
    }

    @Primary
    @Bean(name = "simulationDataSource")
    public DataSource dataSource(@Qualifier("simulationDataSourceProps") DataSourceProps dataSourceProps) {
        return DatabaseUtil.createP6DataSource(dataSourceProps);
    }

    @Primary
    @Bean(name = "simulationEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory (
            @Qualifier("simulationDataSource") DataSource dataSource,
            @Qualifier("simulationJpaProps") JpaProps jpaProps,
            EntityManagerFactoryBuilder builder
    ) {
        return builder
                .dataSource(dataSource)
                .packages("com.hscmt.simulation")
                .persistenceUnit("simulationEntityManager")
                .properties(jpaProps.getProperties())
                .build();
    }

    @Primary
    @Bean(name = "simulationTransactionManager")
    public JpaTransactionManager transactionManager (
            @Qualifier("simulationEntityManagerFactory") LocalContainerEntityManagerFactoryBean entityManagerFactory
    ) {
        return new JpaTransactionManager(Objects.requireNonNull(entityManagerFactory.getObject()));
    }
}
