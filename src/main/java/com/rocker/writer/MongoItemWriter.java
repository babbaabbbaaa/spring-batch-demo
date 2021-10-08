package com.rocker.writer;


import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemWriter;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Slf4j
public class MongoItemWriter<T> implements ItemWriter<T> {


    private final MongoTemplate mongoTemplate;

    public MongoItemWriter(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void write(List<? extends T> items) {
        if (!CollectionUtils.isEmpty(items)) {
            mongoTemplate.insertAll(items);
        }
    }

}
