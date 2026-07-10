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
 * FFmpeg ile Shorts video render servisi.
 *
 * <p>ShortsRenderPlan'dan FFmpeg komutları üretir ve çalıştırır.
 * Her sahne için: arka plan renk/görsel + text overlay → scene clip.
 * Tüm scene clip'leri birleştirilerek final video üretilir.</p>
 *
 * <p>Gereksinimler: FFmpeg sistemde kurulu olmalı.</p>
 */
@Service
@Slf4j
public class ShortsVideoRenderService {

    @Value("${shorts.render.output-dir:${java.io.tmpdir}/shorts-render}")
    private String outputDir;

    @Value("${shorts.render.ffmpeg-path:ffmpeg}")
    private String ffmpegPath;

    /**
     * Render planını çalıştırır ve video dosyası üretir.
     */
    public ShortsRenderResult render(ShortsRenderPlan plan) {
        log.info("Shorts video render başlıyor: {} sahne, ~{} saniye",
                plan.getScenes().size(), plan.totalDurationSeconds());

        try {
            Path workDir = prepareWorkDir();
            List<Path> sceneClips = new ArrayList<>();

            // 1. Her sahne için clip üret
            for (SceneRenderPlan scene : plan.getScenes()) {
                Path clipPath = renderSceneClip(workDir, plan, scene);
                sceneClips.add(clipPath);
            }

            // 2. Clip'leri birleştir
            Path concatList = createConcatList(workDir, sceneClips);
            Path finalOutput = workDir.resolve("final_" + UUID.randomUUID() + ".mp4");
            concatenateClips(concatList, finalOutput, plan);

            // 3. Sonuç
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
     * Tek bir sahne clip'i üretir: renkli arka plan + text overlay.
     */
    private Path renderSceneClip(Path workDir, ShortsRenderPlan plan, SceneRenderPlan scene)
            throws IOException, InterruptedException {

        Path outputPath = workDir.resolve(
                String.format("scene_%03d_%s.mp4", scene.getOrderIndex(), UUID.randomUUID()));

        int fontSize = scene.isHook() ? plan.getHookFontSize() : plan.getBodyFontSize();
        String textColor = scene.isHook() ? hexToFfmpeg(plan.getAccentColor()) : hexToFfmpeg(plan.getTextColor());

        // FFmpeg: renkli arka plan + text overlay
        List<String> cmd = new ArrayList<>();
        cmd.add(ffmpegPath);
        cmd.add("-y");

        // Arka plan: düz renk veya görsel
        if (scene.getBackgroundImagePath() != null) {
            cmd.addAll(List.of("-loop", "1", "-i", scene.getBackgroundImagePath()));
        } else {
            cmd.addAll(List.of(
                    "-f", "lavfi", "-i",
                    String.format("color=c=%s:s=%s:d=%d:r=%d",
                            hexToFfmpeg(plan.getBackgroundColor()),
                            plan.getResolution(),
                            scene.getDurationSeconds(),
                            plan.getFps())
            ));
        }

        // Text overlay + watermark
        String escapedText = escapeForDrawtext(scene.getText());
        String drawtext = String.format(
                "drawtext=text='%s':fontsize=%d:fontcolor=%s:" +
                        "x=(w-text_w)/2:y=(h-text_h)/2:" +
                        "shadowcolor=black:shadowx=2:shadowy=2",
                escapedText, fontSize, textColor
        );

        // Watermark (alt kısımda)
        if (plan.getWatermarkText() != null) {
            String escapedWatermark = escapeForDrawtext(plan.getWatermarkText());
            drawtext += String.format(
                    ",drawtext=text='%s':fontsize=24:fontcolor=white@0.5:" +
                            "x=(w-text_w)/2:y=h-th-40",
                    escapedWatermark
            );
        }

        cmd.addAll(List.of("-vf", drawtext));
        cmd.addAll(List.of("-c:v", "libx264", "-preset", "fast", "-pix_fmt", "yuv420p"));
        cmd.addAll(List.of("-t", String.valueOf(scene.getDurationSeconds())));
        cmd.add(outputPath.toString());

        executeCommand(cmd);
        return outputPath;
    }

    /**
     * Sahne clip'lerini birleştirir.
     */
    private void concatenateClips(Path concatList, Path output, ShortsRenderPlan plan)
            throws IOException, InterruptedException {

        List<String> cmd = new ArrayList<>();
        cmd.add(ffmpegPath);
        cmd.add("-y");
        cmd.addAll(List.of("-f", "concat", "-safe", "0", "-i", concatList.toString()));

        // Arka plan müzik ekleme (varsa)
        if (plan.getBackgroundMusicPath() != null) {
            cmd.addAll(List.of("-i", plan.getBackgroundMusicPath()));
            cmd.addAll(List.of("-filter_complex",
                    String.format("[1:a]volume=%.2f[music];[0:a][music]amix=inputs=2[aout]",
                            plan.getMusicVolume())));
            cmd.addAll(List.of("-map", "0:v", "-map", "[aout]"));
        } else {
            cmd.addAll(List.of("-c:v", "copy"));
        }

        cmd.addAll(List.of("-movflags", "+faststart"));
        cmd.add(output.toString());

        executeCommand(cmd);
    }

    private Path createConcatList(Path workDir, List<Path> clips) throws IOException {
        Path listFile = workDir.resolve("concat_list.txt");
        List<String> lines = clips.stream()
                .map(p -> "file '" + p.toAbsolutePath() + "'")
                .toList();
        Files.write(listFile, lines);
        return listFile;
    }

    private Path prepareWorkDir() throws IOException {
        Path dir = Path.of(outputDir, UUID.randomUUID().toString());
        Files.createDirectories(dir);
        return dir;
    }

    private void executeCommand(List<String> command) throws IOException, InterruptedException {
        log.debug("FFmpeg komutu: {}", String.join(" ", command));

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        // FFmpeg çıktısını log'la
        String output = new String(process.getInputStream().readAllBytes());
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            log.error("FFmpeg hata kodu: {}, output: {}", exitCode, output);
            throw new RuntimeException("FFmpeg çalıştırma hatası (exit code: " + exitCode + ")");
        }
    }

    /**
     * Hex renk kodunu FFmpeg formatına çevirir.
     * "#FFD700" → "0xFFD700"
     */
    private String hexToFfmpeg(String hex) {
        if (hex == null) return "0xFFFFFF";
        return hex.replace("#", "0x");
    }

    /**
     * Text'i FFmpeg drawtext filter'ı için escape'ler.
     */
    private String escapeForDrawtext(String text) {
        if (text == null) return "";
        return text
                .replace("\\", "\\\\")
                .replace("'", "'\\''")
                .replace(":", "\\:")
                .replace("%", "%%");
    }
}
