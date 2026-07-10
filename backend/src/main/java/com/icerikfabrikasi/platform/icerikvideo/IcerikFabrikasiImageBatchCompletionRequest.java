package com.icerikfabrikasi.platform.icerikvideo;

/**
 * FP-019 Task 04: Request for image batch completion/partial marking.
 */
public final class IcerikFabrikasiImageBatchCompletionRequest {
    private boolean partial;
    private String safeCompletionNote;

    public IcerikFabrikasiImageBatchCompletionRequest() {}
    public boolean isPartial() { return partial; }
    public void setPartial(boolean v) { this.partial = v; }
    public String getSafeCompletionNote() { return safeCompletionNote; }
    public void setSafeCompletionNote(String v) { this.safeCompletionNote = v; }
}
