package com.rocker.config;


import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQPrefetchPolicy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.QueueChannel;

import java.util.Collections;

@Configuration
public class BatchIntegrationConfig{



    @Bean
    public DirectChannel requests() {
        return new DirectChannel();
    }

    @Bean
    public QueueChannel replies() {
        return new QueueChannel();
    }


    @Bean
    public ActiveMQConnectionFactory connectionFactory(@Value("${spring.activemq.broker-url}") String url,
                                                       @Value("${spring.activemq.user}") String username,
                                                       @Value("${spring.activemq.password}") String password) {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
        connectionFactory.setBrokerURL(url);
        connectionFactory.setUserName(username);
        connectionFactory.setPassword(password);
        connectionFactory.setTrustAllPackages(true);
        connectionFactory.setTrustedPackages(Collections.emptyList());
        ActiveMQPrefetchPolicy policy = new ActiveMQPrefetchPolicy();
        policy.setQueuePrefetch(0);
        connectionFactory.setPrefetchPolicy(policy);
        return connectionFactory;
    }



}
