package com.icerikfabrikasi.platform.shorts.render;

import com.icerikfabrikasi.platform.shorts.ShortsScript;
import com.icerikfabrikasi.platform.shorts.ShortsScriptService;
import com.icerikfabrikasi.platform.shorts.ShortsScriptStatus;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Toplu render servisi — herhangi bir kanalın onaylı script'lerini toplu render eder.
 *
 * <p>Akış: Channel → APPROVED script'ler → render → sonuç raporu</p>
 *
 * <p>Generic: Tüm kanallar için aynı servis kullanılır.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BatchRenderService {

    private final ShortsScriptService scriptService;
    private final ShortsRenderOrchestrator renderOrchestrator;

    /**
     * Kanalın tüm onaylı script'lerini toplu render eder.
     *
     * @param channelId Render edilecek kanalın ID'si
     * @return Toplu render raporu
     */
    public BatchRenderReport renderAllApproved(UUID channelId) {
        List<ShortsScript> approvedScripts = scriptService.getScriptsByStatus(
                channelId, ShortsScriptStatus.APPROVED);

        log.info("Batch render başlıyor: channelId={}, scriptCount={}",
                channelId, approvedScripts.size());

        if (approvedScripts.isEmpty()) {
            log.info("Render edilecek onaylı script yok: channelId={}", channelId);
            return BatchRenderReport.builder()
                    .channelId(channelId)
                    .totalScripts(0)
                    .successCount(0)
                    .failureCount(0)
                    .results(List.of())
                    .startedAt(LocalDateTime.now())
                    .completedAt(LocalDateTime.now())
                    .build();
        }

        LocalDateTime startedAt = LocalDateTime.now();
        List<ScriptRenderOutcome> outcomes = new ArrayList<>();
        int successCount = 0;
        int failureCount = 0;

        for (ShortsScript script : approvedScripts) {
            try {
                ShortsRenderResult result = renderOrchestrator.renderScript(script.getId());

                if (result.isSuccess()) {
                    successCount++;
                    outcomes.add(ScriptRenderOutcome.builder()
                            .scriptId(script.getId())
                            .title(script.getTitle())
                            .success(true)
                            .outputPath(result.getOutputPath())
                            .durationSeconds(result.getDurationSeconds())
                            .fileSizeBytes(result.getFileSizeBytes())
                            .build());
                    log.info("Batch render başarılı: [{}/{}] title='{}'",
                            successCount + failureCount, approvedScripts.size(), script.getTitle());
                } else {
                    failureCount++;
                    outcomes.add(ScriptRenderOutcome.builder()
                            .scriptId(script.getId())
                            .title(script.getTitle())
                            .success(false)
                            .errorMessage(result.getErrorMessage())
                            .build());
                    log.warn("Batch render başarısız: title='{}', hata={}",
                            script.getTitle(), result.getErrorMessage());
                }

            } catch (Exception e) {
                failureCount++;
                outcomes.add(ScriptRenderOutcome.builder()
                        .scriptId(script.getId())
                        .title(script.getTitle())
                        .success(false)
                        .errorMessage("Beklenmeyen hata: " + e.getMessage())
                        .build());
                log.error("Batch render exception: title='{}', hata={}", script.getTitle(), e.getMessage(), e);
            }
        }

        BatchRenderReport report = BatchRenderReport.builder()
                .channelId(channelId)
                .totalScripts(approvedScripts.size())
                .successCount(successCount)
                .failureCount(failureCount)
                .results(outcomes)
                .startedAt(startedAt)
                .completedAt(LocalDateTime.now())
                .build();

        log.info("Batch render tamamlandı: channelId={}, toplam={}, başarılı={}, başarısız={}",
                channelId, report.getTotalScripts(), successCount, failureCount);

        return report;
    }

    /**
     * Toplu render raporu.
     */
    @Value
    @Builder
    public static class BatchRenderReport {
        UUID channelId;
        int totalScripts;
        int successCount;
        int failureCount;
        List<ScriptRenderOutcome> results;
        LocalDateTime startedAt;
        LocalDateTime completedAt;

        public boolean isAllSuccess() {
            return failureCount == 0 && totalScripts > 0;
        }

        public long totalFileSizeBytes() {
            return results.stream()
                    .filter(ScriptRenderOutcome::isSuccess)
                    .mapToLong(ScriptRenderOutcome::getFileSizeBytes)
                    .sum();
        }
    }

    /**
     * Tek bir script'in render sonucu.
     */
    @Value
    @Builder
    public static class ScriptRenderOutcome {
        UUID scriptId;
        String title;
        boolean success;
        String outputPath;
        int durationSeconds;
        long fileSizeBytes;
        String errorMessage;
    }
}
