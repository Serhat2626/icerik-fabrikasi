package com.icerikfabrikasi.platform.shorts.publish;

/**
 * YouTube video yükleme gateway arayüzü.
 *
 * <p>Farklı implementasyonlar kullanılabilir:
 * <ul>
 *   <li>{@code YouTubeDataApiGateway} — Gerçek YouTube Data API v3</li>
 *   <li>{@code StubYouTubeUploadGateway} — Test/dev ortamı için stub</li>
 * </ul>
 * </p>
 */
public interface YouTubeUploadGateway {

    /**
     * Video dosyasını YouTube'a yükler.
     *
     * @param request Yükleme isteği (dosya yolu, başlık, açıklama vb.)
     * @return Yükleme sonucu
     */
    YouTubeUploadResult upload(YouTubeUploadRequest request);

    /**
     * Gateway'in kullanılabilir olup olmadığını kontrol eder.
     */
    boolean isAvailable();

    /**
     * Provider adı.
     */
    String getProviderName();
}
