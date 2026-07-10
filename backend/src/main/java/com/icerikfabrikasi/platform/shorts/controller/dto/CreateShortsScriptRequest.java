package com.icerikfabrikasi.platform.shorts.controller.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.List;
import java.util.UUID;

/**
 * Yeni shorts script oluşturma request DTO.
 */
@Value
@Builder
@Jacksonized
public class CreateShortsScriptRequest {

    @NotNull(message = "Kanal ID zorunludur")
    UUID channelId;

    @NotBlank(message = "Konu zorunludur")
    @Size(max = 500, message = "Konu en fazla 500 karakter olabilir")
    String topic;

    @NotBlank(message = "Başlık zorunludur")
    @Size(max = 200, message = "Başlık en fazla 200 karakter olabilir")
    String title;

    @NotBlank(message = "Hook zorunludur")
    @Size(max = 500, message = "Hook en fazla 500 karakter olabilir")
    String hook;

    @Size(max = 300, message = "CTA en fazla 300 karakter olabilir")
    String cta;

    String hashtags;

    @NotNull(message = "Sahneler zorunludur")
    @Size(min = 3, max = 12, message = "En az 3, en fazla 12 sahne olmalıdır")
    @Valid
    List<SceneInput> scenes;

    @Value
    @Builder
    @Jacksonized
    public static class SceneInput {

        @NotBlank(message = "Sahne metni zorunludur")
        @Size(max = 500, message = "Sahne metni en fazla 500 karakter olabilir")
        String text;

        String visualHint;

        int durationSeconds;
    }
}
