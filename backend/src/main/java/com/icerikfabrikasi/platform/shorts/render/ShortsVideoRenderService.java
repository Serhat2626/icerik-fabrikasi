package com.icerikfabrikasi.platform.shorts.render;

import com.icerikfabrikasi.platform.shorts.render.ShortsRenderPlan.SceneRenderPlan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Profesyonel kalitede Shorts video render servisi.
 *
 * <p>Her sahne için: AI arka plan görsel + gradient karartma + modern büyük text.
 * Sahneler arası xfade geçişler + Ken Burns zoom efekti + arka plan müzik.</p>
 *
 * <p>İyileştirmeler v2:
 * <ul>
 *   <li>Büyük font boyutları (hook: 96px, body: 72px)</li>
 *   <li>Kanal bazlı font, renk ve stil konfigürasyonu</li>
 *   <li>Sahneler arası xfade geçiş efektleri (dissolve, fade, slide)</li>
 *   <li>Ken Burns zoom efekti — statik görsellere dinamik hareket</li>
 *   <li>Gradient overlay — modern, sinematik görünüm</li>
 *   <li>Watermark desteği</li>
 *   <li>Google Fonts otomatik indirme (fontfile= parametresi)</li>
 * </ul></p>
 */
@Service
@Slf4j
public class ShortsVideoRenderService {

    @Value("${shorts.render.output-dir:${java.io.tmpdir}/shorts-render}")
    private String outputDir;

    @Value("${shorts.render.ffmpeg-path:ffmpeg}")
    private String ffmpegPath;

    private final FontManager fontManager;

    public ShortsVideoRenderService(FontManager fontManager) {
        this.fontManager = fontManager;
    }

    /**
     * 1080px genişlik, fontsize=72 → ~22 karakter/satır güvenli.
     * Padding bırakarak taşmayı önler.
     */
    private static final int MAX_CHARS_PER_LINE = 22;

    /** Varsayılan font — indirme başarısız olursa */
    private static final String FALLBACK_FONT_NAME = "Segoe UI Bold";

    public ShortsRenderResult render(ShortsRenderPlan plan) {
        log.info("Shorts video render başlıyor: {} sahne, ~{} saniye, transition={}, kenBurns={}",
                plan.getScenes().size(), plan.totalDurationSeconds(),
                plan.getTransitionType(), plan.isKenBurnsEnabled());

        try {
            Path workDir = prepareWorkDir();
            List<Path> sceneClips = new ArrayList<>();

            int idx = 0;
            for (SceneRenderPlan scene : plan.getScenes()) {
                log.info("  Sahne {}/{} render ediliyor...", ++idx, plan.getScenes().size());
                Path clipPath = renderSceneClip(workDir, plan, scene);
                sceneClips.add(clipPath);
            }

            Path finalOutput;
            if (plan.getTransitionType() != null && plan.getTransitionType() != TransitionType.NONE
                    && sceneClips.size() > 1) {
                finalOutput = concatenateWithTransitions(workDir, sceneClips, plan);
            } else {
                finalOutput = concatenateClips(workDir, sceneClips, plan);
            }

            long fileSize = Files.exists(finalOutput) ? Files.size(finalOutput) : 0;

            log.info("Shorts video render tamamlandı: path={}, size={}KB",
                    finalOutput, fileSize / 1024);

            return ShortsRenderResult.success(
                    finalOutput.toString(),
                    plan.totalDurationSeconds(),
                    fileSize,
                    plan.getResolution()
            );
        } catch (Exception e) {
            log.error("Shorts video render hatası: {}", e.getMessage(), e);
            return ShortsRenderResult.failure("Render hatası: " + e.getMessage());
        }
    }

