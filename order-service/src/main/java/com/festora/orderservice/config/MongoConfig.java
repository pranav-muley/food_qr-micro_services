package com.festora.orderservice.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
public class MongoConfig {
    @Value("${SPRING_DATA_MONGODB_URI:}")
    private String mongoUrl;

    public MongoClient mongoClient(){
        try {
            ConnectionString connectionString = new ConnectionString(mongoUrl);
            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(connectionString).build();

            return MongoClients.create(settings);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    @Bean
    public MongoTemplate mongoTemplate(){
        return new MongoTemplate(mongoClient(), "orders");
    }
}
