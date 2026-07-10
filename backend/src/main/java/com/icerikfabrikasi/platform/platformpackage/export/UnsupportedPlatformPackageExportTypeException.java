package com.icerikfabrikasi.platform.platformpackage.export;

import com.icerikfabrikasi.platform.common.exception.ValidationException;

/**
 * FP-005: Desteklenmeyen export tipi istendiğinde fırlatılır.
 *
 * <p>GlobalExceptionHandler tarafından HTTP 400 Bad Request olarak döndürülür.</p>
 */
public class UnsupportedPlatformPackageExportTypeException extends ValidationException {

    public UnsupportedPlatformPackageExportTypeException(String requestedType) {
        super(String.format(
                "Desteklenmeyen export tipi: %s. Desteklenen tipler: " +
                "MARKDOWN_BRIEF, JSON_PACKAGE, COPY_TEXT, PRODUCTION_BRIEF",
                requestedType
        ));
    }
}
