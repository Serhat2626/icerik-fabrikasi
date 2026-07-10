package com.icerikfabrikasi.platform.icerikvideo;

import java.util.List;

/**
 * FP-053: Safe operational summary — counts and statuses only.
 * No record bodies, no paths, no logs, no storage, no URLs, no media.
 */
public final class IcerikFabrikasiInternalDryRunOperationalSummary {
    private final int totalRecordCount;
    private final int acceptedCount;
    private final int failedCount;
    private final int rejectedCount;
    private final int readyForAdminHistoryOnlyCount;
    private final IcerikFabrikasiInternalDryRunOperationalSummaryStatus overallStatus;
    private final List<IcerikFabrikasiInternalDryRunOperationalSummaryIssueCode> issueCodes;
    private final int totalSceneCount;
    private final int totalClipCount;
    private final int stitchedCount;
    private final boolean internalOnly;

    public IcerikFabrikasiInternalDryRunOperationalSummary(
            int totalRecordCount, int acceptedCount, int failedCount, int rejectedCount,
            int readyForAdminHistoryOnlyCount,
            IcerikFabrikasiInternalDryRunOperationalSummaryStatus overallStatus,
            List<IcerikFabrikasiInternalDryRunOperationalSummaryIssueCode> issueCodes,
            int totalSceneCount, int totalClipCount, int stitchedCount) {
        this.totalRecordCount = totalRecordCount;
        this.acceptedCount = acceptedCount;
        this.failedCount = failedCount;
        this.rejectedCount = rejectedCount;
        this.readyForAdminHistoryOnlyCount = readyForAdminHistoryOnlyCount;
        this.overallStatus = overallStatus;
        this.issueCodes = issueCodes == null ? List.of() : List.copyOf(issueCodes);
        this.totalSceneCount = totalSceneCount;
        this.totalClipCount = totalClipCount;
        this.stitchedCount = stitchedCount;
        this.internalOnly = true;
    }

    public int getTotalRecordCount() { return totalRecordCount; }
    public int getAcceptedCount() { return acceptedCount; }
    public int getFailedCount() { return failedCount; }
    public int getRejectedCount() { return rejectedCount; }
    public int getReadyForAdminHistoryOnlyCount() { return readyForAdminHistoryOnlyCount; }
    public IcerikFabrikasiInternalDryRunOperationalSummaryStatus getOverallStatus() { return overallStatus; }
    public List<IcerikFabrikasiInternalDryRunOperationalSummaryIssueCode> getIssueCodes() { return issueCodes; }
    public int getTotalSceneCount() { return totalSceneCount; }
    public int getTotalClipCount() { return totalClipCount; }
    public int getStitchedCount() { return stitchedCount; }
    public boolean isInternalOnly() { return internalOnly; }
}
