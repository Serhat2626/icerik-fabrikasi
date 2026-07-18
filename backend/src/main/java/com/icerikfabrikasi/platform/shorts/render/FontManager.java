package com.icerikfabrikasi.platform.shorts.render;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Google Fonts otomatik indirme ve yönetim servisi.
 *
 * <p>Kanal bazlı font konfigürasyonlarını destekler.
 * Font dosyaları lokal cache'e indirilir ve FFmpeg'e
 * {@code fontfile=} parametresi ile verilir.</p>
 *
 * <p>Desteklenen fontlar: Montserrat, Inter, Poppins, Bebas Neue,
 * Nunito, Quicksand, Space Grotesk, Outfit.</p>
 */
@Component
@Slf4j
public class FontManager {

    /**
     * Bilinen Google Font'ların doğrudan TTF indirme URL'leri.
     * GitHub raw URL'leri üzerinden indirme yapılır.
     */
    private static final Map<String, String> FONT_URLS = Map.ofEntries(
            Map.entry("Montserrat",
                    "https://github.com/JulietaUla/Montserrat/raw/master/fonts/ttf/Montserrat-Bold.ttf"),
            Map.entry("Inter",
                    "https://github.com/rsms/inter/raw/master/docs/font-files/Inter-Bold.otf"),
            Map.entry("Nunito",
                    "https://github.com/googlefonts/nunito/raw/main/fonts/ttf/Nunito-Bold.ttf"),
            Map.entry("Poppins",
                    "https://github.com/itfoundry/Poppins/raw/master/products/Poppins-Bold.ttf"),
            Map.entry("Bebas Neue",
                    "https://github.com/dharmatype/Bebas-Neue/raw/master/fonts/BebasNeue-Regular.ttf"),
            Map.entry("Space Grotesk",
                    "https://github.com/nicholasfcarvalheira/Space-Grotesk/raw/master/fonts/SpaceGrotesk-Bold.ttf"),
            Map.entry("Quicksand",
                    "https://github.com/andrew-paglinawan/QuicksandFamily/raw/master/fonts/Quicksand-Bold.ttf"),
            Map.entry("Outfit",
                    "https://github.com/Outfitio/Outfit-Fonts/raw/main/fonts/ttf/Outfit-Bold.ttf")
    );

    @Value("${shorts.render.font-dir:${java.io.tmpdir}/shorts-fonts}")
    private String fontDir;

    /** İndirilen fontların in-memory cache'i — tekrar indirme önlenir */
    private final ConcurrentHashMap<String, Path> fontCache = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Path.of(fontDir));
            log.info("Font dizini hazır: {}", fontDir);
        } catch (IOException e) {
            log.warn("Font dizini oluşturulamadı: {}", e.getMessage());
        }
    }

    /**
     * Font dosya yolunu döndürür. Cache'de yoksa indirir.
     *
     * <p>FFmpeg {@code fontfile=} parametresi için kullanılır.
     * Font bulunamazsa {@code null} döner ve sistem fontu kullanılır.</p>
     *
     * @param fontFamily Font ailesi adı — örn: "Montserrat"
     * @return TTF dosya yolu veya null (indirilemezse)
     */
    public String getFontPath(String fontFamily) {
        if (fontFamily == null || fontFamily.isBlank()) {
            return null;
        }

        // In-memory cache kontrolü
        Path cached = fontCache.get(fontFamily);
        if (cached != null && Files.exists(cached)) {
            return cached.toString();
        }

        // Disk cache kontrolü
        String safeFileName = fontFamily.replaceAll("[^a-zA-Z0-9]", "_") + ".ttf";
        Path fontPath = Path.of(fontDir, safeFileName);
        if (Files.exists(fontPath)) {
            try {
                if (Files.size(fontPath) > 1000) {
                    fontCache.put(fontFamily, fontPath);
                    return fontPath.toString();
                }
            } catch (IOException e) {
                // Devam et — indirme dene
            }
        }

        // İndir
        return downloadFont(fontFamily, fontPath);
    }

    private String downloadFont(String fontFamily, Path targetPath) {
        String url = FONT_URLS.get(fontFamily);
        if (url == null) {
            log.warn("Font URL bulunamadı: '{}', sistem fontu kullanılacak", fontFamily);
            return null;
        }

        try {
            log.info("Font indiriliyor: {} -> {}", fontFamily, targetPath);

            HttpURLConnection conn = (HttpURLConnection) URI.create(url).toURL().openConnection();
            conn.setRequestProperty("User-Agent", "IcerikFabrikasi/1.0");
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(30000);
            conn.setInstanceFollowRedirects(true);

            // GitHub raw URL'leri redirect yapar — takip et
            int status = conn.getResponseCode();
            if (status == HttpURLConnection.HTTP_MOVED_TEMP
                    || status == HttpURLConnection.HTTP_MOVED_PERM
                    || status == 307 || status == 308) {
                String newUrl = conn.getHeaderField("Location");
                conn.disconnect();
                conn = (HttpURLConnection) URI.create(newUrl).toURL().openConnection();
                conn.setRequestProperty("User-Agent", "IcerikFabrikasi/1.0");
            }

            try (InputStream in = conn.getInputStream()) {
                Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
            } finally {
                conn.disconnect();
            }

            if (Files.exists(targetPath) && Files.size(targetPath) > 1000) {
                fontCache.put(fontFamily, targetPath);
                log.info("Font indirildi: {} ({}KB)", fontFamily, Files.size(targetPath) / 1024);
                return targetPath.toString();
            } else {
                Files.deleteIfExists(targetPath);
                log.warn("Font dosyası geçersiz: {}", fontFamily);
                return null;
            }

        } catch (Exception e) {
            log.error("Font indirme hatası: {} — {}", fontFamily, e.getMessage());
            return null;
        }
    }
}
