package com.icerikfabrikasi.platform.common.entity;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * BaseEntity PrePersist ve UUID üretim testleri.
 */
class BaseEntityTest {

    /**
     * Test için somut alt sınıf — BaseEntity abstract olduğu için gerekli.
     */
    static class TestEntity extends BaseEntity {
    }

    @Test
    void onPrePersist_shouldGenerateUUID_whenIdIsNull() {
        TestEntity entity = new TestEntity();
        assertNull(entity.getId());

        entity.onPrePersist();

        assertNotNull(entity.getId());
    }

    @Test
    void onPrePersist_shouldNotOverwriteExistingId() {
        TestEntity entity = new TestEntity();
        UUID existingId = UUID.randomUUID();
        entity.setId(existingId);

        entity.onPrePersist();

        assertEquals(existingId, entity.getId());
    }

    @Test
    void onPrePersist_shouldGenerateUniqueIds() {
        TestEntity entity1 = new TestEntity();
        TestEntity entity2 = new TestEntity();

        entity1.onPrePersist();
        entity2.onPrePersist();

        assertNotEquals(entity1.getId(), entity2.getId());
    }
}
