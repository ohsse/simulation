package com.hscmt.common.config;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.hscmt.common.enumeration.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {
    @Bean
    public SimpleModule enumModule() {
        SimpleModule module = new SimpleModule();

        /* 계측주기 deserializer 연결 */
        module.addDeserializer(CycleCd.class, new EnumDeserializer<>(CycleCd.class));
        /* YN deserializer 연결 */
        module.addDeserializer(YesOrNo.class, new EnumDeserializer<>(YesOrNo.class));
        /* FeatureType Deserializer */
        module.addDeserializer(FeatureType.class, new EnumDeserializer<>(FeatureType.class));
        /* conditionType deserializer */
        module.addDeserializer(ConditionType.class, new EnumDeserializer<>(ConditionType.class));
        /* direction 방향 */
        module.addDeserializer(DirectionType.class, new EnumDeserializer<>(DirectionType.class));

        return module;
    }
}
