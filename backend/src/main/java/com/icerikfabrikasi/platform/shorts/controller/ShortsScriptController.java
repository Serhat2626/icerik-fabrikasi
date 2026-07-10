package com.icerikfabrikasi.platform.shorts.controller;

import com.icerikfabrikasi.platform.common.api.ApiResponse;
import com.icerikfabrikasi.platform.shorts.ShortsScript;
import com.icerikfabrikasi.platform.shorts.ShortsScriptService;
import com.icerikfabrikasi.platform.shorts.ShortsScriptService.SceneInput;
import com.icerikfabrikasi.platform.shorts.ShortsScriptStatus;
import com.icerikfabrikasi.platform.shorts.controller.dto.CreateShortsScriptRequest;
import com.icerikfabrikasi.platform.shorts.controller.dto.ShortsScriptResponse;
import com.icerikfabrikasi.platform.shorts.render.ShortsRenderOrchestrator;
import com.icerikfabrikasi.platform.shorts.render.ShortsRenderResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Shorts video script yönetim REST controller.
 *
 * <p>Script oluşturma, listeleme, review, render ve lifecycle yönetimi endpoint'leri.</p>
 */
@RestController
@RequestMapping("/api/admin/shorts-scripts")
@RequiredArgsConstructor
@Tag(name = "Shorts Scripts", description = "YouTube Shorts video script yönetimi")
public class ShortsScriptController {

    private final ShortsScriptService shortsScriptService;
    private final ShortsRenderOrchestrator renderOrchestrator;

    @PostMapping
    @Operation(summary = "Yeni shorts script oluştur")
    public ResponseEntity<ApiResponse<ShortsScriptResponse>> createScript(
            @Valid @RequestBody CreateShortsScriptRequest request) {

        List<SceneInput> sceneInputs = request.getScenes().stream()
                .map(s -> new SceneInput(s.getText(), s.getVisualHint(), s.getDurationSeconds()))
                .toList();

        ShortsScript script = shortsScriptService.createScript(
                request.getChannelId(), request.getTopic(), request.getTitle(),
                request.getHook(), request.getCta(), request.getHashtags(), sceneInputs);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(ShortsScriptResponse.from(script), "Script başarıyla oluşturuldu"));
    }

    @GetMapping
    @Operation(summary = "Shorts script'lerini listele")
    public ResponseEntity<ApiResponse<List<ShortsScriptResponse>>> listScripts(
            @RequestParam UUID channelId,
            @RequestParam(required = false) ShortsScriptStatus status) {

        List<ShortsScript> scripts = status != null
                ? shortsScriptService.getScriptsByStatus(channelId, status)
                : shortsScriptService.getScriptsByChannel(channelId);

        List<ShortsScriptResponse> responses = scripts.stream()
                .map(ShortsScriptResponse::from).toList();

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/{scriptId}")
    @Operation(summary = "Shorts script detayını getir")
    public ResponseEntity<ApiResponse<ShortsScriptResponse>> getScript(@PathVariable UUID scriptId) {
        ShortsScript script = shortsScriptService.getScript(scriptId);
        return ResponseEntity.ok(ApiResponse.success(ShortsScriptResponse.from(script)));
    }

    @PostMapping("/{scriptId}/approve")
    @Operation(summary = "Script'i onayla — render'a hazır")
    public ResponseEntity<ApiResponse<ShortsScriptResponse>> approveScript(@PathVariable UUID scriptId) {
        ShortsScript script = shortsScriptService.approveScript(scriptId);
        return ResponseEntity.ok(ApiResponse.success(ShortsScriptResponse.from(script), "Script onaylandı — render'a hazır"));
    }

    @PostMapping("/{scriptId}/render")
    @Operation(summary = "Onaylı script'i video olarak render et")
    public ResponseEntity<ApiResponse<ShortsRenderResult>> renderScript(@PathVariable UUID scriptId) {
        ShortsRenderResult result = renderOrchestrator.renderScript(scriptId);
        if (result.isSuccess()) {
            return ResponseEntity.ok(ApiResponse.success(result,
                    "Video render başarılı — " + result.getDurationSeconds() + " saniye"));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(result.getErrorMessage()));
        }
    }

    @PostMapping("/{scriptId}/request-revision")
    @Operation(summary = "Script revizyon iste")
    public ResponseEntity<ApiResponse<ShortsScriptResponse>> requestRevision(
            @PathVariable UUID scriptId, @RequestBody String reviewNotes) {
        ShortsScript script = shortsScriptService.requestRevision(scriptId, reviewNotes);
        return ResponseEntity.ok(ApiResponse.success(ShortsScriptResponse.from(script), "Revizyon istendi"));
    }

    @PostMapping("/{scriptId}/publish")
    @Operation(summary = "Script'i yayınlandı olarak işaretle")
    public ResponseEntity<ApiResponse<ShortsScriptResponse>> markPublished(@PathVariable UUID scriptId) {
        ShortsScript script = shortsScriptService.markPublished(scriptId);
        return ResponseEntity.ok(ApiResponse.success(ShortsScriptResponse.from(script), "Video yayınlandı olarak işaretlendi"));
    }
}
