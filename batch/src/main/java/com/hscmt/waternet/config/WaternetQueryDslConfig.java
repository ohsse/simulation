package com.hscmt.waternet.config;

import com.querydsl.jpa.JPQLTemplates;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WaternetQueryDslConfig {
    @PersistenceContext(unitName = "waternetEntityManager")
    private EntityManager entityManager;

    @Bean(name = "waternetQueryFactory")
    public JPAQueryFactory queryFactory() {return new JPAQueryFactory(JPQLTemplates.DEFAULT,entityManager);}
}
