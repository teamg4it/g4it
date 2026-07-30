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

}
