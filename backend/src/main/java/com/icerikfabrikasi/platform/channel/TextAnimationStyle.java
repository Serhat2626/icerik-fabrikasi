package com.icerikfabrikasi.platform.channel;

/**
 * Text overlay animasyon stili.
 */
public enum TextAnimationStyle {

    /** Metin yavaşça belirme */
    FADE_IN,

    /** Metin aşağıdan yukarı kayma */
    SLIDE_UP,

    /** Metin soldan sağa kayma */
    SLIDE_LEFT,

    /** Metin kelime kelime belirme */
    WORD_BY_WORD,

    /** Metin karakter karakter belirme (typewriter efekti) */
    TYPEWRITER,

    /** Animasyon yok — anında görünür */
    NONE,

    /** Metin zıplama efekti */
    BOUNCE
}
