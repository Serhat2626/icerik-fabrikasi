package com.icerikfabrikasi.platform.icerikvideo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * FP-036: Render bundle manifest service.
 * Builds/validates bundle from safe artifact refs. No ProcessBuilder.
 */
public class IcerikFabrikasiRenderBundleManifestService {

    private static final int MAX_ARTIFACTS = 10;
    private static final long MAX_TOTAL_SIZE_BYTES = 100L * 1024 * 1024; // 100MB

    /**
     * Build manifest from artifact refs with policy validation.
     */
    public IcerikFabrikasiRenderBundleManifest build(String opaqueBundleRef,
            List<IcerikFabrikasiRenderBundleArtifactRef> artifacts) {

        var issues = new ArrayList<IcerikFabrikasiRenderBundleIssueCode>();
        var safeArtifacts = artifacts != null ? artifacts : List.<IcerikFabrikasiRenderBundleArtifactRef>of();

        // 1. Bundle ref
        if (opaqueBundleRef == null || opaqueBundleRef.isBlank()) {
            opaqueBundleRef = "bundle:" + UUID.randomUUID();
            issues.add(IcerikFabrikasiRenderBundleIssueCode.BUNDLE_REF_MISSING);
        }

        // 2. Max artifacts
        if (safeArtifacts.size() > MAX_ARTIFACTS)
            issues.add(IcerikFabrikasiRenderBundleIssueCode.TOO_MANY_ARTIFACTS);

        // 3. Total size
        long totalSize = safeArtifacts.stream().mapToLong(IcerikFabrikasiRenderBundleArtifactRef::getSizeBytes).sum();
        if (totalSize > MAX_TOTAL_SIZE_BYTES)
            issues.add(IcerikFabrikasiRenderBundleIssueCode.TOTAL_SIZE_EXCEEDS_LIMIT);

        // 4. Primary video check
        long primaryCount = safeArtifacts.stream()
                .filter(a -> a.getRole() == IcerikFabrikasiRenderBundleArtifactRole.PRIMARY_VIDEO_CLIP).count();
        if (primaryCount == 0)
            issues.add(IcerikFabrikasiRenderBundleIssueCode.MISSING_PRIMARY_VIDEO);
        else if (primaryCount > 1)
            issues.add(IcerikFabrikasiRenderBundleIssueCode.DUPLICATE_PRIMARY_VIDEO);

        // 5. Role validation
        if (safeArtifacts.stream().anyMatch(a -> !a.getRole().isAllowed()))
            issues.add(IcerikFabrikasiRenderBundleIssueCode.UNSUPPORTED_ROLE);

        // 6. Artifact status checks
        if (safeArtifacts.stream().anyMatch(IcerikFabrikasiRenderBundleArtifactRef::isRejected))
            issues.add(IcerikFabrikasiRenderBundleIssueCode.ARTIFACT_REJECTED);
        if (safeArtifacts.stream().anyMatch(a ->
                a.getVerificationStatus() == IcerikFabrikasiRenderArtifactVerificationStatus.FAILED_SAFE))
            issues.add(IcerikFabrikasiRenderBundleIssueCode.ARTIFACT_FAILED);

        // 7. Determine bundle status
        IcerikFabrikasiRenderBundleStatus status;
        if (issues.stream().anyMatch(i -> i == IcerikFabrikasiRenderBundleIssueCode.ARTIFACT_FAILED))
            status = IcerikFabrikasiRenderBundleStatus.FAILED_SAFE;
        else if (issues.stream().anyMatch(i -> i == IcerikFabrikasiRenderBundleIssueCode.ARTIFACT_REJECTED
                || i == IcerikFabrikasiRenderBundleIssueCode.UNSUPPORTED_ROLE
                || i == IcerikFabrikasiRenderBundleIssueCode.TOO_MANY_ARTIFACTS
                || i == IcerikFabrikasiRenderBundleIssueCode.TOTAL_SIZE_EXCEEDS_LIMIT
                || i == IcerikFabrikasiRenderBundleIssueCode.MISSING_PRIMARY_VIDEO
                || i == IcerikFabrikasiRenderBundleIssueCode.DUPLICATE_PRIMARY_VIDEO))
            status = IcerikFabrikasiRenderBundleStatus.REJECTED_UNSAFE;
        else if (safeArtifacts.stream().allMatch(IcerikFabrikasiRenderBundleArtifactRef::isVerified))
            status = IcerikFabrikasiRenderBundleStatus.READY_FOR_INTERNAL_REVIEW_ONLY;
        else
            status = IcerikFabrikasiRenderBundleStatus.ARTIFACTS_ATTACHED;

        return new IcerikFabrikasiRenderBundleManifest(opaqueBundleRef, status, safeArtifacts, issues);
    }
}
