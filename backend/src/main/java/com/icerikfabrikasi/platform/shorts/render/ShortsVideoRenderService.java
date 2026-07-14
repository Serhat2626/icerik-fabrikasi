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
 * <p>Her sahne için: AI arka plan görsel + yarı-saydam kutu + modern beyaz text.
 * Sahneler arası concat + arka plan müzik.</p>
 */
@Service
@Slf4j
public class ShortsVideoRenderService {

    @Value("${shorts.render.output-dir:${java.io.tmpdir}/shorts-render}")
    private String outputDir;

    @Value("${shorts.render.ffmpeg-path:ffmpeg}")
    private String ffmpegPath;

    /**
     * 1080px genişlik, fontsize=38 → ~20 karakter/satır güvenli.
     * Padding bırakarak taşmayı önler.
     */
    private static final int MAX_CHARS_PER_LINE = 22;

    public ShortsRenderResult render(ShortsRenderPlan plan) {
        log.info("Shorts video render başlıyor: {} sahne, ~{} saniye",
                plan.getScenes().size(), plan.totalDurationSeconds());

        try {
            Path workDir = prepareWorkDir();
            List<Path> sceneClips = new ArrayList<>();

            int idx = 0;
            for (SceneRenderPlan scene : plan.getScenes()) {
                log.info("  Sahne {}/{} render ediliyor...", ++idx, plan.getScenes().size());
                Path clipPath = renderSceneClip(workDir, plan, scene);
                sceneClips.add(clipPath);
            }

            Path finalOutput = concatenateClips(workDir, sceneClips, plan);

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
     * - Arka plan görsel varsa: görsel + alt bölgede gradient karartma + modern text
     * - Yoksa: koyu arka plan + text
     */
    private Path renderSceneClip(Path workDir, ShortsRenderPlan plan, SceneRenderPlan scene)
            throws IOException, InterruptedException {

        Path outputPath = workDir.resolve(
                String.format("scene_%03d.mp4", scene.getOrderIndex()));

        String cleanText = sanitizeText(scene.getText());
        String wrappedText = wrapText(cleanText, MAX_CHARS_PER_LINE);

        int fontSize = scene.isHook() ? 46 : 38;
        int dur = scene.getDurationSeconds();
        String bgColor = hexToFfmpeg(plan.getBackgroundColor());
        int fadeOutStart = Math.max(1, dur - 1);

        boolean hasImage = scene.getBackgroundImagePath() != null
                && Files.exists(Path.of(scene.getBackgroundImagePath()));

        boolean hasVoice = scene.getVoiceoverPath() != null
                && Files.exists(Path.of(scene.getVoiceoverPath()));

        List<String> cmd = new ArrayList<>();
        cmd.add(ffmpegPath);
        cmd.add("-y");

        if (hasImage) {
            // Input 0: arka plan görseli
            cmd.addAll(List.of("-loop", "1", "-t", String.valueOf(dur), "-i", scene.getBackgroundImagePath()));
        } else {
            // Input 0: gradient arka plan
            String gradientFilter = String.format(
                    "color=c=%s:s=1080x1920:d=%d:r=%d,format=yuv420p",
                    bgColor, dur, plan.getFps()
            );
            cmd.addAll(List.of("-f", "lavfi", "-t", String.valueOf(dur), "-i", gradientFilter));
        }

        // Input 1 (opsiyonel): seslendirme
        if (hasVoice) {
            cmd.addAll(List.of("-i", scene.getVoiceoverPath()));
        }

        // Video filtresi — modern text overlay
        String videoFilter = buildVideoFilter(wrappedText, fontSize, dur, fadeOutStart, hasImage, bgColor, plan.getFps());

        cmd.addAll(List.of("-vf", videoFilter));
        cmd.addAll(List.of("-c:v", "libx264", "-preset", "ultrafast", "-crf", "23"));
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
     * Modern text overlay filter:
     * - Görsel arka planda: alt kısımda gradient karartma + beyaz text
     * - Düz arka planda: ortada text
     */
    private String buildVideoFilter(String wrappedText, int fontSize, int dur,
                                     int fadeOutStart, boolean hasImage, String bgColor, int fps) {
        // Satırları ayrı drawtext olarak ekle — FFmpeg \n desteği sorunlu
        String[] lines = wrappedText.split("\n");
        StringBuilder filter = new StringBuilder();

        if (hasImage) {
            // Görsel: scale + crop + alt kısımda gradient karartma
            filter.append("scale=1080:1920:force_original_aspect_ratio=increase,crop=1080:1920,");

            // Alt kısımda gradient black overlay (y=1200'den aşağısı)
            filter.append("drawbox=x=0:y=1100:w=iw:h=820:color=black@0.55:t=fill,");
        } else {
            // Düz arka plan — orta bölgede hafif kutu
            filter.append("drawbox=x=0:y=ih/2-200:w=iw:h=400:color=black@0.3:t=fill,");
        }

        // Her satır için ayrı drawtext — taşma ve \n sorunu olmaz
        int totalLines = lines.length;
        int lineSpacing = fontSize + 16; // satır aralığı
        // Konumlandırma: görsel varsa alt kısımda (y=1250 civarı), yoksa ortada
        int baseY = hasImage ? 1280 : 960;
        int startY = baseY - (totalLines * lineSpacing / 2);

        for (int i = 0; i < lines.length; i++) {
            String escapedLine = escapeForDrawtext(lines[i].trim());
            int y = startY + (i * lineSpacing);

            filter.append(String.format(
                    "drawtext=text='%s':" +
                    "fontsize=%d:" +
                    "fontcolor=white:" +
                    "borderw=2:bordercolor=black@0.8:" +
                    "x=(w-text_w)/2:" +
                    "y=%d:" +
                    "shadowcolor=black@0.6:shadowx=2:shadowy=2",
                    escapedLine, fontSize, y
            ));

            if (i < lines.length - 1) {
                filter.append(",");
            }
        }

        // Fade in/out
        filter.append(String.format(
                ",fade=t=in:st=0:d=0.5,fade=t=out:st=%d:d=0.5",
                fadeOutStart
        ));

        return filter.toString();
    }

    /**
     * Sahne clip'lerini birleştirir, varsa müzik ekler.
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
