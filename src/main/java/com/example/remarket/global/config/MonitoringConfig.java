package com.example.remarket.global.config;

import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.config.MeterFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MonitoringConfig {

    @Bean
    public MeterFilter commonTagMeterFilter() {
        return MeterFilter.commonTags(
                Tags.of("application", "re-market")
                        .and("env", "dev")
        );
    }

}
