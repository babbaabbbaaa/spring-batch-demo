package com.rocker.launcher;

import com.rocker.reader.FileItemReader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.batch.integration.chunk.RemoteChunkingManagerStepBuilderFactory;
import org.springframework.batch.item.file.separator.SimpleRecordSeparatorPolicy;
import org.springframework.batch.item.support.PassThroughItemProcessor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.dsl.StandardIntegrationFlow;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

@Slf4j
@Service
public class RemoteChunkingLauncher {

    @Resource
    private RemoteChunkingManagerStepBuilderFactory managerStepBuilderFactory;
    @Resource
    private JobBuilderFactory jobBuilderFactory;
    @Resource
    private JobLauncher jobLauncher;
    @Resource
    private StandardIntegrationFlow readReplies;
    @Resource
    private QueueChannel replies;
    @Resource
    private DirectChannel requests;

    public String launchJob(JobParameters jobParameters) {
        try {
            //5. prepare remote chunking step
            if (!readReplies.isRunning()) {
                readReplies.start();
            }
            Long chunkSize = jobParameters.getLong("chunk_size");
            if (null == chunkSize) {
                chunkSize = 1000L;
            }
            TaskletStep step = managerStepBuilderFactory.get("remoteChunkingStep")
                    .chunk(chunkSize.intValue())
                    .reader(itemReader(jobParameters.getString("source_file_path")))
                    .processor(new PassThroughItemProcessor<>())
                    .inputChannel(replies)
                    .outputChannel(requests)
                    .allowStartIfComplete(true).build();
            Job job = jobBuilderFactory.get("remoteJob")
                    .incrementer(new RunIdIncrementer())
                    .start(step)
                    .build();
            return jobLauncher.run(job, new JobParametersBuilder().addDate("executionDate", new Date()).toJobParameters()).toString();
        } catch (JobInstanceAlreadyCompleteException | JobExecutionAlreadyRunningException | JobParametersInvalidException | JobRestartException e) {
            log.error("Job executed failed: ", e);
        } finally {
            if (readReplies.isRunning()) {
                readReplies.stop();
            }
        }

        return "FAILED";
    }

    private FileItemReader<String> itemReader(String filename) {
        FileItemReader<String> itemReader = new FileItemReader<>();
        itemReader.setRecordSeparatorPolicy(new SimpleRecordSeparatorPolicy() {
            @Override
            public boolean isEndOfRecord(String line) {
                return line.startsWith("T");
            }

            @Override
            public String preProcess(String line) {
                return super.preProcess(line) + "\n";
            }
        });
        itemReader.setResource(new FileSystemResource(filename));
        itemReader.setLineMapper((line, lineNumber) -> line);
        itemReader.afterPropertiesSet();
        return itemReader;
    }
}
