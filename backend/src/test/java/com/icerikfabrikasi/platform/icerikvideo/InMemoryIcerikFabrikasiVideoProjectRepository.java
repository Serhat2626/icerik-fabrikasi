package com.icerikfabrikasi.platform.icerikvideo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Test-only in-memory implementasyonu. IcerikFabrikasiVideoProjectRepository interface'ini
 * unit test'lerde veritabanı bağlantısı olmadan kullanmak için.
 *
 * <p>Sadece test tarafında kullanılır. Production'da Spring Data JPA proxy kullanılır.</p>
 */
public class InMemoryIcerikFabrikasiVideoProjectRepository implements IcerikFabrikasiVideoProjectRepository {

    private final Map<UUID, IcerikFabrikasiVideoProject> store = new ConcurrentHashMap<>();

    // ── Interface metotları ──

    @Override
    public IcerikFabrikasiVideoProject save(IcerikFabrikasiVideoProject entity) {
        store.put(entity.getId(), entity);
        return entity;
    }

    @Override
    public Optional<IcerikFabrikasiVideoProject> findById(UUID id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<IcerikFabrikasiVideoProject> findAll() {
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
    public List<IcerikFabrikasiVideoProject> findByWorkflowStage(IcerikFabrikasiVideoWorkflowStage workflowStage) {
        return store.values().stream()
                .filter(p -> p.getWorkflowStage() == workflowStage)
                .collect(Collectors.toList());
    }

    @Override
    public List<IcerikFabrikasiVideoProject> findByNeedsHumanReviewTrue() {
        return store.values().stream()
                .filter(IcerikFabrikasiVideoProject::isNeedsHumanReview)
                .collect(Collectors.toList());
    }

    @Override
    public List<IcerikFabrikasiVideoProject> findByIcerikFabrikasiNameContainingIgnoreCase(String icerikFabrikasiName) {
        String lower = icerikFabrikasiName.toLowerCase();
        return store.values().stream()
                .filter(p -> p.getIcerikFabrikasiName() != null && p.getIcerikFabrikasiName().toLowerCase().contains(lower))
                .collect(Collectors.toList());
    }

    @Override
    public List<IcerikFabrikasiVideoProject> findBySourceConfidence(IcerikFabrikasiSourceConfidence sourceConfidence) {
        return store.values().stream()
                .filter(p -> p.getSourceConfidence() == sourceConfidence)
                .collect(Collectors.toList());
    }

    // ── Test helper metotları (interface'de yok) ──

    public void deleteById(UUID id) {
        store.remove(id);
    }

    public void delete(IcerikFabrikasiVideoProject entity) {
        store.remove(entity.getId());
    }

    public boolean existsById(UUID id) {
        return store.containsKey(id);
    }

    public <S extends IcerikFabrikasiVideoProject> List<S> saveAll(Iterable<S> entities) {
        List<S> saved = new ArrayList<>();
        entities.forEach(e -> { save(e); saved.add(e); });
        return saved;
    }

    /** Test desteği — tüm veriyi temizle */
    public void clear() {
        store.clear();
    }
}
