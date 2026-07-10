package com.icerikfabrikasi.platform.channel;

/**
 * Hedef yayın platformu.
 */
public enum TargetPlatform {

    YOUTUBE_SHORTS("YouTube Shorts", 60, "9:16"),
    YOUTUBE_LONG("YouTube Long-form", 900, "16:9"),
    TIKTOK("TikTok", 60, "9:16"),
    INSTAGRAM_REELS("Instagram Reels", 90, "9:16");

    private final String displayName;
    private final int maxDurationSeconds;
    private final String aspectRatio;

    TargetPlatform(String displayName, int maxDurationSeconds, String aspectRatio) {
        this.displayName = displayName;
        this.maxDurationSeconds = maxDurationSeconds;
        this.aspectRatio = aspectRatio;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getMaxDurationSeconds() {
        return maxDurationSeconds;
    }

    public String getAspectRatio() {
        return aspectRatio;
    }
}
