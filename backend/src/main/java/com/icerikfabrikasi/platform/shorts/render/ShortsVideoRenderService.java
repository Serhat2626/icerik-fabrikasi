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
 * <p>Her sahne için: stock görsel arka plan + karartma + modern text kutusu.
 * Sahneler arası concat + arka plan müzik.</p>
 */
@Service
@Slf4j
public class ShortsVideoRenderService {

    @Value("${shorts.render.output-dir:${java.io.tmpdir}/shorts-render}")
    private String outputDir;

    @Value("${shorts.render.ffmpeg-path:ffmpeg}")
    private String ffmpegPath;

    private static final int MAX_CHARS_PER_LINE = 26;

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
     * - Arka plan görsel varsa: görsel + karartma + text kutusu
     * - Yoksa: gradient arka plan + text
     */
    private Path renderSceneClip(Path workDir, ShortsRenderPlan plan, SceneRenderPlan scene)
            throws IOException, InterruptedException {

        Path outputPath = workDir.resolve(
                String.format("scene_%03d.mp4", scene.getOrderIndex()));

        String cleanText = sanitizeText(scene.getText());
        String wrappedText = wrapText(cleanText, MAX_CHARS_PER_LINE);
        String escapedText = escapeForDrawtext(wrappedText);

        int fontSize = scene.isHook() ? 52 : 42;
        String textColor = scene.isHook() ? "0xFFD700" : "0xFFFFFF";
        int dur = scene.getDurationSeconds();
        String bgColor = hexToFfmpeg(plan.getBackgroundColor());
        int fadeOutStart = Math.max(1, dur - 1);

        boolean hasImage = scene.getBackgroundImagePath() != null
                && Files.exists(Path.of(scene.getBackgroundImagePath()));

        List<String> cmd = new ArrayList<>();
        cmd.add(ffmpegPath);
        cmd.add("-y");

        if (hasImage) {
            // Arka plan: stock görsel + karartma overlay + modern text kutusu
            cmd.addAll(List.of("-loop", "1", "-i", scene.getBackgroundImagePath()));
            cmd.addAll(List.of("-t", String.valueOf(dur)));

            // Filter: scale → crop 1080x1920 → karartma → text box → fade
            String filter = String.format(
                    "[0:v]scale=1080:1920:force_original_aspect_ratio=increase,crop=1080:1920," +
                    // Yarı saydam karartma — text okunabilirliği için
                    "drawbox=x=0:y=ih/2-200:w=iw:h=400:color=black@0.6:t=fill," +
                    // Text — kutu arka planlı modern görünüm
                    "drawtext=text='%s':" +
                    "fontsize=%d:fontcolor=%s:" +
                    "x=(w-text_w)/2:y=(h-text_h)/2:" +
                    "shadowcolor=black@0.9:shadowx=3:shadowy=3:" +
                    "line_spacing=14," +
                    // Fade in/out
                    "fade=t=in:st=0:d=0.5,fade=t=out:st=%d:d=0.5",
                    escapedText, fontSize, textColor, fadeOutStart
            );

            cmd.addAll(List.of("-vf", filter));
        } else {
            // Gradient arka plan
            String gradientFilter = String.format(
                    "color=c=%s:s=1080x1920:d=%d:r=%d,format=yuv420p," +
                    "drawbox=x=0:y=ih/2-200:w=iw:h=400:color=black@0.3:t=fill," +
                    "drawtext=text='%s':" +
                    "fontsize=%d:fontcolor=%s:" +
                    "x=(w-text_w)/2:y=(h-text_h)/2:" +
                    "shadowcolor=black@0.7:shadowx=2:shadowy=2:" +
                    "line_spacing=14," +
                    "fade=t=in:st=0:d=0.5,fade=t=out:st=%d:d=0.5",
                    bgColor, dur, plan.getFps(),
                    escapedText, fontSize, textColor, fadeOutStart
            );

            cmd.addAll(List.of("-f", "lavfi", "-i", gradientFilter));
        }

        cmd.addAll(List.of("-c:v", "libx264", "-preset", "ultrafast", "-crf", "23"));
        cmd.addAll(List.of("-pix_fmt", "yuv420p"));
        cmd.addAll(List.of("-t", String.valueOf(dur)));
        cmd.add(outputPath.toString());

        executeCommand(cmd);
        return outputPath;
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

        boolean hasMusic = plan.getBackgroundMusicPath() != null
                && Files.exists(Path.of(plan.getBackgroundMusicPath()));

        if (hasMusic) {
            cmd.addAll(List.of("-i", plan.getBackgroundMusicPath()));
            cmd.addAll(List.of("-filter_complex",
                    String.format("[1:a]volume=%.2f,afade=t=out:st=%d:d=3[music]",
                            plan.getMusicVolume(),
                            Math.max(1, plan.totalDurationSeconds() - 3))));
            cmd.addAll(List.of("-map", "0:v", "-map", "[music]"));
            cmd.addAll(List.of("-shortest"));
        } else {
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
                .replace("\n", "\\n");
    }

    /**
     * Uzun text'i satırlara böler (FFmpeg drawtext \n desteği).
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
