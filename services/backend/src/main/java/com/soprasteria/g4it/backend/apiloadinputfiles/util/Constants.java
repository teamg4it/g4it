/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiloadinputfiles.util;

public class Constants {

    private Constants() {}

    public static final int MAX_ROWS = 100_000;
    public static final String READ_CSV_ERROR = "Unable to read imported CSV file.";
    public static final String BLANK_EXCEL_MSG = "Excel file does not contain any sheet.";
    public static final String VALIDATION_IMPORT_CSV_ERROR = "Unable to validate CSV file ";
    public static final String VALIDATION_MSG = " imported file exceeds the number of rows that the calculation system can process in a single import (100,000 rows). Please perform your import in multiple files.";
    public static final String READ_EXCEL_ERROR = "Unable to read imported Excel file.";
    public static final String VALIDATION_IMPORT_EXCEL_ERROR = "Unable to validate Excel file ";

}
