package com.icerikfabrikasi.platform.turkuvideo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Test-only in-memory implementasyonu. TurkuVideoProjectRepository interface'ini
 * unit test'lerde veritabanı bağlantısı olmadan kullanmak için.
 *
 * <p>Sadece test tarafında kullanılır. Production'da Spring Data JPA proxy kullanılır.</p>
 */
public class InMemoryTurkuVideoProjectRepository implements TurkuVideoProjectRepository {

    private final Map<UUID, TurkuVideoProject> store = new ConcurrentHashMap<>();

    // ── Interface metotları ──

    @Override
    public TurkuVideoProject save(TurkuVideoProject entity) {
        store.put(entity.getId(), entity);
        return entity;
    }

    @Override
    public Optional<TurkuVideoProject> findById(UUID id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<TurkuVideoProject> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public long count() {
        return store.size();
    }

    @Override
    public void deleteAll() {
        store.clear();
    }

    @Override
    public List<TurkuVideoProject> findByWorkflowStage(TurkuVideoWorkflowStage workflowStage) {
        return store.values().stream()
                .filter(p -> p.getWorkflowStage() == workflowStage)
                .collect(Collectors.toList());
    }

    @Override
    public List<TurkuVideoProject> findByNeedsHumanReviewTrue() {
        return store.values().stream()
                .filter(TurkuVideoProject::isNeedsHumanReview)
                .collect(Collectors.toList());
    }

    @Override
    public List<TurkuVideoProject> findByTurkuNameContainingIgnoreCase(String turkuName) {
        String lower = turkuName.toLowerCase();
        return store.values().stream()
                .filter(p -> p.getTurkuName() != null && p.getTurkuName().toLowerCase().contains(lower))
                .collect(Collectors.toList());
    }

    @Override
    public List<TurkuVideoProject> findBySourceConfidence(TurkuSourceConfidence sourceConfidence) {
        return store.values().stream()
                .filter(p -> p.getSourceConfidence() == sourceConfidence)
                .collect(Collectors.toList());
    }

    // ── Test helper metotları (interface'de yok) ──

    public void deleteById(UUID id) {
        store.remove(id);
    }

    public void delete(TurkuVideoProject entity) {
        store.remove(entity.getId());
    }

    public boolean existsById(UUID id) {
        return store.containsKey(id);
    }

    public <S extends TurkuVideoProject> List<S> saveAll(Iterable<S> entities) {
        List<S> saved = new ArrayList<>();
        entities.forEach(e -> { save(e); saved.add(e); });
        return saved;
    }

    /** Test desteği — tüm veriyi temizle */
    public void clear() {
        store.clear();
    }
}
