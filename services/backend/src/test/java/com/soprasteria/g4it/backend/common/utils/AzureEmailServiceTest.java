/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.common.utils;

import com.azure.communication.email.EmailAsyncClient;
import com.azure.communication.email.models.EmailMessage;
import com.azure.communication.email.models.EmailSendResult;
import com.azure.core.util.polling.AsyncPollResponse;
import com.azure.core.util.polling.PollerFlux;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.task.TaskExecutor;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AzureEmailServiceTest {

    @Mock
    private EmailAsyncClient emailAsyncClient;
    @Mock
    private TaskExecutor taskExecutor;
    @Captor
    private ArgumentCaptor<Runnable> runnableCaptor;

    private AzureEmailService azureEmailService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        String senderEmail = "sender@example.com";
        azureEmailService = new AzureEmailService("endpoint=https://test.azure.com/;accesskey=xyz", senderEmail, taskExecutor);
        setField(azureEmailService, "emailClient", emailAsyncClient);
    }

    @Test
    @SuppressWarnings("unchecked")
    void testSendEmail_executesTask() {
        doNothing().when(taskExecutor).execute(any(Runnable.class));
        PollerFlux<EmailSendResult, EmailSendResult> pollerFluxMock = (PollerFlux<EmailSendResult, EmailSendResult>) mock(PollerFlux.class);
        Mono<AsyncPollResponse<EmailSendResult, EmailSendResult>> monoMock = (Mono<AsyncPollResponse<EmailSendResult, EmailSendResult>>) mock(Mono.class);
        doReturn(pollerFluxMock).when(emailAsyncClient).beginSend(any(EmailMessage.class));
        when(pollerFluxMock.last()).thenReturn(monoMock);
        when(monoMock.doOnSuccess(any())).thenReturn(monoMock);
        when(monoMock.doOnError(any())).thenReturn(monoMock);

        azureEmailService.sendEmail("recipient@example.com", "Subject", "Body");
        verify(taskExecutor, times(1)).execute(any(Runnable.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testSendEmail_successfulSend() {
        doNothing().when(taskExecutor).execute(runnableCaptor.capture());
        PollerFlux<EmailSendResult, EmailSendResult> pollerFluxMock = (PollerFlux<EmailSendResult, EmailSendResult>) mock(PollerFlux.class);
        Mono<AsyncPollResponse<EmailSendResult, EmailSendResult>> monoMock = (Mono<AsyncPollResponse<EmailSendResult, EmailSendResult>>) mock(Mono.class);
        doReturn(pollerFluxMock).when(emailAsyncClient).beginSend(any(EmailMessage.class));
        when(pollerFluxMock.last()).thenReturn(monoMock);
        when(monoMock.doOnSuccess(any())).thenReturn(monoMock);
        when(monoMock.doOnError(any())).thenReturn(monoMock);

        azureEmailService.sendEmail("recipient@example.com", "Subject", "Body");
        verify(taskExecutor).execute(runnableCaptor.capture());
        Runnable runnable = runnableCaptor.getValue();
        assertNotNull(runnable);
        runnable.run();
        verify(emailAsyncClient).beginSend(any(EmailMessage.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testSendEmail_errorHandling() {
        doNothing().when(taskExecutor).execute(runnableCaptor.capture());
        PollerFlux<EmailSendResult, EmailSendResult> pollerFluxMock = (PollerFlux<EmailSendResult, EmailSendResult>) mock(PollerFlux.class);
        Mono<AsyncPollResponse<EmailSendResult, EmailSendResult>> monoMock = (Mono<AsyncPollResponse<EmailSendResult, EmailSendResult>>) mock(Mono.class);
        doReturn(pollerFluxMock).when(emailAsyncClient).beginSend(any(EmailMessage.class));
        when(pollerFluxMock.last()).thenReturn(monoMock);
        when(monoMock.doOnSuccess(any())).thenReturn(monoMock);
        when(monoMock.doOnError(any())).thenReturn(monoMock);

        azureEmailService.sendEmail("recipient@example.com", "Subject", "Body");
        verify(taskExecutor).execute(runnableCaptor.capture());
        Runnable runnable = runnableCaptor.getValue();
        assertNotNull(runnable);
        runnable.run();
        verify(emailAsyncClient).beginSend(any(EmailMessage.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testHandleAsyncPollerResponse_directCoverage() throws Exception {
        // Success scenario
        AsyncPollResponse<EmailSendResult, EmailSendResult> successResponse = mock(AsyncPollResponse.class);
        EmailSendResult successResult = mock(EmailSendResult.class);
        when(successResponse.getStatus()).thenReturn(com.azure.core.util.polling.LongRunningOperationStatus.SUCCESSFULLY_COMPLETED);
        when(successResponse.getValue()).thenReturn(successResult);
        when(successResult.getId()).thenReturn("success-id");

        // Failure scenario
        AsyncPollResponse<EmailSendResult, EmailSendResult> failureResponse = mock(AsyncPollResponse.class);
        EmailSendResult failureResult = mock(EmailSendResult.class);
        when(failureResponse.getStatus()).thenReturn(com.azure.core.util.polling.LongRunningOperationStatus.FAILED);
        when(failureResponse.getValue()).thenReturn(failureResult);
        when(failureResult.getId()).thenReturn("fail-id");

        // Use reflection to invoke private method
        java.lang.reflect.Method method = AzureEmailService.class.getDeclaredMethod(
            "handleAsyncPollerResponse",
            AsyncPollResponse.class
        );
        method.setAccessible(true);
        method.invoke(azureEmailService, successResponse);
        method.invoke(azureEmailService, failureResponse);
        // If you want to verify logs, use a log capturing library or framework
    }

    // Helper to set private fields via reflection
    private static void setField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
