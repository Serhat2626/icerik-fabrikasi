package com.icerikfabrikasi.platform.icerikvideo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * LIVE-005: Human review + controlled delivery service.
 * Delivery/export blocked until human review approved.
 */
public class IcerikFabrikasiLiveReviewDeliveryService {

    /**
     * Evaluate delivery eligibility based on render result + human review.
     */
    public IcerikFabrikasiLiveControlledDeliveryResult evaluate(
            IcerikFabrikasiLiveRenderResult renderResult,
            IcerikFabrikasiLiveReviewDecision reviewDecision,
            List<IcerikFabrikasiLiveReviewChecklistItem> checklist) {

        if (renderResult == null || renderResult.getRenderArtifact() == null) {
            return blocked(IcerikFabrikasiLiveDeliveryStatus.NOT_ELIGIBLE, null,
                    List.of(IcerikFabrikasiLiveDeliveryIssueCode.RENDER_NOT_VERIFIED),
                    IcerikFabrikasiLiveReviewDecision.PENDING, List.of(),
                    List.of("Render sonucu eksik"), "Engellendi: render eksik");
        }

        if (checklist == null) checklist = IcerikFabrikasiLiveReviewChecklistItem.defaultChecklist();

        var artifact = renderResult.getRenderArtifact();
        String renderRef = artifact.getOpaqueRenderRef();

        // Gate: Human review required
        if (reviewDecision == null || reviewDecision == IcerikFabrikasiLiveReviewDecision.PENDING) {
            return new IcerikFabrikasiLiveControlledDeliveryResult(
                    IcerikFabrikasiLiveDeliveryStatus.PENDING_HUMAN_REVIEW,
                    List.of(IcerikFabrikasiLiveDeliveryIssueCode.HUMAN_REVIEW_NOT_COMPLETED),
                    null, renderRef, IcerikFabrikasiLiveReviewDecision.PENDING, checklist,
                    artifact.getArtifactLabel(), artifact.getFormatHint(),
                    artifact.getResolutionHint(),
                    String.format("~%.0f saniye", artifact.getDurationSecondsHint()),
                    artifact.getSizeLabel(),
                    false, "Insan incelemesi tamamlanmadi",
                    List.of("Insan incelemesi bekleniyor"), "Engellendi: inceleme bekleniyor");
        }

        // Rejected
        if (reviewDecision == IcerikFabrikasiLiveReviewDecision.REJECTED) {
            return new IcerikFabrikasiLiveControlledDeliveryResult(
                    IcerikFabrikasiLiveDeliveryStatus.BLOCKED_BY_REVIEW,
                    List.of(IcerikFabrikasiLiveDeliveryIssueCode.REVIEW_REJECTED),
                    null, renderRef, IcerikFabrikasiLiveReviewDecision.REJECTED, checklist,
                    artifact.getArtifactLabel(), artifact.getFormatHint(),
                    artifact.getResolutionHint(),
                    String.format("~%.0f saniye", artifact.getDurationSecondsHint()),
                    artifact.getSizeLabel(),
                    false, "Inceleme reddedildi",
                    List.of("Insan incelemesi reddedildi"), "Engellendi: reddedildi");
        }

        // Needs revision
        if (reviewDecision == IcerikFabrikasiLiveReviewDecision.NEEDS_REVISION) {
            return new IcerikFabrikasiLiveControlledDeliveryResult(
                    IcerikFabrikasiLiveDeliveryStatus.BLOCKED_BY_REVIEW,
                    List.of(IcerikFabrikasiLiveDeliveryIssueCode.REVIEW_NEEDS_REVISION),
                    null, renderRef, IcerikFabrikasiLiveReviewDecision.NEEDS_REVISION, checklist,
                    artifact.getArtifactLabel(), artifact.getFormatHint(),
                    artifact.getResolutionHint(),
                    String.format("~%.0f saniye", artifact.getDurationSecondsHint()),
                    artifact.getSizeLabel(),
                    false, "Revizyon gerekiyor",
                    List.of("Revizyon gerekiyor"), "Engellendi: revizyon");
        }

        // Approved for controlled delivery
        return new IcerikFabrikasiLiveControlledDeliveryResult(
                IcerikFabrikasiLiveDeliveryStatus.ELIGIBLE_FOR_CONTROLLED_DELIVERY,
                List.of(),
                "ldlv:" + UUID.randomUUID().toString().substring(0, 8),
                renderRef,
                IcerikFabrikasiLiveReviewDecision.APPROVED_FOR_CONTROLLED_DELIVERY, checklist,
                artifact.getArtifactLabel(), artifact.getFormatHint(),
                artifact.getResolutionHint(),
                String.format("~%.0f saniye", artifact.getDurationSecondsHint()),
                artifact.getSizeLabel(),
                true, "Kontrollü teslim icin uygun — sadece admin erisimiyle",
                List.of(),
                String.format("Dogrulama: inceleme onaylandi, kontrollü teslim uygun, icerik gizli"));
    }

    private IcerikFabrikasiLiveControlledDeliveryResult blocked(
            IcerikFabrikasiLiveDeliveryStatus status, String renderRef,
            List<IcerikFabrikasiLiveDeliveryIssueCode> issues,
            IcerikFabrikasiLiveReviewDecision decision, List<IcerikFabrikasiLiveReviewChecklistItem> checklist,
            List<String> reasons, String summary) {
        return new IcerikFabrikasiLiveControlledDeliveryResult(
                status, issues, null, renderRef, decision, checklist,
                "", "", "", "", "",
                false, "Uygun degil", reasons, summary);
    }
}
