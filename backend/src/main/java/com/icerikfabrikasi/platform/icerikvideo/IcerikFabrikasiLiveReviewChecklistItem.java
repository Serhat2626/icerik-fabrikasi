package com.icerikfabrikasi.platform.icerikvideo;

import java.util.List;

/**
 * LIVE-005: Human review checklist item.
 */
public final class IcerikFabrikasiLiveReviewChecklistItem {
    private final String label;
    private final boolean checked;
    private final String note;

    public IcerikFabrikasiLiveReviewChecklistItem(String label, boolean checked, String note) {
        this.label = label;
        this.checked = checked;
        this.note = note;
    }

    public String getLabel() { return label; }
    public boolean isChecked() { return checked; }
    public String getNote() { return note; }

    public static List<IcerikFabrikasiLiveReviewChecklistItem> defaultChecklist() {
        return List.of(
                new IcerikFabrikasiLiveReviewChecklistItem("Gorsel kalitesi kabul edilebilir", false, ""),
                new IcerikFabrikasiLiveReviewChecklistItem("Ses/muzik uyumlu", false, ""),
                new IcerikFabrikasiLiveReviewChecklistItem("Metin/altyazi dogru", false, ""),
                new IcerikFabrikasiLiveReviewChecklistItem("Telif ihlali yok", false, ""),
                new IcerikFabrikasiLiveReviewChecklistItem("Icerik politikasina uygun", false, ""),
                new IcerikFabrikasiLiveReviewChecklistItem("Teknik format gecerli", false, ""));
    }
}
