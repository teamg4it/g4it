/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiloadinputfiles.util;
import com.soprasteria.g4it.backend.exception.BadRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;


import static org.junit.jupiter.api.Assertions.*;
public class CsvReaderUtilsTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldFallbackToWindows1252WhenUtf8DecodingFails() throws Exception {

        Path csv = tempDir.resolve("windows1252.csv");

        Files.write(
                csv,
                "café".getBytes(Charset.forName("windows-1252"))
        );

        String result = CsvReaderUtils.execute(
                csv,
                reader -> {
                    try {
                        return reader.readLine();
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });

        assertEquals("café", result);
    }

    @Test
    void shouldReadUtf16LeFile() throws Exception {

        Path csv = tempDir.resolve("utf16le.csv");

        try (OutputStream os = Files.newOutputStream(csv)) {
            os.write(0xFF);
            os.write(0xFE);

            os.write("hello".getBytes(StandardCharsets.UTF_16LE));
        }

        String result = CsvReaderUtils.execute(
                csv,
                reader -> {
                    try {
                        return reader.readLine();
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });

        assertEquals("hello", result);
    }
    @Test
    void shouldReadUtf16BeFile() throws Exception {

        Path csv = tempDir.resolve("utf16be.csv");

        try (OutputStream os = Files.newOutputStream(csv)) {
            os.write(0xFE);
            os.write(0xFF);

            os.write("hello".getBytes(StandardCharsets.UTF_16BE));
        }

        String result = CsvReaderUtils.execute(
                csv,
                reader -> {
                    try {
                        return reader.readLine();
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });

        assertEquals("hello", result);
    }
    @Test
    void shouldRejectUtf32LeFiles() throws Exception {

        Path csv = tempDir.resolve("utf32le.csv");

        Files.write(csv, new byte[]{
                0x00,
                0x00,
                (byte) 0xFE,
                (byte) 0xFF
        });

        BadRequestException ex = assertThrows(
                BadRequestException.class,
                () -> CsvReaderUtils.execute(csv, r -> null));

        assertEquals(
                Constants.UNSUPPORTED_UTF32_ERROR,
                ex.getError());
    }
    @Test
    void shouldRejectUtf32BeFiles() throws Exception {

        Path csv = tempDir.resolve("utf32be.csv");

        Files.write(csv, new byte[]{
                0x00,
                0x00,
                (byte) 0xFE,
                (byte) 0xFF
        });

        BadRequestException ex = assertThrows(
                BadRequestException.class,
                () -> CsvReaderUtils.execute(csv, r -> null));

        assertEquals(
                Constants.UNSUPPORTED_UTF32_ERROR,
                ex.getError());
    }
    @Test
    void shouldRethrowCauseFromUncheckedIOException() throws Exception {

        Path csv = tempDir.resolve("sample.csv");

        Files.writeString(csv, "abc");

        IOException ex = assertThrows(
                IOException.class,
                () -> CsvReaderUtils.execute(
                        csv,
                        reader -> {
                            throw new UncheckedIOException(
                                    new IOException("boom"));
                        }));

        assertEquals("boom", ex.getMessage());
    }
    @Test
    void shouldSkipBom() throws Exception {

        Method method =
                CsvReaderUtils.class.getDeclaredMethod(
                        "skipBom",
                        BufferedReader.class);

        method.setAccessible(true);

        BufferedReader reader =
                new BufferedReader(
                        new StringReader("\uFEFFhello"));

        method.invoke(null, reader);

        assertEquals('h', reader.read());
    }
    @Test
    void shouldResetReaderWhenBomAbsent() throws Exception {

        Method method =
                CsvReaderUtils.class.getDeclaredMethod(
                        "skipBom",
                        BufferedReader.class);

        method.setAccessible(true);

        BufferedReader reader =
                new BufferedReader(
                        new StringReader("hello"));

        method.invoke(null, reader);

        assertEquals('h', reader.read());
    }
    @Test
    void shouldRethrowMalformedInputWhenUtf16BomDetected() throws Exception {

        Path csv = tempDir.resolve("invalidUtf16.csv");

        Files.write(csv, new byte[]{
                (byte) 0xFF,
                (byte) 0xFE,
                (byte) 0xFF
        });

        assertThrows(
                MalformedInputException.class,
                () -> CsvReaderUtils.execute(
                        csv,
                        reader -> {
                            try {
                                while (reader.read() != -1) {
                                    // force decoding
                                }
                                return null;
                            } catch (IOException e) {
                                throw new UncheckedIOException(e);
                            }
                        }));
    }
}
