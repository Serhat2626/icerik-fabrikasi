package com.icerikfabrikasi.platform.icerikvideo;

/**
 * FP-019 Task 02: Safe exception for türkü video project operations.
 *
 * <p>Never exposes tokens, credentials, stack traces, or internal data
 * in message or toString().</p>
 */
public class IcerikFabrikasiVideoProjectException extends RuntimeException {

    private final String safeErrorCode;

    public IcerikFabrikasiVideoProjectException(String message, String safeErrorCode) {
        super(message);
        this.safeErrorCode = safeErrorCode;
    }

    public String getSafeErrorCode() { return safeErrorCode; }

    @Override
    public String toString() {
        return "IcerikFabrikasiVideoProjectException{code='" + safeErrorCode + "', message='" + getMessage() + "'}";
    }

    // ── Factory methods ──

    public static IcerikFabrikasiVideoProjectException icerikFabrikasiNameRequired() {
        return new IcerikFabrikasiVideoProjectException("Türkü adı gereklidir", "TURKU_NAME_REQUIRED");
    }

    public static IcerikFabrikasiVideoProjectException icerikFabrikasiNameTooLong() {
        return new IcerikFabrikasiVideoProjectException("Türkü adı çok uzun (maks. 200 karakter)", "TURKU_NAME_TOO_LONG");
    }

    public static IcerikFabrikasiVideoProjectException unsafeInput(String fieldName) {
        return new IcerikFabrikasiVideoProjectException(
                "Güvenli olmayan girdi: " + fieldName, "UNSAFE_INPUT");
    }

    public static IcerikFabrikasiVideoProjectException fieldTooLong(String fieldName, int maxLength) {
        return new IcerikFabrikasiVideoProjectException(
                fieldName + " çok uzun (maks. " + maxLength + " karakter)", "FIELD_TOO_LONG");
    }

    public static IcerikFabrikasiVideoProjectException projectNotFound() {
        return new IcerikFabrikasiVideoProjectException("Proje bulunamadı", "PROJECT_NOT_FOUND");
    }

    public static IcerikFabrikasiVideoProjectException invalidDuration() {
        return new IcerikFabrikasiVideoProjectException(
                "Hedef süre 1-30 dakika arasında olmalıdır", "INVALID_DURATION");
    }

    public static IcerikFabrikasiVideoProjectException invalidStageTransition(
            IcerikFabrikasiVideoWorkflowStage current, IcerikFabrikasiVideoWorkflowStage target) {
        return new IcerikFabrikasiVideoProjectException(
                "Geçersiz aşama geçişi: " + current + " → " + target,
                "INVALID_STAGE_TRANSITION");
    }

    public static IcerikFabrikasiVideoProjectException missingPrerequisite(String detail) {
        return new IcerikFabrikasiVideoProjectException(
                "Ön koşul eksik: " + detail, "MISSING_PREREQUISITE");
    }
}