    /**
     * Tek sahne clip:
     * - Arka plan görsel varsa: Ken Burns zoom + gradient overlay + modern text
     * - Yoksa: koyu arka plan + text
     */
    private Path renderSceneClip(Path workDir, ShortsRenderPlan plan, SceneRenderPlan scene)
            throws IOException, InterruptedException {

        // Countdown sahnesi ise özel render
        if (scene.isCountdown()) {
            return renderCountdownClip(workDir, plan, scene);
        }

        Path outputPath = workDir.resolve(
                String.format("scene_%03d.mp4", scene.getOrderIndex()));

        String cleanText = sanitizeText(scene.getText());
        String wrappedText = wrapText(cleanText, MAX_CHARS_PER_LINE);

        int fontSize = scene.isHook() ? plan.getHookFontSize() : plan.getBodyFontSize();
        int dur = scene.getDurationSeconds();
        String bgColor = hexToFfmpeg(plan.getBackgroundColor());
        int fadeOutStart = Math.max(1, dur - 1);

        boolean hasImage = scene.getBackgroundImagePath() != null
                && Files.exists(Path.of(scene.getBackgroundImagePath()));

        boolean hasVoice = scene.getVoiceoverPath() != null
                && Files.exists(Path.of(scene.getVoiceoverPath()));

        // Font çözümleme — önce indirmeyi dene, başarısızsa sistem fontu
        String fontPath = fontManager.getFontPath(plan.getFontFamily());

        List<String> cmd = new ArrayList<>();
        cmd.add(ffmpegPath);
        cmd.add("-y");

        if (hasImage) {
            // Input 0: arka plan görseli
            cmd.addAll(List.of("-loop", "1", "-t", String.valueOf(dur), "-i", scene.getBackgroundImagePath()));
        } else {
            // Input 0: düz renk arka plan
            String colorFilter = String.format(
                    "color=c=%s:s=1080x1920:d=%d:r=%d,format=yuv420p",
                    bgColor, dur, plan.getFps()
            );
            cmd.addAll(List.of("-f", "lavfi", "-t", String.valueOf(dur), "-i", colorFilter));
        }

        // Input 1 (opsiyonel): seslendirme
        if (hasVoice) {
            cmd.addAll(List.of("-i", scene.getVoiceoverPath()));
        }

        // Video filtresi — Ken Burns + gradient + modern text overlay
        String videoFilter = buildVideoFilter(
                wrappedText, fontSize, dur, fadeOutStart,
                hasImage, bgColor, plan, fontPath
        );

        cmd.addAll(List.of("-vf", videoFilter));
        cmd.addAll(List.of("-c:v", "libx264", "-preset", "medium", "-crf", "20"));
        cmd.addAll(List.of("-pix_fmt", "yuv420p"));

        if (hasVoice) {
            cmd.addAll(List.of("-c:a", "aac", "-b:a", "128k"));
            cmd.addAll(List.of("-map", "0:v", "-map", "1:a"));
            cmd.addAll(List.of("-shortest"));
        } else {
            cmd.addAll(List.of("-an"));
        }

        cmd.addAll(List.of("-t", String.valueOf(dur)));
        cmd.add(outputPath.toString());

        executeCommand(cmd);
        return outputPath;
    }

