package com.icerikfabrikasi.platform.icerikvideo;

import java.util.*;

/**
 * FP-042: Service to build internal render review packages from safe metadata.
 * No auto-approval. No public delivery readiness.
 */
public class IcerikFabrikasiRenderReviewPackageService {

    private static final int MAX_ARTIFACTS = 10;

    /**
     * Build review package from verified artifact summaries.
     */
    public IcerikFabrikasiRenderReviewPackage build(String opaqueJobRef, String opaqueBundleRef,
            List<IcerikFabrikasiRenderReviewArtifactSummary> summaries,
            int sceneCount, int inputClipCount) {

        String pkgRef = "review-pkg:" + UUID.randomUUID();
        var issues = new ArrayList<IcerikFabrikasiRenderReviewPackageIssueCode>();
        var safe = summaries != null ? summaries : List.<IcerikFabrikasiRenderReviewArtifactSummary>of();

        // Validate
        if (safe.isEmpty()) {
            issues.add(IcerikFabrikasiRenderReviewPackageIssueCode.MISSING_ARTIFACT_REF);
        }
        if (safe.size() > MAX_ARTIFACTS) {
            issues.add(IcerikFabrikasiRenderReviewPackageIssueCode.TOO_MANY_ARTIFACTS);
        }

        boolean hasPrimary = false;
        var refSet = new HashSet<String>();
        for (var s : safe) {
            if (s.getOpaqueArtifactRef() == null || s.getOpaqueArtifactRef().isBlank())
                issues.add(IcerikFabrikasiRenderReviewPackageIssueCode.MISSING_ARTIFACT_REF);
            if (!refSet.add(s.getOpaqueArtifactRef()))
                issues.add(IcerikFabrikasiRenderReviewPackageIssueCode.DUPLICATE_ARTIFACT_REF);
            if (s.isPrimary()) hasPrimary = true;
            if (s.getVerificationStatus() == IcerikFabrikasiRenderArtifactVerificationStatus.REJECTED_DURATION
                    || s.getVerificationStatus() == IcerikFabrikasiRenderArtifactVerificationStatus.REJECTED_SIZE
                    || s.getVerificationStatus() == IcerikFabrikasiRenderArtifactVerificationStatus.REJECTED_MULTIPLE)
                issues.add(IcerikFabrikasiRenderReviewPackageIssueCode.ARTIFACT_REJECTED);
            if (!s.isVerified() && s.getVerificationStatus() != null
                    && s.getVerificationStatus() != IcerikFabrikasiRenderArtifactVerificationStatus.PASSED)
                issues.add(IcerikFabrikasiRenderReviewPackageIssueCode.ARTIFACT_NOT_VERIFIED);
            if (s.getRole() == null)
                issues.add(IcerikFabrikasiRenderReviewPackageIssueCode.ARTIFACT_ROLE_UNKNOWN);
        }
        if (!hasPrimary && !safe.isEmpty())
            issues.add(IcerikFabrikasiRenderReviewPackageIssueCode.NO_PRIMARY_ARTIFACT);

        // Compute aggregates
        int totalDuration = safe.stream().mapToInt(IcerikFabrikasiRenderReviewArtifactSummary::getDurationSeconds).sum();
        int maxW = safe.stream().mapToInt(IcerikFabrikasiRenderReviewArtifactSummary::getWidth).max().orElse(0);
        int maxH = safe.stream().mapToInt(IcerikFabrikasiRenderReviewArtifactSummary::getHeight).max().orElse(0);
        int maxFps = safe.stream().mapToInt(IcerikFabrikasiRenderReviewArtifactSummary::getFps).max().orElse(0);
        long totalSize = safe.stream().mapToLong(IcerikFabrikasiRenderReviewArtifactSummary::getSizeBytes).sum();

        IcerikFabrikasiRenderReviewPackageStatus status;
        if (!issues.isEmpty()) {
            boolean hasRejection = issues.stream().anyMatch(i ->
                    i == IcerikFabrikasiRenderReviewPackageIssueCode.ARTIFACT_REJECTED
                    || i == IcerikFabrikasiRenderReviewPackageIssueCode.ARTIFACT_NOT_VERIFIED);
            status = hasRejection
                    ? IcerikFabrikasiRenderReviewPackageStatus.REJECTED_UNSAFE
                    : IcerikFabrikasiRenderReviewPackageStatus.FAILED_SAFE;
        } else {
            status = IcerikFabrikasiRenderReviewPackageStatus.READY_FOR_INTERNAL_REVIEW_ONLY;
        }

        return new IcerikFabrikasiRenderReviewPackage(pkgRef, opaqueJobRef, opaqueBundleRef,
                status, issues, safe, sceneCount, inputClipCount,
                totalDuration, maxW, maxH, maxFps, totalSize);
    }
}
