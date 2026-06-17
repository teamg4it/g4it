/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiloadinputfiles.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;

public final class CsvReaderUtils {

    private static final Charset FALLBACK_CHARSET =
            Charset.forName("windows-1252");

    private CsvReaderUtils() {
    }

    public static <T> T execute(
            Path path,
            Function<BufferedReader, T> operation)
            throws IOException {

        Charset charset = detectCharset(path);

        try {
            return executeWithCharset(path, charset, operation);

        } catch (MalformedInputException e) {

            // Fallback only when charset detection defaults to UTF-8.
            // This covers both UTF-8 files and Windows-1252 files without BOM.
            boolean bomDetected =
                    StandardCharsets.UTF_16LE.equals(charset)
                            || StandardCharsets.UTF_16BE.equals(charset);
            if (bomDetected) {
                throw e;
            }

            return executeWithCharset(
                    path,
                    FALLBACK_CHARSET,
                    operation
            );
        }
    }

    private static Charset detectCharset(Path path) throws IOException {

        try (InputStream is = Files.newInputStream(path)) {

            byte[] bom = new byte[4];
            int bytesRead = is.read(bom);
            // UTF-32 BOMs are not supported.
            if (bytesRead >= 4) {

                if ((bom[0] & 0xFF) == 0xFF &&
                        (bom[1] & 0xFF) == 0xFE &&
                        bom[2] == 0x00 &&
                        bom[3] == 0x00) {
                    throw new IOException(
                            Constants.UNSUPPORTED_UTF32_ERROR);
                }

                if (bom[0] == 0x00 &&
                        bom[1] == 0x00 &&
                        (bom[2] & 0xFF) == 0xFE &&
                        (bom[3] & 0xFF) == 0xFF) {
                    throw new IOException(Constants.UNSUPPORTED_UTF32_ERROR);
                }
            }
            if (bytesRead >= 2) {

                if ((bom[0] & 0xFF) == 0xFF &&
                        (bom[1] & 0xFF) == 0xFE) {
                    return StandardCharsets.UTF_16LE;
                }

                if ((bom[0] & 0xFF) == 0xFE &&
                        (bom[1] & 0xFF) == 0xFF) {
                    return StandardCharsets.UTF_16BE;
                }
            }

            if (bytesRead >= 3 &&
                    (bom[0] & 0xFF) == 0xEF &&
                    (bom[1] & 0xFF) == 0xBB &&
                    (bom[2] & 0xFF) == 0xBF) {

                return StandardCharsets.UTF_8;
            }

            return StandardCharsets.UTF_8;
        }
    }

    private static <T> T executeWithCharset(
            Path path,
            Charset charset,
            Function<BufferedReader, T> operation)
            throws IOException {

        try (BufferedReader reader =
                     Files.newBufferedReader(path, charset)) {

            skipBom(reader);
            return operation.apply(reader);

        } catch (UncheckedIOException e) {
            IOException cause = e.getCause();

            if (cause != null) {
                throw cause;
            }

            throw e;
        }
    }

    private static void skipBom(BufferedReader reader)
            throws IOException {

        reader.mark(1);

        if (reader.read() != '\uFEFF') {
            reader.reset();
        }
    }
}