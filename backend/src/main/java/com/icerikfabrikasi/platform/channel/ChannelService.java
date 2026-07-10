package com.icerikfabrikasi.platform.channel;

import com.icerikfabrikasi.platform.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Kanal yönetim servisi.
 *
 * <p>Kanalların oluşturulması, güncellenmesi, yapılandırılması
 * ve durum yönetimini sağlar.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ChannelService {

    private final ChannelRepository channelRepository;

    /**
     * Yeni kanal oluşturur.
     */
    public Channel createChannel(String name, ChannelNiche niche, TargetPlatform platform) {
        log.info("Yeni kanal oluşturuluyor: name={}, niche={}, platform={}", name, niche, platform);

        Channel channel = Channel.builder()
                .name(name)
                .niche(niche)
                .targetPlatform(platform)
                .status(ChannelStatus.SETUP)
                .contentLanguage("en")
                .weeklyVideoTarget(5)
                .targetDurationSeconds(45)
                .targetSceneCount(8)
                .animationStyle(TextAnimationStyle.FADE_IN)
                .build();

        Channel saved = channelRepository.save(channel);
        log.info("Kanal oluşturuldu: id={}, name={}", saved.getId(), saved.getName());
        return saved;
    }

    /**
     * Kanal görsel kimliğini günceller.
     */
    public Channel updateVisualIdentity(UUID channelId, ChannelVisualIdentity identity) {
        Channel channel = getChannelOrThrow(channelId);

        channel.setBackgroundColor(identity.getBackgroundColor());
        channel.setPrimaryTextColor(identity.getPrimaryTextColor());
        channel.setAccentColor(identity.getAccentColor());
        channel.setSecondaryAccentColor(identity.getSecondaryAccentColor());
        channel.setFontFamily(identity.getFontFamily());
        channel.setAnimationStyle(identity.getAnimationStyle());
        channel.setWatermarkText(identity.getWatermarkText());

        log.info("Kanal görsel kimliği güncellendi: id={}", channelId);
        return channelRepository.save(channel);
    }

    /**
     * Kanal AI prompt konfigürasyonunu günceller.
     */
    public Channel updatePromptConfig(UUID channelId, ChannelPromptConfig config) {
        Channel channel = getChannelOrThrow(channelId);

        channel.setAiSystemPrompt(config.getSystemPrompt());
        channel.setAiScriptTemplate(config.getScriptTemplate());
        channel.setContentTone(config.getContentTone());
        channel.setTargetDurationSeconds(config.getTargetDurationSeconds());
        channel.setTargetSceneCount(config.getTargetSceneCount());

        log.info("Kanal AI prompt konfigürasyonu güncellendi: id={}", channelId);
        return channelRepository.save(channel);
    }

    /**
     * Kanal durumunu günceller.
     */
    public Channel updateStatus(UUID channelId, ChannelStatus newStatus) {
        Channel channel = getChannelOrThrow(channelId);
        ChannelStatus oldStatus = channel.getStatus();

        channel.setStatus(newStatus);

        log.info("Kanal durumu güncellendi: id={}, {} -> {}", channelId, oldStatus, newStatus);
        return channelRepository.save(channel);
    }

    /**
     * Aktif kanalları listeler.
     */
    @Transactional(readOnly = true)
    public List<Channel> getActiveChannels() {
        return channelRepository.findByStatus(ChannelStatus.ACTIVE);
    }

    /**
     * Tüm kanalları listeler.
     */
    @Transactional(readOnly = true)
    public List<Channel> getAllChannels() {
        return channelRepository.findAll();
    }

    /**
     * ID ile kanal getirir.
     */
    @Transactional(readOnly = true)
    public Channel getChannel(UUID channelId) {
        return getChannelOrThrow(channelId);
    }

    private Channel getChannelOrThrow(UUID channelId) {
        return channelRepository.findById(channelId)
                .orElseThrow(() -> new NotFoundException("Kanal", channelId));
    }
}
