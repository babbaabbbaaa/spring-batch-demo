package com.rocker.config.pubsub;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.jms.dsl.Jms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

@Configuration
public class PubSubConfig {

    @Value("${spring.activemq.requests.topic}")
    private String requestTopic;
    @Value("${spring.activemq.replies.topic}")
    private String replyTopic;

    @Bean
    public IntegrationFlow writeRequestsWithPubSub(ActiveMQConnectionFactory connectionFactory, DirectChannel requests) {
        return IntegrationFlows.from(requests)
                .handle(Jms.publishSubscribeChannel(connectionFactory).destination(requestTopic))
                .get();
    }

    @Bean
    public IntegrationFlow readRepliesWithPubSub(ActiveMQConnectionFactory connectionFactory, QueueChannel replies) {
        return IntegrationFlows.from(Jms.publishSubscribeChannel(connectionFactory).destination(replyTopic).autoStartup(false))
                .channel(replies)
                .get();
    }


    @Bean
    public JmsListenerContainerFactory<DefaultMessageListenerContainer> jmsListenerContainerFactory(ActiveMQConnectionFactory connectionFactory) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setPubSubDomain(true);
        return factory;
    }

}