    /**
     * Modern video filter zinciri:
     * <ol>
     *   <li>Scale/Crop → 1080x1920</li>
     *   <li>Ken Burns zoom efekti (opsiyonel)</li>
     *   <li>Gradient overlay — alt %60'ta yarı-saydam siyah</li>
     *   <li>Büyük, modern font ile text overlay</li>
     *   <li>Watermark (opsiyonel)</li>
     *   <li>Fade in/out</li>
     * </ol>
     */
    private String buildVideoFilter(String wrappedText, int fontSize, int dur,
                                     int fadeOutStart, boolean hasImage, String bgColor,
                                     ShortsRenderPlan plan, String fontPath) {
        String[] lines = wrappedText.split("\n");
        StringBuilder filter = new StringBuilder();

        // Font parametresi — fontfile varsa onu kullan, yoksa font adı
        String fontParam;
        if (fontPath != null) {
            // Windows yollarında backslash'leri escape et
            String escapedPath = fontPath.replace("\\", "/").replace(":", "\\:");
            fontParam = "fontfile='" + escapedPath + "'";
        } else {
            fontParam = "font='" + FALLBACK_FONT_NAME + "'";
        }

        // Metin rengi
        String textColor = plan.getTextColor() != null ? plan.getTextColor().replace("#", "") : "FFFFFF";

        if (hasImage) {
            // 1. Scale + Crop
            filter.append("scale=1080:1920:force_original_aspect_ratio=increase,crop=1080:1920,");

            // 2. Ken Burns zoom efekti
            if (plan.isKenBurnsEnabled()) {
                double zoomStep = (plan.getKenBurnsZoom() - 1.0) / (dur * plan.getFps());
                filter.append(String.format(
                        "zoompan=z='min(zoom+%.6f,%.2f)':x='iw/2-(iw/zoom/2)':y='ih/2-(ih/zoom/2)':d=%d:s=1080x1920:fps=%d,",
                        zoomStep, plan.getKenBurnsZoom(), dur * plan.getFps(), plan.getFps()
                ));
            }

            // 3. Gradient overlay — üst %25 ve alt %55 karartma (metin alanı)
            // Üst kısım: watermark için hafif karartma
            filter.append("drawbox=x=0:y=0:w=iw:h=ih*0.15:color=black@0.3:t=fill,");
            // Alt kısım: metin alanı için güçlü karartma
            filter.append("drawbox=x=0:y=ih*0.40:w=iw:h=ih*0.60:color=black@0.55:t=fill,");

        } else {
            // Düz renk arka plan için hafif overlay
            filter.append("drawbox=x=0:y=ih/2-350:w=iw:h=700:color=black@0.25:t=fill,");
        }

        // 4. Text overlay — ekranın merkezine yerleştir
        int totalLines = lines.length;
        int lineSpacing = fontSize + 24;
        int baseY = 960; // Ekranın tam ortası (1920/2)
        int startY = baseY - (totalLines * lineSpacing / 2);

        for (int i = 0; i < lines.length; i++) {
            String escapedLine = escapeForDrawtext(lines[i].trim());
            int y = startY + (i * lineSpacing);

            filter.append(String.format(
                    "drawtext=text='%s':" +
                    "%s:" +
                    "fontsize=%d:" +
                    "fontcolor=0x%s:" +
                    "borderw=5:bordercolor=black@0.85:" +
                    "x=(w-text_w)/2:" +
                    "y=%d:" +
                    "shadowcolor=black@0.6:shadowx=4:shadowy=4",
                    escapedLine, fontParam, fontSize, textColor, y
            ));

            filter.append(",");
        }

        // 5. Watermark (opsiyonel)
        if (plan.getWatermarkText() != null && !plan.getWatermarkText().isBlank()) {
            String escapedWatermark = escapeForDrawtext(plan.getWatermarkText());
            filter.append(String.format(
                    "drawtext=text='%s':" +
                    "%s:" +
                    "fontsize=24:" +
                    "fontcolor=white@0.5:" +
                    "x=30:y=40",
                    escapedWatermark, fontParam
            ));
            filter.append(",");
        }

        // 6. Fade in/out
        filter.append(String.format(
                "fade=t=in:st=0:d=0.4,fade=t=out:st=%d:d=0.4",
                fadeOutStart
        ));

        return filter.toString();
    }

