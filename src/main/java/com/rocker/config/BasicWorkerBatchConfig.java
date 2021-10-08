package com.rocker.config;


import com.rocker.module.Aggregations;
import com.rocker.writer.MongoItemWriter;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.batch.core.step.item.ChunkProcessor;
import org.springframework.batch.core.step.item.SimpleChunkProcessor;
import org.springframework.batch.integration.chunk.ChunkProcessorChunkHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.jms.dsl.Jms;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;

@Slf4j
@Configuration
public class BasicWorkerBatchConfig {

    @Resource
    private MongoTemplate mongoTemplate;


    @Bean
    public IntegrationFlow workerIntegrationFlow(ActiveMQConnectionFactory connectionFactory) {
        ChunkProcessorChunkHandler<String> handler = new ChunkProcessorChunkHandler<>();
        ChunkProcessor<String> processor = new SimpleChunkProcessor<>(item -> {
                    Aggregations aggregations = new Aggregations();
                    if (StringUtils.hasText(item)) {
                        for (String line : item.split("\n")) {
                            if (line.startsWith("H")) {
                                aggregations.initiateHeader(line);
                            }
                            if (line.startsWith("D")) {
                                aggregations.initiateDetail(line);
                            }
                            if (line.startsWith("T")) {
                                aggregations.initiateTrailer(line);
                            }
                        }
                    }
                    return aggregations;
                }, new MongoItemWriter<>(mongoTemplate));
        handler.setChunkProcessor(processor);
        return IntegrationFlows.from(Jms.messageDrivenChannelAdapter(connectionFactory).destination("requests"))
                .handle(handler, "handleChunk")
                .handle(Jms.outboundAdapter(connectionFactory).destination("replies"))
                .get();
    }

}
