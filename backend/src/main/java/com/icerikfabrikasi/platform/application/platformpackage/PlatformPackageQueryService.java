package com.icerikfabrikasi.platform.application.platformpackage;

import com.icerikfabrikasi.platform.common.exception.NotFoundException;
import com.icerikfabrikasi.platform.platformpackage.entity.PlatformPackageDraft;
import com.icerikfabrikasi.platform.platformpackage.repository.PlatformPackageDraftRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * FP-004: Platform paketi taslağı read-only query service.
 * Controller'dan çağrılır, iş kuralı içermez.
 * FP-003 StoryboardQueryService pattern'ı ile tutarlıdır.
 */
@Service
@RequiredArgsConstructor
public class PlatformPackageQueryService {

    private final PlatformPackageDraftRepository platformPackageDraftRepository;

    /**
     * Content Project'e ait tüm platform paket taslaklarını listeler.
     * PackageType asc, version desc sıralı.
     */
    @Transactional(readOnly = true)
    public List<PlatformPackageDraft> listByContentProject(UUID contentProjectId) {
        List<PlatformPackageDraft> drafts = platformPackageDraftRepository
                .findByContentProjectIdOrderByPackageTypeAscVersionDesc(contentProjectId);
        // Force lazy load: open-in-view=false, session kapanır
        drafts.forEach(d -> {
            d.getSceneUsages().size();
            d.getContentProject().getId();
            d.getSourceStoryboardDraft().getId();
        });
        return drafts;
    }

    /**
     * StoryboardDraft'a ait tüm platform paket taslaklarını listeler.
     * PackageType asc, version desc sıralı.
     */
    @Transactional(readOnly = true)
    public List<PlatformPackageDraft> listBySourceStoryboard(UUID storyboardDraftId) {
        List<PlatformPackageDraft> drafts = platformPackageDraftRepository
                .findBySourceStoryboardDraftIdOrderByPackageTypeAscVersionDesc(storyboardDraftId);
        // Force lazy load
        drafts.forEach(d -> {
            d.getSceneUsages().size();
            d.getContentProject().getId();
        });
        return drafts;
    }

    /**
     * Platform paket taslağını scene usage'larıyla birlikte döndürür.
     */
    @Transactional(readOnly = true)
    public PlatformPackageDraft getDetail(UUID packageDraftId) {
        PlatformPackageDraft draft = platformPackageDraftRepository.findById(packageDraftId)
                .orElseThrow(() -> new NotFoundException("PlatformPackageDraft", packageDraftId));
        // Force lazy load
        draft.getSceneUsages().size();
        draft.getContentProject().getId();
        draft.getSourceStoryboardDraft().getId();
        return draft;
    }
}
