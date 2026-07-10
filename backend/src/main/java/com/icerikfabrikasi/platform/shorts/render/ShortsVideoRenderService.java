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
 * <p>Her sahne için: gradient arka plan veya stock görsel + animasyonlu text overlay.
 * Sahneler arası crossfade geçiş + arka plan müzik.</p>
 */
@Service
@Slf4j
public class ShortsVideoRenderService {

    @Value("${shorts.render.output-dir:${java.io.tmpdir}/shorts-render}")
    private String outputDir;

    @Value("${shorts.render.ffmpeg-path:ffmpeg}")
    private String ffmpegPath;

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

            // Concat ile birleştir
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
     * Tek sahne clip: solid arka plan + text overlay, fade geçiş.
     */
    private Path renderSceneClip(Path workDir, ShortsRenderPlan plan, SceneRenderPlan scene)
            throws IOException, InterruptedException {

        Path outputPath = workDir.resolve(
                String.format("scene_%03d.mp4", scene.getOrderIndex()));

        int fontSize = scene.isHook() ? plan.getHookFontSize() : plan.getBodyFontSize();
        String textColor = hexToFfmpeg(scene.isHook() ? plan.getAccentColor() : plan.getTextColor());
        int dur = scene.getDurationSeconds();
        String bgColor = hexToFfmpeg(plan.getBackgroundColor());

        boolean hasImage = scene.getBackgroundImagePath() != null
                && Files.exists(Path.of(scene.getBackgroundImagePath()));

        List<String> cmd = new ArrayList<>();
        cmd.add(ffmpegPath);
        cmd.add("-y");

        if (hasImage) {
            // Stock görsel → scale+crop 9:16, dark overlay + text
            cmd.addAll(List.of("-loop", "1", "-i", scene.getBackgroundImagePath()));
            cmd.addAll(List.of("-t", String.valueOf(dur)));

            String filter = String.format(
                    "[0:v]scale=1080:1920:force_original_aspect_ratio=increase,crop=1080:1920," +
                    "drawbox=x=0:y=0:w=iw:h=ih:color=black@0.5:t=fill," +
                    "drawtext=text='%s':fontsize=%d:fontcolor=%s:" +
                    "x=(w-text_w)/2:y=(h-text_h)/2:" +
                    "shadowcolor=black@0.8:shadowx=3:shadowy=3," +
                    "fade=t=in:st=0:d=0.4,fade=t=out:st=%d:d=0.4",
                    escapeForDrawtext(scene.getText()), fontSize, textColor,
                    Math.max(1, dur - 1)
            );

            // Watermark
            if (plan.getWatermarkText() != null) {
                filter = filter.replace(
                        "fade=t=in:st=0:d=0.4,fade=t=out",
                        String.format("drawtext=text='%s':fontsize=22:fontcolor=white@0.4:" +
                                "x=(w-text_w)/2:y=h-th-60," +
                                "fade=t=in:st=0:d=0.4,fade=t=out",
                                escapeForDrawtext(plan.getWatermarkText()))
                );
            }

            cmd.addAll(List.of("-vf", filter));
        } else {
            // Solid color arka plan + text
            cmd.addAll(List.of(
                    "-f", "lavfi", "-i",
                    String.format("color=c=%s:s=1080x1920:d=%d:r=%d",
                            bgColor, dur, plan.getFps())
            ));

            String textFilter = String.format(
                    "drawtext=text='%s':fontsize=%d:fontcolor=%s:" +
                    "x=(w-text_w)/2:y=(h-text_h)/2:" +
                    "shadowcolor=black@0.7:shadowx=2:shadowy=2",
                    escapeForDrawtext(scene.getText()), fontSize, textColor
            );

            // Watermark
            if (plan.getWatermarkText() != null) {
                textFilter += String.format(
                        ",drawtext=text='%s':fontsize=22:fontcolor=white@0.35:" +
                        "x=(w-text_w)/2:y=h-th-60",
                        escapeForDrawtext(plan.getWatermarkText())
                );
            }

            // Fade in/out
            textFilter += String.format(",fade=t=in:st=0:d=0.4,fade=t=out:st=%d:d=0.4",
                    Math.max(1, dur - 1));

            cmd.addAll(List.of("-vf", textFilter));
        }

        cmd.addAll(List.of("-c:v", "libx264", "-preset", "ultrafast", "-crf", "26"));
        cmd.addAll(List.of("-pix_fmt", "yuv420p"));
        cmd.addAll(List.of("-t", String.valueOf(dur)));
        cmd.add(outputPath.toString());

        executeCommand(cmd);
        return outputPath;
    }

    /**
     * Sahne clip'lerini birleştirir.
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

        // Arka plan müzik
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
            cmd.addAll(List.of("-an")); // no audio if no music
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
        log.debug("FFmpeg komutu: {}", String.join(" ", command));
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process process = pb.start();
        String output = new String(process.getInputStream().readAllBytes());
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            log.error("FFmpeg hata: exitCode={}, output={}", exitCode, output);
            throw new RuntimeException("FFmpeg hatası (exit: " + exitCode + "): " + output.substring(0, Math.min(output.length(), 500)));
        }
    }

    private String hexToFfmpeg(String hex) {
        if (hex == null) return "0x1A1A2E";
        return hex.replace("#", "0x");
    }

    private String escapeForDrawtext(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\").replace("'", "'\\''")
                .replace(":", "\\:").replace("%", "%%")
                .replace("\n", " ");
    }
}
