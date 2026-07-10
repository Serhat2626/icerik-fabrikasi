package com.icerikfabrikasi.platform.icerikvideo;

import org.springframework.stereotype.Service;

/**
 * FP-020 Task 02: Provider safety policy enforcement.
 *
 * <p>Enforces pre/post-generation invariants.
 * autoApproved must always be false. humanReviewRequired must always be true.
 * lyricsIncluded must always be false. copyrightClearanceVerified must always be false (manual only).</p>
 */
@Service
public class IcerikFabrikasiProviderSafetyPolicy {

    /**
     * Validates envelope before generation.
     * Throws if safety invariants are violated.
     */
    public void enforcePreGenerationPolicy(IcerikFabrikasiGenerationSafetyEnvelope envelope) {
        if (envelope.isAutoApproved()) {
            throw new IcerikFabrikasiVideoProjectException(
                    "Güvenlik ihlali: autoApproved üretim öncesi true olamaz",
                    "POLICY_AUTO_APPROVED_VIOLATION");
        }
        if (!envelope.isHumanReviewRequired()) {
            throw new IcerikFabrikasiVideoProjectException(
                    "Güvenlik ihlali: humanReviewRequired üretim öncesi false olamaz",
                    "POLICY_HUMAN_REVIEW_VIOLATION");
        }
    }

    /**
     * Validates envelope after generation.
     * Throws if safety invariants are violated.
     */
    public void enforcePostGenerationPolicy(IcerikFabrikasiGenerationSafetyEnvelope envelope) {
        if (envelope.isAutoApproved()) {
            throw new IcerikFabrikasiVideoProjectException(
                    "Güvenlik ihlali: autoApproved üretim sonrası true olamaz",
                    "POLICY_AUTO_APPROVED_VIOLATION");
        }
        if (envelope.isLyricsIncluded()) {
            throw new IcerikFabrikasiVideoProjectException(
                    "Güvenlik ihlali: lyricsIncluded true olamaz",
                    "POLICY_LYRICS_VIOLATION");
        }
        if (envelope.isCopyrightClearanceVerified()) {
            throw new IcerikFabrikasiVideoProjectException(
                    "Güvenlik ihlali: copyrightClearanceVerified otomatik true olamaz",
                    "POLICY_COPYRIGHT_VIOLATION");
        }
        if (!envelope.isHumanReviewRequired()) {
            throw new IcerikFabrikasiVideoProjectException(
                    "Güvenlik ihlali: humanReviewRequired üretim sonrası false olamaz",
                    "POLICY_HUMAN_REVIEW_VIOLATION");
        }
    }

    /**
     * Resolves the effective provider mode for a project.
     * Returns DETERMINISTIC_STUB if not explicitly set.
     */
    public IcerikFabrikasiProviderMode resolveProviderMode(IcerikFabrikasiVideoProject project) {
        IcerikFabrikasiProviderMode mode = project.getProviderMode();
        return mode != null ? mode : IcerikFabrikasiProviderMode.DETERMINISTIC_STUB;
    }
}
