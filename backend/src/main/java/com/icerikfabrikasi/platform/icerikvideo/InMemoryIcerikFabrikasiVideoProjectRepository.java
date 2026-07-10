package com.icerikfabrikasi.platform.icerikvideo;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory implementasyonu — IcerikFabrikasiVideoProject henüz JPA Entity olmadığı için
 * ConcurrentHashMap ile çalışır. Hem production hem test'te kullanılır.
 *
 * <p>JPA Entity'ye geçildiğinde bu sınıf kaldırılıp Spring Data proxy kullanılır.</p>
 */
@Repository
public class InMemoryIcerikFabrikasiVideoProjectRepository implements IcerikFabrikasiVideoProjectRepository {

    private final Map<UUID, IcerikFabrikasiVideoProject> store = new ConcurrentHashMap<>();

    @Override
    public IcerikFabrikasiVideoProject save(IcerikFabrikasiVideoProject project) {
        store.put(project.getId(), project);
        return project;
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

    /** Test desteği — tüm veriyi temizle */
    public void clear() {
        store.clear();
    }
}
