package com.icerikfabrikasi.platform.icerikvideo;

import java.util.List;

/**
 * FP-051: Safe admin-facing dry-run record summary. Metadata-only.
 */
public final class IcerikFabrikasiInternalDryRunRecordSummary {
    private final String opaqueRecordRef;
    private final String opaqueDryRunRef;
    private final String opaqueStoryboardRef;
    private final IcerikFabrikasiInternalDryRunRecordStatus status;
    private final List<IcerikFabrikasiInternalDryRunRecordIssueCode> issueCodes;
    private final int sceneCount;
    private final int generatedClipCount;
    private final int inputClipCount;
    private final boolean stitchedArtifactPresent;
    private final int durationSeconds;
    private final int fps;
    private final int width;
    private final int height;
    private final long sizeBytes;
    private final boolean internalOnly;

    public IcerikFabrikasiInternalDryRunRecordSummary(IcerikFabrikasiInternalDryRunRecord record) {
        this.opaqueRecordRef = record.getOpaqueRecordRef();
        this.opaqueDryRunRef = record.getOpaqueDryRunRef();
        this.opaqueStoryboardRef = record.getOpaqueStoryboardRef();
        this.status = record.getStatus();
        this.issueCodes = record.getIssueCodes();
        this.sceneCount = record.getSceneCount();
        this.generatedClipCount = record.getGeneratedClipCount();
        this.inputClipCount = record.getInputClipCount();
        this.stitchedArtifactPresent = record.isStitchedArtifactPresent();
        this.durationSeconds = record.getDurationSeconds();
        this.fps = record.getFps();
        this.width = record.getWidth();
        this.height = record.getHeight();
        this.sizeBytes = record.getSizeBytes();
        this.internalOnly = true;
    }

    public String getOpaqueRecordRef() { return opaqueRecordRef; }
    public String getOpaqueDryRunRef() { return opaqueDryRunRef; }
    public String getOpaqueStoryboardRef() { return opaqueStoryboardRef; }
    public IcerikFabrikasiInternalDryRunRecordStatus getStatus() { return status; }
    public List<IcerikFabrikasiInternalDryRunRecordIssueCode> getIssueCodes() { return issueCodes; }
    public int getSceneCount() { return sceneCount; }
    public int getGeneratedClipCount() { return generatedClipCount; }
    public int getInputClipCount() { return inputClipCount; }
    public boolean isStitchedArtifactPresent() { return stitchedArtifactPresent; }
    public int getDurationSeconds() { return durationSeconds; }
    public int getFps() { return fps; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public long getSizeBytes() { return sizeBytes; }
    public boolean isInternalOnly() { return internalOnly; }
}
