# İçerik Fabrikası

AI destekli çok kanallı içerik üretim platformu.

## Teknoloji

- Java 21
- Spring Boot 3.5.0
- PostgreSQL 16 + Flyway
- Redis 7
- Amazon S3 (depolama)

## Geliştirme Ortamı

### Ön Koşullar

- Java 21+
- Maven 3.9+
- Docker & Docker Compose

### Altyapıyı Başlatma

```bash
docker-compose up -d
```

Bu komut PostgreSQL ve Redis servislerini başlatır.

### Uygulamayı Çalıştırma

```bash
cd backend
mvn spring-boot:run
```

Uygulama `http://localhost:8080` adresinde çalışır.

### Testleri Çalıştırma

```bash
cd backend
mvn clean test
```

Testler H2 in-memory veritabanı kullanır. PostgreSQL, Redis veya S3 bağlantısı gerekmez.

### API Dokümantasyonu

Uygulama çalışırken:
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/api-docs

## Endpoint'ler

| Method | Path | Açıklama |
|--------|------|----------|
| GET | /api/health | Sistem sağlık kontrolü |

## Proje Yapısı

```
icerik-fabrikasi/
├── backend/                 # Spring Boot backend
│   └── src/
│       ├── main/
│       │   ├── java/        # Kaynak kodlar
│       │   └── resources/   # Konfigürasyon
│       └── test/            # Testler
├── docs/                    # Mühendislik dokümanları
│   └── engineering/
├── docker-compose.yml       # Yerel geliştirme altyapısı
└── README.md
```

## Lisans

Tescilli — Tüm hakları saklıdır.
