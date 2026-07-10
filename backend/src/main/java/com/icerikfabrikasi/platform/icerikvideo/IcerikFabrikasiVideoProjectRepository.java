package com.icerikfabrikasi.platform.icerikvideo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * FP-019: Türkü video project repository interface.
 *
 * <p>IcerikFabrikasiVideoProject henüz JPA Entity değil — in-memory domain object olarak kullanılır.
 * Runtime ve test'lerde InMemoryIcerikFabrikasiVideoProjectRepository implement eder.</p>
 */
public interface IcerikFabrikasiVideoProjectRepository {

    IcerikFabrikasiVideoProject save(IcerikFabrikasiVideoProject project);

    Optional<IcerikFabrikasiVideoProject> findById(UUID id);

    List<IcerikFabrikasiVideoProject> findAll();

    long count();

    void deleteAll();

    List<IcerikFabrikasiVideoProject> findByWorkflowStage(IcerikFabrikasiVideoWorkflowStage workflowStage);

    List<IcerikFabrikasiVideoProject> findByNeedsHumanReviewTrue();

    List<IcerikFabrikasiVideoProject> findByIcerikFabrikasiNameContainingIgnoreCase(String icerikFabrikasiName);

    List<IcerikFabrikasiVideoProject> findBySourceConfidence(IcerikFabrikasiSourceConfidence sourceConfidence);
}
