/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.common.utils;

import com.azure.communication.email.EmailAsyncClient;
import com.azure.communication.email.EmailClientBuilder;
import com.azure.communication.email.models.EmailMessage;
import com.azure.communication.email.models.EmailSendResult;
import com.azure.core.util.polling.AsyncPollResponse;
import com.azure.core.util.polling.LongRunningOperationStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class AzureEmailService {

    private final EmailAsyncClient emailClient;
    private final String senderEmail;
    private final TaskExecutor taskExecutor;

    public AzureEmailService(@Value("${azure.communication.connection-string}") String connectionString,
                             @Value("${azure.communication.sender-email}") String senderEmail, TaskExecutor taskExecutor) {
        this.emailClient = new EmailClientBuilder()
                .connectionString(connectionString)
                .buildAsyncClient();
        this.senderEmail= senderEmail;
        this.taskExecutor = taskExecutor;
    }

    public void sendEmail(String recipient, String subject, String body) {
        EmailMessage emailMessage = new EmailMessage()
                .setSenderAddress(senderEmail)
                .setToRecipients(recipient)
                .setSubject(subject)
                .setBodyPlainText(body)
                .setBodyHtml(body);
        taskExecutor.execute(() -> {
            emailClient.beginSend(emailMessage)
                    .last()
                    .doOnSuccess(this::handleAsyncPollerResponse)
                    .doOnError(error -> log.error("Error occurred while sending email to recipient [{}]: {}", recipient, error.getMessage(), error))
                    .subscribe();
        });
    }

    private void handleAsyncPollerResponse(AsyncPollResponse<EmailSendResult, EmailSendResult> response) {
        if (response.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED) {
            log.info("Successfully sent the email operation id: {}", response.getValue().getId());
        } else {
            log.info("Email send status: {} , operation id: {}", response.getStatus(), response.getValue().getId());
        }
    }
}
