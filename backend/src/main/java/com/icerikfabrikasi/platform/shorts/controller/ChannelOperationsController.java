package com.icerikfabrikasi.platform.shorts.controller;

import com.icerikfabrikasi.platform.common.api.ApiResponse;
import com.icerikfabrikasi.platform.shorts.publish.VideoPublishService;
import com.icerikfabrikasi.platform.shorts.publish.VideoPublishService.BatchPublishReport;
import com.icerikfabrikasi.platform.shorts.publish.VideoPublishService.PublishResult;
import com.icerikfabrikasi.platform.shorts.render.BatchRenderService;
import com.icerikfabrikasi.platform.shorts.render.BatchRenderService.BatchRenderReport;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Batch operasyonları REST controller — render + publish.
 *
 * <p>Generic: Herhangi bir kanal için çalışır.</p>
 */
@RestController
@RequestMapping("/api/admin/channels/{channelId}")
@RequiredArgsConstructor
@Tag(name = "Channel Operations", description = "Kanal bazlı toplu render ve yayınlama")
public class ChannelOperationsController {

    private final BatchRenderService batchRenderService;
    private final VideoPublishService videoPublishService;

    // ── POST /api/admin/channels/{channelId}/batch-render ──

    @PostMapping("/batch-render")
    @Operation(summary = "Tüm onaylı script'leri toplu render et")
    public ResponseEntity<ApiResponse<BatchRenderReport>> batchRender(
            @PathVariable UUID channelId) {

        BatchRenderReport report = batchRenderService.renderAllApproved(channelId);

        String message = String.format("Batch render tamamlandı: %d/%d başarılı",
                report.getSuccessCount(), report.getTotalScripts());

        return ResponseEntity.ok(ApiResponse.success(report, message));
    }

    // ── POST /api/admin/channels/{channelId}/batch-publish ──

    @PostMapping("/batch-publish")
    @Operation(summary = "Tüm render edilmiş videoları toplu yayınla")
    public ResponseEntity<ApiResponse<BatchPublishReport>> batchPublish(
            @PathVariable UUID channelId) {

        BatchPublishReport report = videoPublishService.publishAllRendered(channelId);

        String message = String.format("Toplu yayınlama tamamlandı: %d/%d başarılı",
                report.getSuccessCount(), report.getTotalScripts());

        return ResponseEntity.ok(ApiResponse.success(report, message));
    }

    // ── POST /api/admin/shorts-scripts/{scriptId}/publish-to-youtube ──

    @PostMapping("/scripts/{scriptId}/publish-to-youtube")
    @Operation(summary = "Tek bir videoyu YouTube'a yükle")
    public ResponseEntity<ApiResponse<PublishResult>> publishToYouTube(
            @PathVariable UUID channelId,
            @PathVariable UUID scriptId) {

        PublishResult result = videoPublishService.publishScript(scriptId);

        if (result.isSuccess()) {
            return ResponseEntity.ok(ApiResponse.success(result,
                    "Video YouTube'a yüklendi: " + result.getYoutubeUrl()));
        } else {
            return ResponseEntity.ok(ApiResponse.success(result, result.getErrorMessage()));
        }
    }
}
