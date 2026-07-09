package com.icerikfabrikasi.platform.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA Auditing konfigürasyonu.
 *
 * <p>{@code @EnableJpaAuditing} ayrı bir {@code @Configuration} sınıfında tutulur,
 * böylece {@code @WebMvcTest} gibi web-only test dilimleri JPA altyapısını
 * yüklemeye çalışmaz.</p>
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
