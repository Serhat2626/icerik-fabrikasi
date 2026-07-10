package com.icerikfabrikasi.platform.icerikvideo;

/**
 * FP-037: Expected artifact within a scene render plan.
 * No content/prompt/lyrics/script body. Metadata-only.
 */
public final class IcerikFabrikasiSceneRenderArtifactExpectation {
    private final IcerikFabrikasiRenderBundleArtifactRole role;
    private final boolean required;

    public IcerikFabrikasiSceneRenderArtifactExpectation(IcerikFabrikasiRenderBundleArtifactRole role, boolean required) {
        this.role = role != null ? role : IcerikFabrikasiRenderBundleArtifactRole.UNKNOWN;
        this.required = required;
    }

    public IcerikFabrikasiRenderBundleArtifactRole getRole() { return role; }
    public boolean isRequired() { return required; }
}
