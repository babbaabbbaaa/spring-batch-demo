package com.rocker.config.pubsub;

import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.Message;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;

@Slf4j
@Component
public class JMSListener {


    @JmsListener(containerFactory = "jmsListenerContainerFactory", destination = "topic_requests")
    public void receive(Message message) throws JMSException {
        log.info(message.getBody(String.class));
    }
}
