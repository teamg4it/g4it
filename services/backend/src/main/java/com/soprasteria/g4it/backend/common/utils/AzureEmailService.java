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
import com.azure.core.util.polling.LongRunningOperationStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class AzureEmailService {

    private final EmailAsyncClient emailClient;

    public AzureEmailService(@Value("${azure.communication.connection-string}") String connectionString) {
        this.emailClient = new EmailClientBuilder()
                .connectionString(connectionString)
                .buildAsyncClient();
    }

    public void sendEmail(String recipient, String subject, String body) {
        EmailMessage emailMessage = new EmailMessage()
                .setSenderAddress("DoNotReply@saas-g4it.com")
                .setToRecipients(recipient)
                .setSubject(subject)
                .setBodyPlainText(body)
                .setBodyHtml(body);
        emailClient.beginSend(emailMessage).subscribe(
                response -> {
                    if (response.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED) {
                        log.info("Successfully sent the email operation id: {}", response.getValue().getId());
                    }
                    else {
                        // The operation ID can be retrieved as soon as the first response is received from the PollerFlux.
                        log.info("Email send status: {} , operation id: {}", response.getStatus(), response.getValue().getId());
                    }
                },error -> log.error("Error occurred while sending email: {}", error.getMessage())
        );
    }
}
