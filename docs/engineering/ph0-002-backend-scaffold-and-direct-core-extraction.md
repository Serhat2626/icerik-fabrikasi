# PH0-002 — Backend Scaffold & Direct-Core Extraction Report

**Tarih:** 2026-07-10
**Faz:** PH0-002
**Durum:** Tamamlandı

---

## Kaynak İnceleme Özeti

### İncelenen Kaynak Dosyalar (turkuproje → icerik-fabrikasi)

| # | Kaynak Dosya (turkuproje) | Hedef (icerik-fabrikasi) | İşlem |
|---|---|---|---|
| 1 | `common/entity/BaseEntity.java` | `common.entity.BaseEntity` | Paket adı değiştirildi |
| 2 | `common/api/ApiResponse.java` | `common.api.ApiResponse` | Paket adı değiştirildi, FieldError ayrı dosyaya çıkarıldı |
| 3 | *(ApiResponse inner class)* | `common.api.FieldError` | Ayrı sınıfa çıkarıldı |
| 4 | `common/exception/BusinessException.java` | `common.exception.BusinessException` | Paket adı değiştirildi |
| 5 | `common/exception/NotFoundException.java` | `common.exception.NotFoundException` | Paket adı değiştirildi, Türkçe mesaj korundu |
| 6 | `common/exception/ValidationException.java` | `common.exception.ValidationException` | Paket adı değiştirildi |
| 7 | `common/exception/GlobalExceptionHandler.java` | `common.exception.GlobalExceptionHandler` | Paket adı değiştirildi, FieldError import güncellendi |
| 8 | `config/JacksonConfig.java` | `config.JacksonConfig` | Paket adı değiştirildi |
| 9 | `config/WebConfig.java` | `config.WebConfig` | Paket adı değiştirildi |
| 10 | `config/RedisConfig.java` | `config.RedisConfig` | Paket adı değiştirildi |
| 11 | `config/OpenApiConfig.java` | `config.OpenApiConfig` | Paket adı + proje adı değiştirildi |
| 12 | `config/MinioConfig.java` | `config.StorageProperties` | S3 odaklı yeniden yazıldı, güvenli toString |
| 13 | `health/controller/HealthController.java` | `health.HealthController` | Paket adı + servis adı değiştirildi |

### Taşınmayan Modüller (Kategori B — Sadece İncelendi)

- `ai/` — AI gateway sözleşmeleri
- `application/` — Uygulama katmanı
- `artist/` — Sanatçı modülü (Türkü-spesifik)
- `content/` — İçerik modülü
- `imagegen/` — Görsel üretim
- `knowledge/` — Bilgi tabanı
- `license/` — Telif/lisans
- `media/` — Medya yönetimi
- `platformpackage/` — Platform paket
- `publishing/` — Yayınlama
- `review/` — İnceleme
- `scenario/` — Senaryo
- `storyboard/` — Storyboard
- `icerikvideo/` — Türkü video (Türkü-spesifik)
- `visualasset/` — Görsel varlık

### Asla Taşınmayacak Öğeler (Kategori C)

- Anadolu Atlası / Türkü / sanatçı / kültürel dikey dilim varsayımları
- Ham sağlayıcı payload modelleri
- base64/binary açığa çıkaran modeller
- rawLog/stdout/stderr/path/storage key açığa çıkaran alanlar
- Otomatik onay veya otomatik yayın mantığı
- ProcessBuilder veya Runtime.exec kullanımı
- Güvensiz HTML kalıpları
- Yanıtlarda/logda açığa çıkan secret/token/credential

---

## Paket Eşleme

```
com.anadoluatlasi.backend.* → com.icerikfabrikasi.platform.*
```

---

## Konfigürasyon Özeti

| Ayar | Değer |
|------|-------|
| Spring Boot | 3.5.0 |
| Java | 21 |
| PostgreSQL | 16 (docker-compose) |
| Redis | 7-alpine (docker-compose) |
| Depolama | Amazon S3 (konfigürasyon only, gerçek istemci yok) |
| Flyway | Aktif (1 placeholder migration) |
| Test DB | H2 in-memory |
| Test profili | `application-test.yml` — Redis/S3/PostgreSQL gerektirmez |

---

## Migration Özeti

| Dosya | İçerik |
|-------|--------|
| `V1__initial_schema.sql` | Yorum satırı — yer tutucu. İş tabloları PH1+'da |

---

## Güvenlik Temeli

- [x] ProcessBuilder / Runtime.exec kullanılmadı
- [x] Otomatik onay mantığı yok
- [x] Otomatik yayın mantığı yok
- [x] Güvensiz HTML kalıbı yok
- [x] Yanıtlarda secret/token/credential açığa çıkmıyor
- [x] 500 yanıtı iç bilgileri açığa çıkarmıyor (test ile doğrulandı)
- [x] StorageProperties.toString() hassas bilgileri maskeler
- [x] Testler harici servislere (PostgreSQL/Redis/S3) bağımlı değil

---

## Test Temeli

| Test Sınıfı | Kapsam |
|-------------|--------|
| `IcerikFabrikasiApplicationTest` | Context yükleme |
| `HealthControllerTest` | GET /api/health → 200 |
| `ApiResponseTest` | success/error factory metodları |
| `BaseEntityTest` | PrePersist UUID üretimi |
| `GlobalExceptionHandlerTest` | 404/400/409/500 eşleme + güvenlik doğrulaması |

---

## Endpoint Sayısı

| Method | Path | Açıklama |
|--------|------|----------|
| GET | `/api/health` | Sistem sağlık kontrolü |

**Toplam:** 1 GET endpoint

---

## Sonraki Önerilen Görev

**PH0-003: Frontend Scaffold + Ortak Bileşenler**
- Vite + React 19 kurulumu
- Ortak UI bileşenleri
- API istemci temeli
- Geliştirme ortamı konfigürasyonu