    /**
     * Sahne clip'lerini xfade geçiş efektleriyle birleştirir.
     *
     * <p>FFmpeg xfade filtresini kullanarak sahneler arasında
     * dissolve, fade, slide gibi geçişler uygular.</p>
     */
    private Path concatenateWithTransitions(Path workDir, List<Path> clips, ShortsRenderPlan plan)
            throws IOException, InterruptedException {

        if (clips.size() == 1) {
            return addAudioToSingleClip(workDir, clips.get(0), plan);
        }

        Path finalOutput = workDir.resolve("final_" + UUID.randomUUID() + ".mp4");
        TransitionType transition = plan.getTransitionType();
        double transDur = plan.getTransitionDuration();

        // Sahne sürelerini topla (xfade offset hesabı için)
        List<Double> durations = new ArrayList<>();
        for (SceneRenderPlan scene : plan.getScenes()) {
            durations.add((double) scene.getDurationSeconds());
        }

        List<String> cmd = new ArrayList<>();
        cmd.add(ffmpegPath);
        cmd.add("-y");

        // Tüm clip'leri input olarak ekle
        for (Path clip : clips) {
            cmd.addAll(List.of("-i", clip.toString()));
        }

        // Müzik input'u
        boolean hasMusic = plan.getBackgroundMusicPath() != null
                && Files.exists(Path.of(plan.getBackgroundMusicPath()));
        int musicInputIdx = -1;
        if (hasMusic) {
            cmd.addAll(List.of("-i", plan.getBackgroundMusicPath()));
            musicInputIdx = clips.size();
        }

        // xfade filter chain oluştur
        StringBuilder filterComplex = new StringBuilder();
        double cumulativeOffset = 0;

        // İlk iki clip'i birleştir
        if (clips.size() >= 2) {
            cumulativeOffset = durations.get(0) - transDur;
            String xfadeFilter = transition.getFfmpegName() != null
                    ? String.format("xfade=transition=%s:duration=%.1f:offset=%.1f",
                            transition.getFfmpegName(), transDur, cumulativeOffset)
                    : "";

            if (!xfadeFilter.isEmpty()) {
                filterComplex.append(String.format("[0:v][1:v]%s[v01]", xfadeFilter));
            }

            // Sonraki clip'leri zincirle
            for (int i = 2; i < clips.size(); i++) {
                cumulativeOffset += durations.get(i - 1) - transDur;
                String prevLabel = String.format("[v%02d]", i - 2) + String.valueOf(i - 1);
                // Etiket düzelt
                String inLabel = (i == 2) ? "[v01]" : "[v" + String.format("%02d", i - 1) + "]";
                String outLabel = "[v" + String.format("%02d", i) + "]";

                String nextXfade = transition.getFfmpegName() != null
                        ? String.format("xfade=transition=%s:duration=%.1f:offset=%.1f",
                                transition.getFfmpegName(), transDur, cumulativeOffset)
                        : "";

                if (!nextXfade.isEmpty()) {
                    filterComplex.append("; ");
                    filterComplex.append(String.format("%s[%d:v]%s%s",
                            inLabel, i, nextXfade, outLabel));
                }
            }
        }

        // Son video etiketini belirle
        String lastVideoLabel = clips.size() <= 1 ? "[0:v]"
                : "[v" + String.format("%02d", clips.size() - 1) + "]";

        // Audio handling
        boolean hasVoiceover = plan.getScenes().stream()
                .anyMatch(s -> s.getVoiceoverPath() != null);

        if (hasVoiceover) {
            // Tüm audio stream'leri concat et
            filterComplex.append("; ");
            for (int i = 0; i < clips.size(); i++) {
                filterComplex.append(String.format("[%d:a]", i));
            }
            filterComplex.append(String.format("concat=n=%d:v=0:a=1[voice]", clips.size()));

            if (hasMusic) {
                int totalDur = plan.totalDurationSeconds();
                filterComplex.append(String.format(
                        "; [%d:a]volume=%.2f,afade=t=out:st=%d:d=3[music]",
                        musicInputIdx, plan.getMusicVolume(),
                        Math.max(1, totalDur - 3)));
                filterComplex.append("; [voice][music]amix=inputs=2:duration=first[aout]");
                cmd.addAll(List.of("-filter_complex", filterComplex.toString()));
                cmd.addAll(List.of("-map", lastVideoLabel, "-map", "[aout]"));
            } else {
                cmd.addAll(List.of("-filter_complex", filterComplex.toString()));
                cmd.addAll(List.of("-map", lastVideoLabel, "-map", "[voice]"));
            }
        } else if (hasMusic) {
            int totalDur = plan.totalDurationSeconds();
            filterComplex.append(String.format(
                    "; [%d:a]volume=%.2f,afade=t=out:st=%d:d=3[music]",
                    musicInputIdx, plan.getMusicVolume(),
                    Math.max(1, totalDur - 3)));
            cmd.addAll(List.of("-filter_complex", filterComplex.toString()));
            cmd.addAll(List.of("-map", lastVideoLabel, "-map", "[music]"));
        } else {
            if (filterComplex.length() > 0) {
                cmd.addAll(List.of("-filter_complex", filterComplex.toString()));
                cmd.addAll(List.of("-map", lastVideoLabel));
            }
            cmd.addAll(List.of("-an"));
        }

        cmd.addAll(List.of("-c:v", "libx264", "-preset", "medium", "-crf", "20"));
        cmd.addAll(List.of("-c:a", "aac", "-b:a", "128k"));
        cmd.addAll(List.of("-pix_fmt", "yuv420p"));
        cmd.addAll(List.of("-movflags", "+faststart"));
        cmd.addAll(List.of("-shortest"));
        cmd.add(finalOutput.toString());

        executeCommand(cmd);
        return finalOutput;
    }

