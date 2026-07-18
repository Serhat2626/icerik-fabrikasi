package com.icerikfabrikasi.platform.channel.affiliate;

import com.icerikfabrikasi.platform.common.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Affiliate link yönetim REST controller.
 */
@RestController
@RequestMapping("/api/admin/channels/{channelId}/affiliate-links")
@RequiredArgsConstructor
@Tag(name = "Affiliate Links", description = "Kanal affiliate link yönetimi")
public class AffiliateLinkController {

    private final AffiliateLinkService affiliateLinkService;

    @GetMapping
    @Operation(summary = "Kanalın tüm affiliate linklerini listele")
    public ResponseEntity<ApiResponse<List<AffiliateLinkDto>>> listLinks(@PathVariable UUID channelId) {
        List<AffiliateLinkDto> links = affiliateLinkService.getAllLinks(channelId).stream()
                .map(AffiliateLinkDto::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(links));
    }

    @GetMapping("/active")
    @Operation(summary = "Kanalın aktif affiliate linklerini listele")
    public ResponseEntity<ApiResponse<List<AffiliateLinkDto>>> listActiveLinks(@PathVariable UUID channelId) {
        List<AffiliateLinkDto> links = affiliateLinkService.getActiveLinks(channelId).stream()
                .map(AffiliateLinkDto::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(links));
    }

    @PostMapping
    @Operation(summary = "Yeni affiliate link ekle")
    public ResponseEntity<ApiResponse<AffiliateLinkDto>> addLink(
            @PathVariable UUID channelId,
            @RequestBody AddAffiliateLinkRequest request) {
        AffiliateLink link = affiliateLinkService.addLink(
                channelId, request.productName(), request.affiliateUrl(),
                request.commissionType(), request.commissionValue(),
                request.displayText(), request.keywords(),
                request.priority() != null ? request.priority() : 5,
                request.emoji());
        return ResponseEntity.ok(ApiResponse.success(AffiliateLinkDto.from(link), "Affiliate link eklendi"));
    }

    @PutMapping("/{linkId}")
    @Operation(summary = "Affiliate linki güncelle")
    public ResponseEntity<ApiResponse<AffiliateLinkDto>> updateLink(
            @PathVariable UUID channelId,
            @PathVariable UUID linkId,
            @RequestBody UpdateAffiliateLinkRequest request) {
        AffiliateLink link = affiliateLinkService.updateLink(
                linkId, request.affiliateUrl(), request.displayText(),
                request.keywords(), request.priority() != null ? request.priority() : 5,
                request.active() != null ? request.active() : true);
        return ResponseEntity.ok(ApiResponse.success(AffiliateLinkDto.from(link), "Link güncellendi"));
    }

    @DeleteMapping("/{linkId}")
    @Operation(summary = "Affiliate linki devre dışı bırak")
    public ResponseEntity<ApiResponse<Void>> deactivateLink(
            @PathVariable UUID channelId,
            @PathVariable UUID linkId) {
        affiliateLinkService.deactivateLink(linkId);
        return ResponseEntity.ok(ApiResponse.success(null, "Link devre dışı bırakıldı"));
    }

    @GetMapping("/match")
    @Operation(summary = "Konu bazlı affiliate link eşleştir")
    public ResponseEntity<ApiResponse<List<AffiliateLinkDto>>> matchLinks(
            @PathVariable UUID channelId,
            @RequestParam String topic,
            @RequestParam(required = false) String scriptText) {
        List<AffiliateLinkDto> links = affiliateLinkService.matchLinksForContent(channelId, topic, scriptText).stream()
                .map(AffiliateLinkDto::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(links));
    }

    // ── DTOs ──

    public record AffiliateLinkDto(
            UUID id,
            String productName,
            String affiliateUrl,
            CommissionType commissionType,
            String commissionValue,
            String displayText,
            String keywords,
            int priority,
            boolean active,
            String emoji
    ) {
        public static AffiliateLinkDto from(AffiliateLink link) {
            return new AffiliateLinkDto(
                    link.getId(), link.getProductName(), link.getAffiliateUrl(),
                    link.getCommissionType(), link.getCommissionValue(),
                    link.getDisplayText(), link.getKeywords(),
                    link.getPriority(), link.isActive(), link.getEmoji());
        }
    }

    public record AddAffiliateLinkRequest(
            String productName,
            String affiliateUrl,
            CommissionType commissionType,
            String commissionValue,
            String displayText,
            String keywords,
            Integer priority,
            String emoji
    ) {}

    public record UpdateAffiliateLinkRequest(
            String affiliateUrl,
            String displayText,
            String keywords,
            Integer priority,
            Boolean active
    ) {}
}
