package com.icerikfabrikasi.platform.shorts.media;

import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;

/**
 * Stock medya stub gateway — API key olmadığında kullanılır.
 *
 * <p>Geliştirme ortamında Pexels API key olmadan çalışabilmek için
 * boş sonuç döndürür. Bu durumda render düz renkli arka plan kullanır.</p>
 */
@Slf4j
public class StubStockMediaGateway implements StockMediaGateway {

    @Override
    public List<StockMediaItem> searchPhotos(String query, int count) {
        log.debug("Stub: fotoğraf araması atlandı — query='{}'", query);
        return Collections.emptyList();
    }

    @Override
    public StockMediaItem findPortraitPhoto(String query) {
        return null;
    }

    @Override
    public String getProviderName() {
        return "stub";
    }
}
