package com.icerikfabrikasi.platform.shorts.controller.dto;

import com.icerikfabrikasi.platform.shorts.ShortsScript;
import com.icerikfabrikasi.platform.shorts.ShortsScriptStatus;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Shorts script response DTO.
 */
@Value
@Builder
public class ShortsScriptResponse {

    UUID id;
    UUID channelId;
    String channelName;
    String title;
    String topic;
    String hook;
    String cta;
    String hashtags;
    ShortsScriptStatus status;
    int targetDurationSeconds;
    int calculatedDurationSeconds;
    int sceneCount;
    List<SceneResponse> scenes;
    String reviewNotes;
    LocalDateTime approvedAt;
    LocalDateTime publishedAt;
    LocalDateTime createdAt;

    @Value
    @Builder
    public static class SceneResponse {
        int orderIndex;
        String text;
        String visualHint;
        int durationSeconds;
    }

    public static ShortsScriptResponse from(ShortsScript script) {
        List<SceneResponse> sceneResponses = script.getScenes().stream()
                .map(s -> SceneResponse.builder()
                        .orderIndex(s.getOrderIndex())
                        .text(s.getText())
                        .visualHint(s.getVisualHint())
                        .durationSeconds(s.getDurationSeconds())
                        .build())
                .toList();

        return ShortsScriptResponse.builder()
                .id(script.getId())
                .channelId(script.getChannel() != null ? script.getChannel().getId() : null)
                .channelName(script.getChannel() != null ? script.getChannel().getName() : null)
                .title(script.getTitle())
                .topic(script.getTopic())
                .hook(script.getHook())
                .cta(script.getCta())
                .hashtags(script.getHashtags())
                .status(script.getStatus())
                .targetDurationSeconds(script.getTargetDurationSeconds())
                .calculatedDurationSeconds(script.calculateTotalDuration())
                .sceneCount(script.getScenes().size())
                .scenes(sceneResponses)
                .reviewNotes(script.getReviewNotes())
                .approvedAt(script.getApprovedAt())
                .publishedAt(script.getPublishedAt())
                .createdAt(script.getCreatedAt())
                .build();
    }
}
