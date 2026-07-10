# FP-012: Admin Review API Authorization & Observability Hardening

## Observability Metrikleri

Güvenli metrik adları (PII içermez):

- `admin_review_action_total` — Toplam review aksiyonu sayısı
- `admin_review_transition_violation_total` — Geçersiz durum geçiş denemesi sayısı

### İzin Verilen Etiketler

| Etiket | Açıklama |
|--------|----------|
| `action_type` | Review aksiyon türü (SUBMIT, APPROVE, REJECT, vb.) |
| `target_type` | Hedef kaynak türü (CONTENT_PROJECT, SCENARIO, vb.) |
| `actor_role` | Aktör rolü (ADMIN, REVIEWER) |

### Forbidden Label Listesi

Aşağıdaki etiketler **asla** metrik veya log'larda kullanılmamalıdır:

| Forbidden Label | Neden |
|----------------|-------|
| `actor_reference` | PII — kullanıcı kimliği ifşa eder |
| `target_id` | Dahili UUID — dış erişimde anlamsız |
| `review_record_id` | Dahili UUID |

## Yetkilendirme Notları

> **ÖNEMLİ:** `actorRole` alanı **not a replacement for real authentication**.
> Gerçek kimlik doğrulama JWT tabanlı `SecurityConfig` üzerinden yapılır.

## Üretim Güvenliği

> Production ortamında mutlaka **protect admin routes** için:
> - JWT authentication aktif olmalı
> - Role-based access control uygulanmalı
> - Rate limiting eklenmelidir
