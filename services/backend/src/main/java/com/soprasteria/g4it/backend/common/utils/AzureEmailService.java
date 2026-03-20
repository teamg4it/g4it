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
import com.azure.core.util.polling.PollerFlux;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Service
@Slf4j
public class AzureEmailService {

    private final EmailAsyncClient emailClient;
    private final String senderEmail;

    public AzureEmailService(@Value("${azure.communication.connection-string}") String connectionString,
                             @Value("${azure.communication.sender-email}") String senderEmail) {
        this.emailClient = new EmailClientBuilder()
                .connectionString(connectionString)
                .buildAsyncClient();
        this.senderEmail= senderEmail;
    }

    public void sendEmail(String recipient, String subject, String body) {
        EmailMessage emailMessage = new EmailMessage()
                .setSenderAddress(senderEmail)
                .setToRecipients(recipient)
                .setSubject(subject)
                .setBodyPlainText(body)
                .setBodyHtml(body);

        PollerFlux<EmailSendResult, EmailSendResult> poller = emailClient.beginSend(emailMessage);
        try (ExecutorService executorService = Executors.newSingleThreadExecutor()) {
            executorService.submit(() -> poller.subscribe(
                    this::handleAsyncPollerResponse,
                    error -> log.error("Error occurred while sending email: {}", error.getMessage())
            ));
            try {
                Thread.sleep(Duration.ofSeconds(30));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Thread was interrupted while waiting for email send operation.", e);
            }
        }
    }

    private void handleAsyncPollerResponse(AsyncPollResponse<EmailSendResult, EmailSendResult> response) {
        if (response.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED) {
            log.info("Successfully sent the email operation id: {}", response.getValue().getId());
        } else {
            log.info("Email send status: {} , operation id: {}", response.getStatus(), response.getValue().getId());
        }
    }
}
