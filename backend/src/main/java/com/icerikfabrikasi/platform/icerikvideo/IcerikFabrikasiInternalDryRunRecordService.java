package com.icerikfabrikasi.platform.icerikvideo;

import java.time.Instant;
import java.util.*;

/**
 * FP-051: Service that creates safe audit-only dry-run records from FP-049 responses.
 * In-memory only; no persistence/migration required.
 */
public class IcerikFabrikasiInternalDryRunRecordService {

    private final List<IcerikFabrikasiInternalDryRunRecord> records = new ArrayList<>();

    /**
     * Create a dry-run record from a safe FP-049 trigger response.
     */
    public IcerikFabrikasiInternalDryRunRecord createFromDryRunResponse(
            IcerikFabrikasiInternalDryRunTriggerResponse response) {
        if (response == null) {
            return createRejectedRecord("trigger:null", List.of(IcerikFabrikasiInternalDryRunRecordIssueCode.TRIGGER_REJECTED));
        }

        IcerikFabrikasiInternalDryRunRecordStatus status = mapStatus(response);
        List<IcerikFabrikasiInternalDryRunRecordIssueCode> issues = mapIssues(response);

        var now = Instant.now();
        var record = new IcerikFabrikasiInternalDryRunRecord(
                UUID.randomUUID(),
                "dryrec:" + UUID.randomUUID().toString().substring(0, 8),
                response.getOpaqueChainRef() != null ? response.getOpaqueChainRef() : "unknown",
                response.getOpaqueStoryboardRef() != null ? response.getOpaqueStoryboardRef() : "unknown",
                "review:" + UUID.randomUUID().toString().substring(0, 8),
                status, issues,
                response.getSceneCount(),
                response.getGeneratedClipCount(),
                response.getSceneCount(), // inputClipCount = sceneCount for dry-run
                response.isStitchedArtifactPresent(),
                response.getTotalDurationSeconds(),
                response.getMaxFps(),
                response.getMaxWidth(),
                response.getMaxHeight(),
                response.getTotalSizeBytes(),
                now, now);
        records.add(record);
        return record;
    }

    /**
     * Create a rejected record for invalid/unsafe triggers.
     */
    public IcerikFabrikasiInternalDryRunRecord createRejectedRecord(
            String opaqueRef, List<IcerikFabrikasiInternalDryRunRecordIssueCode> issues) {
        var now = Instant.now();
        var record = new IcerikFabrikasiInternalDryRunRecord(
                UUID.randomUUID(),
                "dryrec:" + UUID.randomUUID().toString().substring(0, 8),
                opaqueRef, "unknown", "none",
                IcerikFabrikasiInternalDryRunRecordStatus.REJECTED_UNSAFE,
                issues, 0, 0, 0, false, 0, 0, 0, 0, 0, now, now);
        records.add(record);
        return record;
    }

    /**
     * Get safe summary for admin history view.
     */
    public IcerikFabrikasiInternalDryRunRecordSummary getSummary(IcerikFabrikasiInternalDryRunRecord record) {
        return new IcerikFabrikasiInternalDryRunRecordSummary(record);
    }

    /**
     * Get all records as safe summaries for admin history.
     */
    public List<IcerikFabrikasiInternalDryRunRecordSummary> getHistory() {
        return records.stream()
                .map(IcerikFabrikasiInternalDryRunRecordSummary::new)
                .toList();
    }

    public int getRecordCount() { return records.size(); }

    private IcerikFabrikasiInternalDryRunRecordStatus mapStatus(IcerikFabrikasiInternalDryRunTriggerResponse r) {
        if (r.getStatus() == null) return IcerikFabrikasiInternalDryRunRecordStatus.FAILED_SAFE;
        return switch (r.getStatus()) {
            case ADMIN_METADATA_VISIBLE -> IcerikFabrikasiInternalDryRunRecordStatus.READY_FOR_ADMIN_HISTORY_ONLY;
            case REJECTED_UNSAFE -> IcerikFabrikasiInternalDryRunRecordStatus.REJECTED_UNSAFE;
            default -> IcerikFabrikasiInternalDryRunRecordStatus.METADATA_CAPTURED;
        };
    }

    private List<IcerikFabrikasiInternalDryRunRecordIssueCode> mapIssues(IcerikFabrikasiInternalDryRunTriggerResponse r) {
        if (r.getStatus() == IcerikFabrikasiStoryboardInternalRenderChainStatus.REJECTED_UNSAFE)
            return List.of(IcerikFabrikasiInternalDryRunRecordIssueCode.TRIGGER_REJECTED);
        if (r.getIssueCodes() != null && !r.getIssueCodes().isEmpty())
            return List.of(IcerikFabrikasiInternalDryRunRecordIssueCode.DRY_RUN_CHAIN_FAILED);
        return List.of();
    }
}
