/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apibatchexport.config;

import com.soprasteria.g4it.backend.apibatchexport.listener.ExportJobListener;
import com.soprasteria.g4it.backend.apibatchexport.repository.ExportReportRepository;
import com.soprasteria.g4it.backend.apiinventory.repository.InventoryRepository;
import com.soprasteria.g4it.backend.common.filesystem.business.FileStorage;
import com.soprasteria.g4it.backend.common.filesystem.business.FileSystem;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.job.flow.support.SimpleFlow;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

/**
 * Export Batch Configuration
 */
@Configuration
public class ExportBatchConfiguration {

    public static final String EXPORT_JOB = "exportJob";

    /**
     * Evaluation Job configuration
     *
     * @param jobRepository                Spring Batch Job Repository.
     * @param extractDataToExportFlow      the extract data flow.
     * @param cleanExportWorkingFolderStep clean working folder Step.
     * @param exportJobListener            the job listener.
     * @return the configured Job.
     */
    @Bean
    public Job exportJob(final JobRepository jobRepository,
                         final Step loadSipReferentialStep,
                         final Flow extractDataToExportFlow,
                         final Step uploadExportResultStep,
                         final Step cleanExportWorkingFolderStep,
                         final ExportJobListener exportJobListener) {
        return new JobBuilder(EXPORT_JOB, jobRepository)
                .listener(exportJobListener)
                .start(loadSipReferentialStep)
                .on("*").to(extractDataToExportFlow)
                .next(uploadExportResultStep)
                .on("*").to(cleanExportWorkingFolderStep)
                .build()
                .build();
    }

    /**
     * Inject configured FileStorage in all jobs
     *
     * @param subscriber     the client subscriber.
     * @param organizationId the subscriber's organization.
     * @param fileSystem     Abstraction over local or distant filesystem
     * @return a file storage to interact with files
     */
    @Bean
    @JobScope
    public FileStorage exportFileStorage(@Value("#{jobParameters['subscriber']}") final String subscriber,
                                         @Value("#{jobParameters['organization.id']}") final Long organizationId,
                                         final FileSystem fileSystem) {
        return fileSystem.mount(subscriber, organizationId.toString());
    }

    /**
     * Job listener definition.
     *
     * @param exportReportRepository the repository to access export data.
     * @param inventoryRepository    the repository to access inventory data.
     * @return the configured listener.
     */
    @Bean
    public ExportJobListener exportJobListener(final ExportReportRepository exportReportRepository,
                                               final InventoryRepository inventoryRepository) {
        return new ExportJobListener(exportReportRepository, inventoryRepository);
    }

    /**
     * Parallel Data extraction Flow.
     * // @param inventoryToExportExtractionFlow                  the flow to extract Inventory data.
     *
     * @param dataCenterToExportExtractionFlow                 the flow to extract DataCenter data.
     * @param physicalEquipmentToExportExtractionFlow          the flow to extract PhysicalEquipment data.
     * @param virtualEquipmentToExportExtractionFlow           the flow to extract VirtualEquipment data.
     * @param applicationToExportExtractionFlow                the flow to extract Application data.
     * @param physicalEquipmentIndicatorToExportExtractionFlow the flow to extract Physical Equipment data.
     * @param virtualEquipmentIndicatorToExportExtractionFlow  the flow to extract Virtual Equipment data.
     * @param applicationIndicatorToExportExtractionFlow       the flow to extract Application data.
     * @return the configured Flow.
     */
    @Bean
    public Flow extractDataToExportFlow(
            //skip export of inventory file
            //final Flow inventoryToExportExtractionFlow,
            final Flow dataCenterToExportExtractionFlow,
            final Flow physicalEquipmentToExportExtractionFlow,
            final Flow virtualEquipmentToExportExtractionFlow,
            final Flow applicationToExportExtractionFlow,
            final Flow physicalEquipmentIndicatorToExportExtractionFlow,
            final Flow virtualEquipmentIndicatorToExportExtractionFlow,
            final Flow applicationIndicatorToExportExtractionFlow) {
        return new FlowBuilder<SimpleFlow>("extractDataToExportFlow")
                .split(extractDataToExportTaskExecutor())
                .add(
                        // inventoryToExportExtractionFlow,
                        dataCenterToExportExtractionFlow,
                        physicalEquipmentToExportExtractionFlow,
                        virtualEquipmentToExportExtractionFlow,
                        applicationToExportExtractionFlow,
                        physicalEquipmentIndicatorToExportExtractionFlow,
                        virtualEquipmentIndicatorToExportExtractionFlow,
                        applicationIndicatorToExportExtractionFlow)
                .build();
    }

    /**
     * Parallel Task Executor Configuration.
     *
     * @return the configured taskExecutor.
     */
    @Bean
    public TaskExecutor extractDataToExportTaskExecutor() {
        return new SimpleAsyncTaskExecutor("extractDataToExportTaskExecutor");
    }

    /**
     * Async Job Launcher.
     *
     * @param jobRepository the Spring JobRepository.
     * @return the configured async job launcher.
     * @throws Exception when errors occurs.
     */
    @Bean
    public JobLauncher asyncExportJobLauncher(final JobRepository jobRepository) throws Exception {
        final TaskExecutorJobLauncher jobLauncher = new TaskExecutorJobLauncher();
        jobLauncher.setJobRepository(jobRepository);
        jobLauncher.setTaskExecutor(new SimpleAsyncTaskExecutor());
        jobLauncher.afterPropertiesSet();
        return jobLauncher;
    }

}
