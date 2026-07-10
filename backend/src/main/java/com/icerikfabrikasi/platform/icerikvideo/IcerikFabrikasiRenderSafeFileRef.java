package com.icerikfabrikasi.platform.icerikvideo;

import java.util.UUID;

/**
 * FP-024 Task 03: Opaque safe file reference.
 * No filesystem paths. No real file access.
 */
public final class IcerikFabrikasiRenderSafeFileRef {
    private final String fileId;
    private final String opaqueRef;
    private final String extension;
    private final String purpose;

    public IcerikFabrikasiRenderSafeFileRef(String fileId, String opaqueRef,
                                   String extension, String purpose) {
        this.fileId = fileId;
        this.opaqueRef = opaqueRef;
        this.extension = extension;
        this.purpose = purpose;
    }

    public IcerikFabrikasiRenderSafeFileRef(String extension, String purpose) {
        this(UUID.randomUUID().toString(),
                "internal-file:" + UUID.randomUUID(),
                extension, purpose);
    }

    public String getFileId() { return fileId; }
    public String getOpaqueRef() { return opaqueRef; }
    public String getExtension() { return extension; }
    public String getPurpose() { return purpose; }
}
