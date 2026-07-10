package com.icerikfabrikasi.platform.shorts.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Render edilmiş video dosyalarını serve eden controller.
 *
 * <p>Dashboard'daki video player bu endpoint'i kullanır.</p>
 */
@RestController
@RequestMapping("/api/admin/videos")
@Slf4j
public class VideoServeController {

    @GetMapping("/stream")
    public ResponseEntity<Resource> streamVideo(@RequestParam String path) {
        try {
            Path videoPath = Path.of(path);

            if (!Files.exists(videoPath)) {
                log.warn("Video dosyası bulunamadı: {}", path);
                return ResponseEntity.notFound().build();
            }

            Resource resource = new FileSystemResource(videoPath.toFile());
            long fileSize = Files.size(videoPath);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                    .contentType(MediaType.parseMediaType("video/mp4"))
                    .contentLength(fileSize)
                    .body(resource);

        } catch (Exception e) {
            log.error("Video stream hatası: path={}, hata={}", path, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
