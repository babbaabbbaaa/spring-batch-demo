package com.rocker.controller;


import com.rocker.launcher.RemoteChunkingLauncher;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class JobLaunchController {


    @Resource
    private RemoteChunkingLauncher launcher;

    @GetMapping("send")
    public String send() {
        return launcher.launchJob(new JobParametersBuilder()
                .addString("source_file_path", "")
                .addLong("chunk_size", 1000L)
                .toJobParameters());
    }
}
