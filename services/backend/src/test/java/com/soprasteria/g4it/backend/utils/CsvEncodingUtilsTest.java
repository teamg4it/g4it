/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.utils;

import com.soprasteria.g4it.backend.common.utils.CsvEncodingUtils;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class CsvEncodingUtilsTest {

    @Test
    void getReader_utf8_default() throws IOException {
        String content = "hello\nworld";
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);

        BufferedReader reader = CsvEncodingUtils.getReader(bytes);

        assertEquals("hello", reader.readLine());
        assertEquals("world", reader.readLine());
    }

    @Test
    void getReader_unknownEncoding_fallbackToUtf8() throws IOException {
        byte[] bytes = "simple text".getBytes(StandardCharsets.UTF_8);

        BufferedReader reader = CsvEncodingUtils.getReader(bytes);

        assertEquals("simple text", reader.readLine());
    }

    @Test
    void getReader_isoEncoding_convertedToWindows1252() throws IOException {
        byte[] bytes = "€ test".getBytes(Charset.forName("Windows-1252"));

        BufferedReader reader = CsvEncodingUtils.getReader(bytes);

        String line = reader.readLine();
        assertNotNull(line);
        assertTrue(line.contains("€"));
    }

    @Test
    void getReader_withBom_shouldStripBom() throws IOException {
        byte[] bomUtf8 = new byte[] {(byte)0xEF, (byte)0xBB, (byte)0xBF};
        byte[] content = "hello".getBytes(StandardCharsets.UTF_8);

        byte[] bytes = new byte[bomUtf8.length + content.length];
        System.arraycopy(bomUtf8, 0, bytes, 0, bomUtf8.length);
        System.arraycopy(content, 0, bytes, bomUtf8.length, content.length);

        BufferedReader reader = CsvEncodingUtils.getReader(bytes);

        assertEquals("hello", reader.readLine());
    }

    @Test
    void detectCharset_invalidEncoding_fallbackToUtf8() {
        byte[] bytes = new byte[]{0x00, 0x01, 0x02};

        // indirect call (method is private)
        BufferedReader reader = assertDoesNotThrow(() -> CsvEncodingUtils.getReader(bytes));

        assertNotNull(reader);
    }

    @Test
    void getReader_emptyInput() throws IOException {
        byte[] bytes = new byte[0];

        BufferedReader reader = CsvEncodingUtils.getReader(bytes);

        assertNull(reader.readLine());
    }
}