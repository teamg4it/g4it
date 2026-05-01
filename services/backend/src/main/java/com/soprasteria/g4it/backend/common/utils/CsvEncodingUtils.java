/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.common.utils;

import org.mozilla.universalchardet.UniversalDetector;
import org.apache.commons.io.input.BOMInputStream;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class CsvEncodingUtils {

    public static BufferedReader getReader(byte[] bytes) throws IOException {

        Charset charset = detectCharset(bytes);

        // 🔥 Normalize common lies from detector
        if (charset.name().equalsIgnoreCase("ISO-8859-1")) {
            charset = Charset.forName("Windows-1252");
        }

        return new BufferedReader(
                new InputStreamReader(
                        new BOMInputStream(new ByteArrayInputStream(bytes)),
                        charset
                )
        );
    }

    private static Charset detectCharset(byte[] bytes) {

        UniversalDetector detector = new UniversalDetector(null);
        detector.handleData(bytes, 0, bytes.length);
        detector.dataEnd();

        String encoding = detector.getDetectedCharset();

        if (encoding == null) {
            return StandardCharsets.UTF_8; // safe fallback
        }

        try {
            return Charset.forName(encoding);
        } catch (Exception e) {
            return StandardCharsets.UTF_8;
        }
    }
}
