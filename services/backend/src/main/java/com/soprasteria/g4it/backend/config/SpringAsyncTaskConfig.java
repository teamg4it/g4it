/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class SpringAsyncTaskConfig {

    @Bean(name = "taskExecutorSingleThreaded")
    public TaskExecutor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(0);  // Minimum number of threads in the pool
        executor.setMaxPoolSize(1);  // Maximum number of threads in the pool
        executor.setQueueCapacity(100);  // Queue capacity for pending tasks
        executor.setThreadNamePrefix("Task-");  // Prefix for thread names
        executor.setWaitForTasksToCompleteOnShutdown(true);  // Ensures tasks complete on shutdown
        executor.setAwaitTerminationSeconds(60 * 10);  // Timeout for waiting for tasks to complete
        executor.initialize();  // Initializes the thread pool
        return executor;
    }

    @Bean(name = "taskExecutorLoading")
    public TaskExecutor loadingTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // B2s-safe defaults (2 vCPU / 4 GiB): keep concurrency low to protect heap.
        // Queue is intentionally small (5) because the DB is the real persistent queue.
        // Tasks enter as TO_START in DB; dispatchPendingLoadingTasks() feeds them here every 5s.
        // Unlimited users can submit — only execution throughput is bounded.
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(20);
        executor.setThreadNamePrefix("LoadTask-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60 * 10);
        executor.initialize();
        return executor;
    }

    @Bean(name = "taskExecutorMetadataLoading")
    public TaskExecutor metadataLoadingTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // Metadata loading performs transactional DB writes, so keep parallelism bounded.
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(20);
        executor.setThreadNamePrefix("MetadataLoad-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60 * 10);
        executor.initialize();
        return executor;
    }

}
