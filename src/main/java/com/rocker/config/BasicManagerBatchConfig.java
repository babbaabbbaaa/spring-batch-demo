package com.rocker.config;


import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.jms.dsl.Jms;

@Configuration
public class BasicManagerBatchConfig {


    @Bean
    public IntegrationFlow writeRequests(ActiveMQConnectionFactory connectionFactory, DirectChannel requests) {
        return IntegrationFlows.from(requests)
                .handle(Jms.outboundAdapter(connectionFactory).destination("requests"))
                .get();
    }

    @Bean
    public IntegrationFlow readReplies(ActiveMQConnectionFactory connectionFactory, QueueChannel replies) {
        return IntegrationFlows.from(Jms.messageDrivenChannelAdapter(connectionFactory).destination("replies").autoStartup(false))
                .channel(replies)
                .get();
    }

}
