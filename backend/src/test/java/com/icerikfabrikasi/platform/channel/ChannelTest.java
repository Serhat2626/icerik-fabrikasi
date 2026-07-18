package com.icerikfabrikasi.platform.channel;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Channel entity ve ilişkili enum/value object testleri.
 */
class ChannelTest {

    @Nested
    @DisplayName("Channel oluşturma")
    class ChannelCreation {

        @Test
        @DisplayName("Builder ile Money Mindset kanalı oluşturulur")
        void shouldCreateMoneyMindsetChannel() {
            Channel channel = Channel.builder()
                    .name("Money Mindset Daily")
                    .niche(ChannelNiche.MONEY_MINDSET)
                    .targetPlatform(TargetPlatform.YOUTUBE_SHORTS)
                    .status(ChannelStatus.SETUP)
                    .contentLanguage("en")
                    .weeklyVideoTarget(5)
                    .backgroundColor("#0D0D0D")
                    .primaryTextColor("#FFFFFF")
                    .accentColor("#FFD700")
                    .fontFamily("Montserrat")
                    .animationStyle(TextAnimationStyle.FADE_IN)
                    .watermarkText("@MoneyMindsetDaily")
                    .targetDurationSeconds(45)
                    .targetSceneCount(8)
                    .build();

            assertThat(channel.getName()).isEqualTo("Money Mindset Daily");
            assertThat(channel.getNiche()).isEqualTo(ChannelNiche.MONEY_MINDSET);
            assertThat(channel.getTargetPlatform()).isEqualTo(TargetPlatform.YOUTUBE_SHORTS);
            assertThat(channel.getStatus()).isEqualTo(ChannelStatus.SETUP);
            assertThat(channel.getContentLanguage()).isEqualTo("en");
            assertThat(channel.getBackgroundColor()).isEqualTo("#0D0D0D");
            assertThat(channel.getAccentColor()).isEqualTo("#FFD700");
        }

        @Test
        @DisplayName("Quiz & Trivia kanalı oluşturulur")
        void shouldCreateQuizChannel() {
            Channel channel = Channel.builder()
                    .name("Brain Buzz")
                    .niche(ChannelNiche.QUIZ_TRIVIA)
                    .targetPlatform(TargetPlatform.YOUTUBE_SHORTS)
                    .status(ChannelStatus.SETUP)
                    .contentLanguage("en")
                    .build();

            assertThat(channel.getNiche()).isEqualTo(ChannelNiche.QUIZ_TRIVIA);
        }

        @Test
        @DisplayName("AI & Tech kanalı oluşturulur")
        void shouldCreateAiTechChannel() {
            Channel channel = Channel.builder()
                    .name("AI Insider")
                    .niche(ChannelNiche.AI_TECH)
                    .targetPlatform(TargetPlatform.YOUTUBE_SHORTS)
                    .status(ChannelStatus.SETUP)
                    .contentLanguage("en")
                    .build();

            assertThat(channel.getNiche()).isEqualTo(ChannelNiche.AI_TECH);
        }
    }

    @Nested
    @DisplayName("Visual Identity")
    class VisualIdentityTests {

        @Test
        @DisplayName("toVisualIdentity() kanal ayarlarını döndürür")
        void shouldConvertToVisualIdentity() {
            Channel channel = Channel.builder()
                    .name("Test")
                    .niche(ChannelNiche.MONEY_MINDSET)
                    .targetPlatform(TargetPlatform.YOUTUBE_SHORTS)
                    .status(ChannelStatus.SETUP)
                    .contentLanguage("en")
                    .backgroundColor("#0D0D0D")
                    .primaryTextColor("#FFFFFF")
                    .accentColor("#FFD700")
                    .secondaryAccentColor("#FF6B35")
                    .fontFamily("Montserrat")
                    .animationStyle(TextAnimationStyle.SLIDE_UP)
                    .watermarkText("@test")
                    .build();

            ChannelVisualIdentity vi = channel.toVisualIdentity();

            assertThat(vi.getBackgroundColor()).isEqualTo("#0D0D0D");
            assertThat(vi.getPrimaryTextColor()).isEqualTo("#FFFFFF");
            assertThat(vi.getAccentColor()).isEqualTo("#FFD700");
            assertThat(vi.getSecondaryAccentColor()).isEqualTo("#FF6B35");
            assertThat(vi.getFontFamily()).isEqualTo("Montserrat");
            assertThat(vi.getAnimationStyle()).isEqualTo(TextAnimationStyle.SLIDE_UP);
            assertThat(vi.getWatermarkText()).isEqualTo("@test");
        }

