package com.icerikfabrikasi.platform.shorts.publish;

import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

/**
 * YouTube yükleme stub gateway'i — test/dev ortamı için.
 *
 * <p>Gerçek yükleme yapmaz, simüle edilmiş başarılı sonuç döndürür.
 * YouTube credentials yokken kullanılır.</p>
 */
@Slf4j
public class StubYouTubeUploadGateway implements YouTubeUploadGateway {

    @Override
    public YouTubeUploadResult upload(YouTubeUploadRequest request) {
        // Simüle edilmiş yükleme süresi
        String fakeVideoId = "STUB_" + UUID.randomUUID().toString().substring(0, 11);

        log.info("[STUB] YouTube yükleme simüle edildi: title='{}', fakeVideoId={}",
                request.getTitle(), fakeVideoId);

        return YouTubeUploadResult.success(fakeVideoId, 500);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String getProviderName() {
        return "stub";
    }
}
