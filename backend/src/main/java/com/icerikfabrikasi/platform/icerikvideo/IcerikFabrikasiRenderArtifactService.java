package com.icerikfabrikasi.platform.icerikvideo;

import java.util.List;
import java.util.UUID;

/**
 * FP-023 Task 04: Render artifact service.
 * Creates safe artifact metadata from render execution results.
 * No filesystem access. No storage provider calls. No real deletion.
 */
public class IcerikFabrikasiRenderArtifactService {

    private static final List<String> FORBIDDEN_REF_PATTERNS = List.of(
            "http://", "https://", "ftp://", "file://",
            "s3://", "gs://", "az://",
            "C:\\", "D:\\", "/usr", "/tmp", "/var", "/home",
            "publicUrl", "signedUrl", "cdnUrl", "storageKey",
            "base64", "data:image", "data:video", "data:audio",
            "Bearer ", "Authorization:", "token=", "apiKey=",
            "password=", "secret=",
            ";", "&&", "||", "$(", "`",
            "rm ", "del ", "format ");

    private final IcerikFabrikasiRenderAdapterSanitizer sanitizer;

    public IcerikFabrikasiRenderArtifactService() {
        this.sanitizer = new IcerikFabrikasiRenderAdapterSanitizer();
    }

    public IcerikFabrikasiInternalRenderArtifact createFromExecutionResult(
            IcerikFabrikasiRenderExecutionResult result,
            IcerikFabrikasiRenderAdapterMode rendererMode) {
        if (result == null || result.getArtifact() == null) {
            return null;
        }

        IcerikFabrikasiRenderArtifact rawArtifact = result.getArtifact();
        String ref = rawArtifact.getArtifactRef();

        // Validate ref is safe
        if (!isValidOpaqueRef(ref)) {
            return null;
        }

        IcerikFabrikasiRenderArtifactReference reference = new IcerikFabrikasiRenderArtifactReference(
                rawArtifact.getArtifactId(),
                ref, rendererMode,
                rawArtifact.getFormat(),
                rawArtifact.getDurationSeconds(),
                rawArtifact.getResolution(),
                rawArtifact.getSizeLabel());

        IcerikFabrikasiInternalRenderArtifact artifact = new IcerikFabrikasiInternalRenderArtifact(reference, rendererMode);
        artifact.markValidated();
        return artifact;
    }

    public boolean isValidOpaqueRef(String ref) {
        if (ref == null || ref.isBlank()) return false;
        for (String pattern : FORBIDDEN_REF_PATTERNS) {
            if (ref.contains(pattern)) return false;
        }
        return true;
    }

    public IcerikFabrikasiRenderArtifactCleanupPlan createCleanupPlan(IcerikFabrikasiInternalRenderArtifact artifact) {
        if (artifact == null) {
            return null;
        }
        boolean eligible = artifact.getStatus() == IcerikFabrikasiRenderArtifactStatus.VALIDATED
                || artifact.getStatus() == IcerikFabrikasiRenderArtifactStatus.CLEANUP_ELIGIBLE;
        String reason = eligible
                ? "Artifact eligible for cleanup after retention period"
                : "Artifact not eligible: status=" + artifact.getStatus();

        return new IcerikFabrikasiRenderArtifactCleanupPlan(
                artifact.getReference().getArtifactId(),
                artifact.getReference().getOpaqueRef(),
                artifact.getRetentionPolicy(),
                eligible, reason);
    }

    public IcerikFabrikasiRenderArtifactSafetyReport getSafetyReport(IcerikFabrikasiInternalRenderArtifact artifact) {
        if (artifact == null) return null;
        return artifact.getSafetyReport();
    }
}
