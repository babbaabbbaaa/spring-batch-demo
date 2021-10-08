package com.rocker.reader;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ReaderNotOpenException;
import org.springframework.batch.item.file.*;
import org.springframework.batch.item.file.separator.RecordSeparatorPolicy;
import org.springframework.batch.item.support.AbstractItemCountingItemStreamItemReader;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
public class FileItemReader<T> extends AbstractItemCountingItemStreamItemReader<T> implements
        ResourceAwareItemReaderItemStream<T>, InitializingBean {

    private BufferedReader reader;
    private Resource resource;
    private RecordSeparatorPolicy recordSeparatorPolicy;
    private LineMapper<T> lineMapper;
    private boolean noInput = false;
    private final String encoding = StandardCharsets.UTF_8.name();
    private int lineCount = 0;
    private final BufferedReaderFactory bufferedReaderFactory = new DefaultBufferedReaderFactory();

    public FileItemReader() {
        setName(ClassUtils.getShortName(FileItemReader.class));
    }

    @Override
    protected T doRead() {
        if (noInput) {
            return null;
        }
        String line = readLine();
        if (line == null) {
            return null;
        } else {
            try {
                return lineMapper.mapLine(line, lineCount);
            } catch (Exception ex) {
                throw new FlatFileParseException("Parsing error at line: " + lineCount + " in resource=["
                        + resource.getDescription() + "], input=[" + line + "]", ex, line, lineCount);
            }
        }

    }

    @Override
    protected void doOpen() throws Exception {
        Assert.notNull(resource, "Input resource must be set");
        Assert.notNull(recordSeparatorPolicy, "RecordSeparatorPolicy must be set");

        noInput = true;
        if (!resource.exists()) {
            log.warn("Input resource does not exist " + resource.getDescription());
            return;
        }

        if (!resource.isReadable()) {
            log.warn("Input resource is not readable " + resource.getDescription());
            return;
        }

        reader = bufferedReaderFactory.create(resource, encoding);
        noInput = false;
    }

    @Override
    protected void doClose() {

    }

    @Override
    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public void setLineMapper(LineMapper<T> lineMapper) {
        this.lineMapper = lineMapper;
    }

    public void setRecordSeparatorPolicy(RecordSeparatorPolicy recordSeparatorPolicy) {
        this.recordSeparatorPolicy = recordSeparatorPolicy;
    }

    @Override
    public void afterPropertiesSet() {}

    @Nullable
    private String readLine() {
        if (reader == null) {
            throw new ReaderNotOpenException("Reader must be open before it can be read.");
        }

        String line = null;

        try {
            line = this.reader.readLine();
            if (line == null) {
                return null;
            }
            lineCount++;

            line = applyRecordSeparatorPolicy(line);
        } catch (IOException e) {
            // Prevent IOException from recurring indefinitely
            // if client keeps catching and re-calling
            noInput = true;
            throw new NonTransientFlatFileException("Unable to read from resource: [" + resource + "]", e, line != null ? line : "NULL",
                    lineCount);
        }
        return line;
    }

    private String applyRecordSeparatorPolicy(String line) throws IOException {

        String record = line;
        while (line != null && !recordSeparatorPolicy.isEndOfRecord(line)) {
            line = this.reader.readLine();
            if (line == null) {
                if (StringUtils.hasText(record)) {
                    // A record was partially complete since it hasn't ended but
                    // the line is null
                    throw new FlatFileParseException("Unexpected end of file before record complete", record, lineCount);
                } else {
                    // Record has no text but it might still be post processed
                    // to something (skipping preProcess since that was already
                    // done)
                    break;
                }
            } else {
                lineCount++;
            }
            record = recordSeparatorPolicy.preProcess(record) + line;
        }

        assert record != null;
        return recordSeparatorPolicy.postProcess(record);

    }

}
