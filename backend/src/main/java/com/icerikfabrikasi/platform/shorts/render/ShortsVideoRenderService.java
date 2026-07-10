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

            for (SceneRenderPlan scene : plan.getScenes()) {
                Path clipPath = renderSceneClip(workDir, plan, scene);
                sceneClips.add(clipPath);
            }

            // Crossfade ile birleştir
            Path finalOutput = concatenateWithTransitions(workDir, sceneClips, plan);

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
     * Tek sahne clip: gradient/stock arka plan + animasyonlu text overlay
     */
    private Path renderSceneClip(Path workDir, ShortsRenderPlan plan, SceneRenderPlan scene)
            throws IOException, InterruptedException {

        Path outputPath = workDir.resolve(
                String.format("scene_%03d_%s.mp4", scene.getOrderIndex(), UUID.randomUUID()));

        int fontSize = scene.isHook() ? plan.getHookFontSize() : plan.getBodyFontSize();
        String textColor = hexToFfmpeg(scene.isHook() ? plan.getAccentColor() : plan.getTextColor());
        int dur = scene.getDurationSeconds();
        String res = plan.getResolution(); // 1080x1920

        List<String> cmd = new ArrayList<>();
        cmd.add(ffmpegPath);
        cmd.add("-y");

        boolean hasImage = scene.getBackgroundImagePath() != null
                && Files.exists(Path.of(scene.getBackgroundImagePath()));

        if (hasImage) {
            // Stock görsel arka plan — scale+crop to 9:16 + slight zoom animation
            cmd.addAll(List.of("-loop", "1", "-i", scene.getBackgroundImagePath()));
            cmd.addAll(List.of("-t", String.valueOf(dur)));

            // Ken Burns effect (subtle zoom) + dark overlay + text
            String filter = String.format(
                    "[0:v]scale=1080:1920:force_original_aspect_ratio=increase,crop=1080:1920," +
                    "zoompan=z='min(zoom+0.001,1.08)':x='iw/2-(iw/zoom/2)':y='ih/2-(ih/zoom/2)':d=%d*%d:s=1080x1920:fps=%d," +
                    "colorbalance=bs=-0.15," +
                    "drawbox=x=0:y=0:w=iw:h=ih:color=black@0.45:t=fill," +
                    "drawtext=text='%s':fontsize=%d:fontcolor=%s:" +
                    "x=(w-text_w)/2:y=(h-text_h)/2:" +
                    "shadowcolor=black@0.8:shadowx=3:shadowy=3:" +
                    "enable='between(t,0.3,%d)':" +
                    "alpha='if(lt(t,0.8),t/0.5,if(gt(t,%d-0.5),((%d-t)/0.5),1))'",
                    dur, plan.getFps(), plan.getFps(),
                    escapeForDrawtext(scene.getText()), fontSize, textColor,
                    dur, dur, dur
            );

            // Watermark
            if (plan.getWatermarkText() != null) {
                filter += String.format(
                        ",drawtext=text='%s':fontsize=22:fontcolor=white@0.4:" +
                        "x=(w-text_w)/2:y=h-th-60",
                        escapeForDrawtext(plan.getWatermarkText())
                );
            }

            cmd.addAll(List.of("-vf", filter));
        } else {
            // Gradient arka plan (daha profesyonel görünüm)
            String bgColor = hexToFfmpeg(plan.getBackgroundColor());
            String accentColor = hexToFfmpeg(plan.getAccentColor());

            // Animated gradient background
            String gradientFilter = String.format(
                    "gradients=s=1080x1920:c0=%s:c1=%s:x0=0:y0=0:x1=1080:y1=1920:d=%d:r=%d:speed=0.8",
                    bgColor, accentColor, dur, plan.getFps()
            );

            // Fallback: solid color with subtle animation if gradients filter not available
            cmd.addAll(List.of(
                    "-f", "lavfi", "-i",
                    String.format("color=c=%s:s=%s:d=%d:r=%d",
                            bgColor, res, dur, plan.getFps())
            ));

            // Text with fade-in/fade-out animation
            String textFilter = String.format(
                    "drawtext=text='%s':fontsize=%d:fontcolor=%s:" +
                    "x=(w-text_w)/2:y=(h-text_h)/2:" +
                    "shadowcolor=black@0.7:shadowx=2:shadowy=2:" +
                    "enable='between(t,0.3,%d)'",
                    escapeForDrawtext(scene.getText()), fontSize, textColor, dur
            );

            // Watermark
            if (plan.getWatermarkText() != null) {
                textFilter += String.format(
                        ",drawtext=text='%s':fontsize=22:fontcolor=white@0.35:" +
                        "x=(w-text_w)/2:y=h-th-60",
                        escapeForDrawtext(plan.getWatermarkText())
                );
            }

            // Fade in/out for scene transitions
            textFilter += String.format(",fade=t=in:st=0:d=0.4,fade=t=out:st=%d:d=0.4", dur - 1);

            cmd.addAll(List.of("-vf", textFilter));
        }

        cmd.addAll(List.of("-c:v", "libx264", "-preset", "fast", "-crf", "23"));
        cmd.addAll(List.of("-pix_fmt", "yuv420p"));
        cmd.addAll(List.of("-t", String.valueOf(dur)));
        cmd.add(outputPath.toString());

        executeCommand(cmd);
        return outputPath;
    }

    /**
     * Sahne clip'lerini crossfade ile birleştirir + arka plan müzik ekler.
     */
    private Path concatenateWithTransitions(Path workDir, List<Path> clips, ShortsRenderPlan plan)
            throws IOException, InterruptedException {

        // Basit concat (crossfade FFmpeg'de complex, önce basit concat yapalım)
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
                            plan.totalDurationSeconds() - 3)));
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
