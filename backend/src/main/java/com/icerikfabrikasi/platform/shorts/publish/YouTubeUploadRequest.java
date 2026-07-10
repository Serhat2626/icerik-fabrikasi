package com.icerikfabrikasi.platform.shorts.publish;

import lombok.Builder;
import lombok.Value;

import java.util.List;

/**
 * YouTube video yükleme isteği.
 *
 * <p>Herhangi bir kanal ve herhangi bir platform için kullanılabilir.</p>
 */
@Value
@Builder
public class YouTubeUploadRequest {

    /** Render edilmiş video dosyasının lokal yolu */
    String videoFilePath;

    /** YouTube video başlığı (max 100 karakter) */
    String title;

    /** YouTube video açıklaması */
    String description;

    /** Etiketler / tag'ler */
    List<String> tags;

    /** YouTube kategori ID — 22 = People & Blogs, 27 = Education */
    @Builder.Default
    String categoryId = "22";

    /** Gizlilik durumu — "public", "unlisted", "private" */
    @Builder.Default
    String privacyStatus = "public";

    /** Shorts video mu? (YouTube Shorts formatı) */
    @Builder.Default
    boolean isShort = true;

    /** Varsayılan dil */
    @Builder.Default
    String defaultLanguage = "en";

    /** Thumbnail dosya yolu (opsiyonel) */
    String thumbnailPath;
}
