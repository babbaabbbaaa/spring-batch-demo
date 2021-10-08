package com.rocker.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.listener.ChunkListenerSupport;
import org.springframework.batch.core.scope.context.ChunkContext;

@Slf4j
public class RemoteChunkListener extends ChunkListenerSupport {

    @Override
    public void beforeChunk(ChunkContext context) {
        log.info("==========chunk start: {}", context);
    }

    @Override
    public void afterChunk(ChunkContext context) {
        log.info("========chunk completed: {}", context);
    }
}