    /**
     * Tek clip'e audio ekler (sadece 1 sahne varsa).
     */
    private Path addAudioToSingleClip(Path workDir, Path clip, ShortsRenderPlan plan)
            throws IOException, InterruptedException {

        boolean hasMusic = plan.getBackgroundMusicPath() != null
                && Files.exists(Path.of(plan.getBackgroundMusicPath()));

        if (!hasMusic) return clip;

        Path finalOutput = workDir.resolve("final_" + UUID.randomUUID() + ".mp4");
        List<String> cmd = new ArrayList<>();
        cmd.add(ffmpegPath);
        cmd.add("-y");
        cmd.addAll(List.of("-i", clip.toString()));
        cmd.addAll(List.of("-i", plan.getBackgroundMusicPath()));

        int totalDur = plan.totalDurationSeconds();
        cmd.addAll(List.of("-filter_complex",
                String.format("[1:a]volume=%.2f,afade=t=out:st=%d:d=3[music]",
                        plan.getMusicVolume(), Math.max(1, totalDur - 3))));
        cmd.addAll(List.of("-map", "0:v", "-map", "[music]"));
        cmd.addAll(List.of("-c:v", "copy", "-c:a", "aac"));
        cmd.addAll(List.of("-shortest"));
        cmd.addAll(List.of("-movflags", "+faststart"));
        cmd.add(finalOutput.toString());

        executeCommand(cmd);
        return finalOutput;
    }

