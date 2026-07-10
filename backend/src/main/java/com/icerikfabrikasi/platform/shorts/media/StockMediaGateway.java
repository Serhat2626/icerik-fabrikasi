package com.icerikfabrikasi.platform.shorts.media;

import java.util.List;

/**
 * Stock medya sağlayıcı arayüzü.
 *
 * <p>Pexels, Pixabay gibi farklı provider'lar bu arayüzü implemente eder.</p>
 */
public interface StockMediaGateway {

    /**
     * Anahtar kelimeye göre fotoğraf arar.
     *
     * @param query Arama sorgusu (İngilizce)
     * @param count İstenen sonuç sayısı
     * @return Bulunan medya öğeleri
     */
    List<StockMediaItem> searchPhotos(String query, int count);

    /**
     * Dikey (portrait) fotoğraf arar — Shorts için optimize.
     *
     * @param query Arama sorgusu
     * @return İlk uygun sonuç veya null
     */
    StockMediaItem findPortraitPhoto(String query);

    /**
     * Provider adı.
     */
    String getProviderName();
}
