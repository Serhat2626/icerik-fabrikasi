package com.icerikfabrikasi.platform.channel.controller;

import com.icerikfabrikasi.platform.channel.Channel;
import com.icerikfabrikasi.platform.channel.ChannelNiche;
import com.icerikfabrikasi.platform.channel.ChannelService;
import com.icerikfabrikasi.platform.channel.ChannelStatus;
import com.icerikfabrikasi.platform.channel.TargetPlatform;
import com.icerikfabrikasi.platform.common.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Kanal yönetim REST controller.
 */
@RestController
@RequestMapping("/api/admin/channels")
@RequiredArgsConstructor
@Tag(name = "Channels", description = "YouTube / sosyal medya kanal yönetimi")
public class ChannelController {

    private final ChannelService channelService;

    @GetMapping
    @Operation(summary = "Tüm kanalları listele")
    public ResponseEntity<ApiResponse<List<ChannelSummary>>> listChannels() {
        List<ChannelSummary> channels = channelService.getAllChannels().stream()
                .map(ChannelSummary::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(channels));
    }

    @GetMapping("/{channelId}")
    @Operation(summary = "Kanal detayını getir")
    public ResponseEntity<ApiResponse<ChannelSummary>> getChannel(@PathVariable UUID channelId) {
        Channel channel = channelService.getChannel(channelId);
        return ResponseEntity.ok(ApiResponse.success(ChannelSummary.from(channel)));
    }

    @PostMapping("/{channelId}/activate")
    @Operation(summary = "Kanalı aktif et")
    public ResponseEntity<ApiResponse<ChannelSummary>> activateChannel(@PathVariable UUID channelId) {
        Channel channel = channelService.updateStatus(channelId, ChannelStatus.ACTIVE);
        return ResponseEntity.ok(ApiResponse.success(ChannelSummary.from(channel), "Kanal aktif edildi"));
    }

    @PostMapping("/{channelId}/pause")
    @Operation(summary = "Kanalı duraklat")
    public ResponseEntity<ApiResponse<ChannelSummary>> pauseChannel(@PathVariable UUID channelId) {
        Channel channel = channelService.updateStatus(channelId, ChannelStatus.PAUSED);
        return ResponseEntity.ok(ApiResponse.success(ChannelSummary.from(channel), "Kanal duraklatıldı"));
    }

    /**
     * Kanal özet DTO — inline record.
     */
    public record ChannelSummary(
            UUID id,
            String name,
            String description,
            ChannelNiche niche,
            TargetPlatform targetPlatform,
            ChannelStatus status,
            String contentLanguage,
            int weeklyVideoTarget,
            String accentColor,
            String fontFamily,
            String watermarkText,
            int totalVideosProduced,
            int totalVideosPublished
    ) {
        public static ChannelSummary from(Channel ch) {
            return new ChannelSummary(
                    ch.getId(), ch.getName(), ch.getDescription(),
                    ch.getNiche(), ch.getTargetPlatform(), ch.getStatus(),
                    ch.getContentLanguage(), ch.getWeeklyVideoTarget(),
                    ch.getAccentColor(), ch.getFontFamily(), ch.getWatermarkText(),
                    ch.getTotalVideosProduced(), ch.getTotalVideosPublished()
            );
        }
    }
}