    /**
     * Sahne clip'lerini basit concat ile birleştirir (transition=NONE için fallback).
     * Varsa müzik ekler.
     */
    private Path concatenateClips(Path workDir, List<Path> clips, ShortsRenderPlan plan)
            throws IOException, InterruptedException {

        Path concatList = workDir.resolve("concat_list.txt");
        List<String> lines = clips.stream()
                .map(p -> "file '" + p.toAbsolutePath().toString().replace("\\", "/") + "'")
                .toList();
        Files.write(concatList, lines);

        Path finalOutput = workDir.resolve("final_" + UUID.randomUUID() + ".mp4");

        List<String> cmd = new ArrayList<>();
        cmd.add(ffmpegPath);
        cmd.add("-y");
        cmd.addAll(List.of("-f", "concat", "-safe", "0", "-i", concatList.toString()));

        // Seslendirme var mı kontrol et (en az bir sahne'de audio stream olmalı)
        boolean hasVoiceover = plan.getScenes().stream()
                .anyMatch(s -> s.getVoiceoverPath() != null);

        boolean hasMusic = plan.getBackgroundMusicPath() != null
                && Files.exists(Path.of(plan.getBackgroundMusicPath()));

        if (hasVoiceover && hasMusic) {
            // Voiceover + müzik — ikisini mixle
            cmd.addAll(List.of("-i", plan.getBackgroundMusicPath()));
            cmd.addAll(List.of("-filter_complex",
                    String.format("[0:a]volume=1.0[voice];[1:a]volume=%.2f,afade=t=out:st=%d:d=3[music];" +
                                    "[voice][music]amix=inputs=2:duration=first[aout]",
                            plan.getMusicVolume(),
                            Math.max(1, plan.totalDurationSeconds() - 3))));
            cmd.addAll(List.of("-map", "0:v", "-map", "[aout]"));
            cmd.addAll(List.of("-shortest"));
        } else if (hasVoiceover) {
            // Sadece seslendirme — copy audio
            cmd.addAll(List.of("-c:v", "copy", "-c:a", "aac"));
        } else if (hasMusic) {
            // Sadece müzik
            cmd.addAll(List.of("-i", plan.getBackgroundMusicPath()));
            cmd.addAll(List.of("-filter_complex",
                    String.format("[1:a]volume=%.2f,afade=t=out:st=%d:d=3[music]",
                            plan.getMusicVolume(),
                            Math.max(1, plan.totalDurationSeconds() - 3))));
            cmd.addAll(List.of("-map", "0:v", "-map", "[music]"));
            cmd.addAll(List.of("-shortest"));
        } else {
            // Ne seslendirme ne müzik
            cmd.addAll(List.of("-c:v", "copy"));
            cmd.addAll(List.of("-an"));
        }

        cmd.addAll(List.of("-movflags", "+faststart"));
        cmd.add(finalOutput.toString());

        executeCommand(cmd);
        return finalOutput;
    }

    /**
     * Countdown sahnesi render — 3... 2... 1... animasyonlu.
     * Her rakam 1 saniye gösterilir, büyük font, dramatik.
     */
    private Path renderCountdownClip(Path workDir, ShortsRenderPlan plan, SceneRenderPlan scene)
            throws IOException, InterruptedException {

        Path outputPath = workDir.resolve(
                String.format("scene_%03d.mp4", scene.getOrderIndex()));

        int dur = 4; // 3 + 2 + 1 + kısa bekleme
        String bgColor = hexToFfmpeg(plan.getBackgroundColor());
        int countdownFontSize = 140;

        // Font çözümleme
        String fontPath = fontManager.getFontPath(plan.getFontFamily());
        String fontParam;
        if (fontPath != null) {
            String escapedPath = fontPath.replace("\\", "/").replace(":", "\\:");
            fontParam = "fontfile='" + escapedPath + "'";
        } else {
            fontParam = "font='" + FALLBACK_FONT_NAME + "'";
        }

        boolean hasImage = scene.getBackgroundImagePath() != null
                && Files.exists(Path.of(scene.getBackgroundImagePath()));

        String textColor = plan.getTextColor() != null ? plan.getTextColor().replace("#", "") : "FFFFFF";
        String accentColorHex = plan.getAccentColor() != null ? plan.getAccentColor().replace("#", "") : "FFD700";

        List<String> cmd = new ArrayList<>();
        cmd.add(ffmpegPath);
        cmd.add("-y");

        if (hasImage) {
            cmd.addAll(List.of("-loop", "1", "-t", String.valueOf(dur), "-i", scene.getBackgroundImagePath()));
        } else {
            String gradientFilter = String.format(
                    "color=c=%s:s=1080x1920:d=%d:r=%d,format=yuv420p",
                    bgColor, dur, plan.getFps()
            );
            cmd.addAll(List.of("-f", "lavfi", "-t", String.valueOf(dur), "-i", gradientFilter));
        }

        // Countdown filter: 3 → 2 → 1 — accent renkli, büyük font
        StringBuilder filter = new StringBuilder();
        if (hasImage) {
            filter.append("scale=1080:1920:force_original_aspect_ratio=increase,crop=1080:1920,");
            filter.append("drawbox=x=0:y=ih*0.30:w=iw:h=ih*0.40:color=black@0.6:t=fill,");
        }

        // "3" — 0-1 saniye
        filter.append(String.format(
                "drawtext=text='3':%s:fontsize=%d:fontcolor=0x%s:" +
                "borderw=6:bordercolor=black@0.9:" +
                "x=(w-text_w)/2:y=(h-text_h)/2:" +
                "enable='between(t,0,1)',",
                fontParam, countdownFontSize, accentColorHex
        ));

        // "2" — 1-2 saniye
        filter.append(String.format(
                "drawtext=text='2':%s:fontsize=%d:fontcolor=0x%s:" +
                "borderw=6:bordercolor=black@0.9:" +
                "x=(w-text_w)/2:y=(h-text_h)/2:" +
                "enable='between(t,1,2)',",
                fontParam, countdownFontSize, accentColorHex
        ));

        // "1" — 2-3 saniye
        filter.append(String.format(
                "drawtext=text='1':%s:fontsize=%d:fontcolor=0x%s:" +
                "borderw=6:bordercolor=black@0.9:" +
                "x=(w-text_w)/2:y=(h-text_h)/2:" +
                "enable='between(t,2,3)',",
                fontParam, countdownFontSize, accentColorHex
        ));

        // Fade in/out
        filter.append("fade=t=in:st=0:d=0.3,fade=t=out:st=3:d=0.5");

        cmd.addAll(List.of("-vf", filter.toString()));
        cmd.addAll(List.of("-c:v", "libx264", "-preset", "medium", "-crf", "20"));
        cmd.addAll(List.of("-pix_fmt", "yuv420p"));
        cmd.addAll(List.of("-an"));
        cmd.addAll(List.of("-t", String.valueOf(dur)));
        cmd.add(outputPath.toString());

        executeCommand(cmd);
        return outputPath;
    }

