package com.icerikfabrikasi.platform.icerikvideo;

/**
 * FP-024 Task 02: Config validation issue.
 * Safe issue representation — no raw paths/secrets.
 */
public final class IcerikFabrikasiRenderConfigValidationIssue {
    private final IcerikFabrikasiRenderConfigFailureCode code;
    private final String field;
    private final String safeMessage;

    public IcerikFabrikasiRenderConfigValidationIssue(IcerikFabrikasiRenderConfigFailureCode code,
                                             String field, String safeMessage) {
        this.code = code;
        this.field = field;
        this.safeMessage = safeMessage;
    }

    public IcerikFabrikasiRenderConfigFailureCode getCode() { return code; }
    public String getField() { return field; }
    public String getSafeMessage() { return safeMessage; }
}
