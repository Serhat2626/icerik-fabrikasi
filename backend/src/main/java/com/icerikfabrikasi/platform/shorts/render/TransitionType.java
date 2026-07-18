package com.icerikfabrikasi.platform.shorts.render;

/**
 * Video sahne geçiş türleri.
 *
 * <p>FFmpeg xfade filtresi ile uygulanacak geçiş efektlerini tanımlar.
 * Her geçiş türü, FFmpeg'deki karşılık gelen transition adını ve
 * varsayılan süresini taşır.</p>
 */
public enum TransitionType {

    /** Çözünme geçişi — sahneler yumuşakça birbirine karışır */
    DISSOLVE("dissolve", 0.5),

    /** Kararma/açılma geçişi */
    FADE("fade", 0.5),

    /** Sola kayma geçişi */
    SLIDE_LEFT("slideleft", 0.4),

    /** Sağa kayma geçişi */
    SLIDE_RIGHT("slideright", 0.4),

    /** Sağa silme geçişi */
    WIPE_RIGHT("wiperight", 0.5),

    /** Yumuşak yukarı zoom geçişi */
    ZOOM("smoothup", 0.6),

    /** Geçiş efekti yok — doğrudan kesme */
    NONE(null, 0.0);

    /** FFmpeg xfade transition adı (NONE için null) */
    private final String ffmpegName;

    /** Varsayılan geçiş süresi (saniye) */
    private final double defaultDuration;

    TransitionType(String ffmpegName, double defaultDuration) {
        this.ffmpegName = ffmpegName;
        this.defaultDuration = defaultDuration;
    }

    public String getFfmpegName() {
        return ffmpegName;
    }

    public double getDefaultDuration() {
        return defaultDuration;
    }

    /**
     * Bu geçiş türü için FFmpeg xfade filtre string'i üretir.
     *
     * <p>Örnek çıktı: {@code xfade=transition=dissolve:duration=0.5:offset=3.0}</p>
     *
     * @param offset geçişin başlayacağı zaman noktası (saniye)
     * @return FFmpeg xfade filtre string'i; {@link #NONE} için {@code null}
     */
    public String toFfmpegFilter(double offset) {
        if (ffmpegName == null) {
            return null;
        }
        return String.format("xfade=transition=%s:duration=%.1f:offset=%.1f",
                ffmpegName, defaultDuration, offset);
    }
}