        @Test
        @DisplayName("Null animasyon stili FADE_IN default alır")
        void shouldDefaultToFadeIn() {
            Channel channel = Channel.builder()
                    .name("Test")
                    .niche(ChannelNiche.MONEY_MINDSET)
                    .targetPlatform(TargetPlatform.YOUTUBE_SHORTS)
                    .status(ChannelStatus.SETUP)
                    .contentLanguage("en")
                    .build();

            ChannelVisualIdentity vi = channel.toVisualIdentity();
            assertThat(vi.getAnimationStyle()).isEqualTo(TextAnimationStyle.FADE_IN);
        }
    }

    @Nested
    @DisplayName("Prompt Config")
    class PromptConfigTests {

        @Test
        @DisplayName("toPromptConfig() AI ayarlarını döndürür")
        void shouldConvertToPromptConfig() {
            Channel channel = Channel.builder()
                    .name("Test")
                    .niche(ChannelNiche.MONEY_MINDSET)
                    .targetPlatform(TargetPlatform.YOUTUBE_SHORTS)
                    .status(ChannelStatus.SETUP)
                    .contentLanguage("en")
                    .aiSystemPrompt("You are a finance expert")
                    .aiScriptTemplate("Write about {topic}")
                    .contentTone("authoritative")
                    .targetDurationSeconds(45)
                    .targetSceneCount(8)
                    .build();

            ChannelPromptConfig config = channel.toPromptConfig();

            assertThat(config.getSystemPrompt()).isEqualTo("You are a finance expert");
            assertThat(config.getScriptTemplate()).isEqualTo("Write about {topic}");
            assertThat(config.getContentTone()).isEqualTo("authoritative");
            assertThat(config.getTargetDurationSeconds()).isEqualTo(45);
            assertThat(config.getTargetSceneCount()).isEqualTo(8);
        }
    }

    @Nested
    @DisplayName("Metrik sayaçları")
    class MetricsTests {

        @Test
        @DisplayName("Video üretim sayacı artırılır")
        void shouldIncrementVideosProduced() {
            Channel channel = Channel.builder()
                    .name("Test")
                    .niche(ChannelNiche.MONEY_MINDSET)
                    .targetPlatform(TargetPlatform.YOUTUBE_SHORTS)
                    .status(ChannelStatus.ACTIVE)
                    .contentLanguage("en")
                    .build();

            assertThat(channel.getTotalVideosProduced()).isZero();
            channel.incrementVideosProduced();
            assertThat(channel.getTotalVideosProduced()).isEqualTo(1);
            channel.incrementVideosProduced();
            assertThat(channel.getTotalVideosProduced()).isEqualTo(2);
        }

        @Test
        @DisplayName("Video yayınlama sayacı artırılır")
        void shouldIncrementVideosPublished() {
            Channel channel = Channel.builder()
                    .name("Test")
                    .niche(ChannelNiche.MONEY_MINDSET)
                    .targetPlatform(TargetPlatform.YOUTUBE_SHORTS)
                    .status(ChannelStatus.ACTIVE)
                    .contentLanguage("en")
                    .build();

            assertThat(channel.getTotalVideosPublished()).isZero();
            channel.incrementVideosPublished();
            assertThat(channel.getTotalVideosPublished()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Enum testleri")
    class EnumTests {

        @Test
        @DisplayName("ChannelNiche display adları doğru")
        void nicheDisplayNames() {
            assertThat(ChannelNiche.MONEY_MINDSET.getDisplayName()).isEqualTo("Money Mindset");
            assertThat(ChannelNiche.QUIZ_TRIVIA.getDisplayName()).isEqualTo("Quiz & Trivia");
            assertThat(ChannelNiche.AI_TECH.getDisplayName()).isEqualTo("AI & Tech");
        }

        @Test
        @DisplayName("TargetPlatform kısıtlamaları doğru")
        void platformConstraints() {
            assertThat(TargetPlatform.YOUTUBE_SHORTS.getMaxDurationSeconds()).isEqualTo(60);
            assertThat(TargetPlatform.YOUTUBE_SHORTS.getAspectRatio()).isEqualTo("9:16");
            assertThat(TargetPlatform.YOUTUBE_LONG.getAspectRatio()).isEqualTo("16:9");
            assertThat(TargetPlatform.TIKTOK.getMaxDurationSeconds()).isEqualTo(60);
        }

        @Test
        @DisplayName("TextAnimationStyle tüm değerler mevcut")
        void animationStyles() {
            assertThat(TextAnimationStyle.values()).hasSize(7);
        }

        @Test
        @DisplayName("ChannelStatus tüm durumlar mevcut")
        void channelStatuses() {
            assertThat(ChannelStatus.values()).containsExactly(
                    ChannelStatus.SETUP,
                    ChannelStatus.ACTIVE,
                    ChannelStatus.PAUSED,
                    ChannelStatus.ARCHIVED
            );
        }
    }
}
