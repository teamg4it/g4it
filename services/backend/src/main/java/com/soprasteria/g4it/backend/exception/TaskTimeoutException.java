/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.exception;

/**
 * Exception thrown when a task execution exceeds the configured timeout period.
 */
public class TaskTimeoutException extends RuntimeException {

    public TaskTimeoutException(String message) {
        super(message);
    }

    public TaskTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}

