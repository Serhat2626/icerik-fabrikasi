package com.icerikfabrikasi.platform.channel;

/**
 * Kanal durumu.
 */
public enum ChannelStatus {

    /** Kurulum aşamasında — henüz yayın yok */
    SETUP,

    /** Aktif — içerik üretiliyor ve yayınlanıyor */
    ACTIVE,

    /** Durduruldu — geçici olarak içerik üretimi durduruldu */
    PAUSED,

    /** Arşiv — kanal kapatıldı / pivot yapıldı */
    ARCHIVED
}
