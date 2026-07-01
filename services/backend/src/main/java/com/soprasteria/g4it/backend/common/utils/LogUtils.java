/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.common.utils;

import java.time.LocalDateTime;

public final class LogUtils {

    /**
     * Database VARCHAR limit for task details, errors, and filenames arrays.
     * Each array element must not exceed this length.
     */
    private static final int DB_VARCHAR_LIMIT = 255;

    private LogUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Returns a string for printing log message info.
     * The result is automatically truncated to fit within database limits.
     *
     * @param message the message
     * @return the log line, truncated to 255 characters if necessary
     */
    public static String info(final String message) {
        String formatted = String.format("%s - INFO - %s", LocalDateTime.now().format(Constants.LOCAL_DATE_TIME_FORMATTER), message);
        return truncateToDbLimit(formatted);
    }

    /**
     * Returns a string for printing log message error.
     * The result is automatically truncated to fit within database limits.
     *
     * @param message the message
     * @return the log line, truncated to 255 characters if necessary
     */
    public static String error(final String message) {
        String formatted = String.format("%s - ERROR - %s", LocalDateTime.now().format(Constants.LOCAL_DATE_TIME_FORMATTER), message);
        return truncateToDbLimit(formatted);
    }

    /**
     * Truncate a string to fit within the database column limit of 255 characters.
     * If the string is longer than the limit, it will be truncated and "..." will be appended.
     *
     * @param text the text to truncate
     * @return the truncated text, or the original if it's within limits
     */
    public static String truncateToDbLimit(String text) {
        if (text == null || text.length() <= DB_VARCHAR_LIMIT) {
            return text;
        }
        return text.substring(0, DB_VARCHAR_LIMIT - 3) + "...";
    }
}
