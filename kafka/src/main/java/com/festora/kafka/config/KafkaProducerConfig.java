package com.festora.kafka.config;

import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
public class KafkaProducerConfig {

    @Bean
    public ProducerFactory<String, String> producerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.putIfAbsent("bootstrap.servers", "localhost:9092");
        props.putIfAbsent("group_id", "group_id");
        props.putIfAbsent("key.serializer", StringSerializer.class);
        props.putIfAbsent("value.serializer", StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, String> KafkaTemplate() {
        return new KafkaTemplate<>(producerConfigs());
    }
}