    private Path prepareWorkDir() throws IOException {
        Path dir = Path.of(outputDir, UUID.randomUUID().toString());
        Files.createDirectories(dir);
        return dir;
    }

    private void executeCommand(List<String> command) throws IOException, InterruptedException {
        log.info("FFmpeg komutu: {}", String.join(" ", command));
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process process = pb.start();
        String output = new String(process.getInputStream().readAllBytes());
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            log.error("FFmpeg hata: exitCode={}, output={}", exitCode,
                    output.substring(Math.max(0, output.length() - 800)));
            throw new RuntimeException("FFmpeg hatası (exit: " + exitCode + "): " +
                    output.substring(Math.max(0, output.length() - 400)));
        }
    }

    private String hexToFfmpeg(String hex) {
        if (hex == null) return "0x1A1A2E";
        return hex.replace("#", "0x");
    }

    /** Text'ten emoji ve sorunlu karakterleri temizle */
    private String sanitizeText(String text) {
        if (text == null) return "";
        return text.replaceAll("[^\\x20-\\x7E]", "")
                   .replace("'", "")
                   .replace("\"", "")
                   .replace("%", " percent")
                   .replace("$", "")
                   .trim();
    }

    /** FFmpeg drawtext için metni escape et */
    private String escapeForDrawtext(String text) {
        if (text == null) return "";
        return text
                .replace("\\", "\\\\")
                .replace(":", "\\:")
                .replace("'", "\\'");
    }

    /**
     * Uzun text'i satırlara böler.
     * Her satır maxChars'ı geçmez — ekran taşması önlenir.
     */
    private String wrapText(String text, int maxChars) {
        if (text == null || text.length() <= maxChars) return text;

        StringBuilder result = new StringBuilder();
        String[] words = text.split("\\s+");
        int lineLen = 0;

        for (String word : words) {
            if (lineLen + word.length() + 1 > maxChars && lineLen > 0) {
                result.append("\n");
                lineLen = 0;
            }
            if (lineLen > 0) {
                result.append(" ");
                lineLen++;
            }
            result.append(word);
            lineLen += word.length();
        }

        return result.toString();
    }
}
