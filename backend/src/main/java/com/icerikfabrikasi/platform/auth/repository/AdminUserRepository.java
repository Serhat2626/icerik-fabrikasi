package com.icerikfabrikasi.platform.auth.repository;

import com.icerikfabrikasi.platform.auth.entity.AdminUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Admin kullanıcı JPA repository.
 */
@Repository
public interface AdminUserRepository extends JpaRepository<AdminUser, UUID> {

    Optional<AdminUser> findByUsername(String username);

    boolean existsByUsername(String username);
}
