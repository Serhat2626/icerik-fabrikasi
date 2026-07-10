package com.icerikfabrikasi.platform.icerikvideo;

import java.util.List;
import java.util.UUID;

/**
 * RP-001: Service that creates copyright-safe storyboard requests from a Türkü title.
 * No AI call. No lyrics. No media. No real generation.
 */
public class IcerikFabrikasiNameStoryboardRequestService {

    private final IcerikFabrikasiNameStoryboardRequestPolicy policy = new IcerikFabrikasiNameStoryboardRequestPolicy();

    public IcerikFabrikasiNameStoryboardRequestResult createRequest(IcerikFabrikasiNameStoryboardRequest request) {
        if (request == null) {
            return IcerikFabrikasiNameStoryboardRequestResult.rejected(IcerikFabrikasiNameStoryboardRequestIssueCode.TITLE_EMPTY);
        }

        String title = request.getIcerikFabrikasiTitle();

        if (policy.isTitleEmpty(title)) {
            return IcerikFabrikasiNameStoryboardRequestResult.rejected(IcerikFabrikasiNameStoryboardRequestIssueCode.TITLE_EMPTY);
        }
        if (policy.isTitleTooLong(title)) {
            return IcerikFabrikasiNameStoryboardRequestResult.rejected(IcerikFabrikasiNameStoryboardRequestIssueCode.TITLE_TOO_LONG);
        }
        if (policy.containsLyrics(title)) {
            return IcerikFabrikasiNameStoryboardRequestResult.rejected(IcerikFabrikasiNameStoryboardRequestIssueCode.TITLE_CONTAINS_LYRICS);
        }
        if (policy.containsMultiline(title)) {
            return IcerikFabrikasiNameStoryboardRequestResult.rejected(IcerikFabrikasiNameStoryboardRequestIssueCode.TITLE_CONTAINS_MULTILINE);
        }
        if (policy.containsUrl(title)) {
            return IcerikFabrikasiNameStoryboardRequestResult.rejected(IcerikFabrikasiNameStoryboardRequestIssueCode.TITLE_CONTAINS_URL);
        }

        if (!policy.isSceneCountValid(request.getTargetSceneCount())) {
            return IcerikFabrikasiNameStoryboardRequestResult.rejected(IcerikFabrikasiNameStoryboardRequestIssueCode.SCENE_COUNT_OUT_OF_RANGE);
        }
        if (!policy.isDurationValid(request.getTargetDurationMs())) {
            return IcerikFabrikasiNameStoryboardRequestResult.rejected(IcerikFabrikasiNameStoryboardRequestIssueCode.DURATION_OUT_OF_RANGE);
        }
        if (!policy.isFpsValid(request.getTargetFps())) {
            return IcerikFabrikasiNameStoryboardRequestResult.rejected(IcerikFabrikasiNameStoryboardRequestIssueCode.FPS_OUT_OF_RANGE);
        }
        if (!policy.isResolutionValid(request.getTargetWidth(), request.getTargetHeight())) {
            return IcerikFabrikasiNameStoryboardRequestResult.rejected(IcerikFabrikasiNameStoryboardRequestIssueCode.RESOLUTION_OUT_OF_RANGE);
        }

        String safeTitle = title.strip();
        String intentSummary = "T\u00fcrk\u00fc ba\u015fl\u0131\u011f\u0131ndan ilham alan, telif hakk\u0131 g\u00fcvenli g\u00f6rsel hikaye olu\u015fturma niyeti. " +
                "S\u00f6z, ses veya video \u00e7o\u011faltma yap\u0131lmayacakt\u0131r.";

        return new IcerikFabrikasiNameStoryboardRequestResult(
                IcerikFabrikasiNameStoryboardRequestStatus.READY_FOR_STORYBOARD_GENERATION_BOUNDARY,
                List.of(),
                "sb-req:" + UUID.randomUUID().toString().substring(0, 8),
                safeTitle,
                "TURKU_TITLE_INSPIRATION",
                request.getTargetSceneCount(), request.getTargetDurationMs(),
                request.getTargetFormat(), request.getTargetWidth(), request.getTargetHeight(),
                request.getTargetFps(), intentSummary);
    }
}
