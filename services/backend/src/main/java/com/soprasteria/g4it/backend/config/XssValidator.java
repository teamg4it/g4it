/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.config;

import com.soprasteria.g4it.backend.exception.G4itRestException;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

public class XssValidator {
    private XssValidator() {}

    public static String validate(String value) {
        if (value == null) return null;

        String cleaned = Jsoup.clean(value, Safelist.none());

        if (!cleaned.equals(value)) {
            //System.out.println("INFO :: invalid input json content exception config ");
            throw new G4itRestException("400", "Invalid input detected. Script or HTML content is not allowed");
        }
        return value;
    }
}
