const CHANNEL_THEMES = {
    'MONEY_MINDSET': { icon: '💰', accent: '#FFD700' },
    'QUIZ_TRIVIA':   { icon: '🧠', accent: '#00E5FF' },
    'AI_TECH':       { icon: '🤖', accent: '#7C4DFF' }
};

async function renderOverview() {
    const content = document.getElementById('main-content');
    content.innerHTML = '<div class="loading-screen"><div class="loader"></div><p>Yükleniyor...</p></div>';

    try {
        const channelsRes = await API.getChannels();
        const channels = channelsRes.data || [];

        // Her kanal için script'leri çek
        const allScripts = [];
        const channelScripts = {};

        for (const ch of channels) {
            const scriptsRes = await API.getScripts(ch.id);
            const scripts = scriptsRes.data || [];
            channelScripts[ch.id] = scripts;
            allScripts.push(...scripts);
        }

        content.innerHTML = `
            <div class="fade-in">
                <h1 class="page-title">İçerik Fabrikası</h1>
                <p class="page-subtitle">3 kanal · ${allScripts.length} video script · Tam otomasyon</p>
            </div>

            ${renderStatsRow(channels, allScripts)}

            <div class="channels-grid">
                ${channels.map(ch => renderChannelCard(ch, channelScripts[ch.id] || [])).join('')}
            </div>
        `;
    } catch (err) {
        content.innerHTML = `
            <div class="empty-state">
                <div class="empty-state-icon">⚠️</div>
                <div class="empty-state-text">API'ye bağlanılamadı — backend çalışıyor mu?</div>
                <p style="color:var(--text-muted);margin-top:8px;font-size:13px">${err.message}</p>
                <button class="btn btn-outline" style="margin-top:16px" onclick="renderOverview()">Tekrar Dene</button>
            </div>
        `;
    }
}

function renderChannelCard(channel, scripts) {
    const theme = CHANNEL_THEMES[channel.niche] || { icon: '📺', accent: '#7C4DFF' };
    const draft = scripts.filter(s => s.status === 'DRAFT').length;
    const approved = scripts.filter(s => s.status === 'APPROVED').length;
    const rendered = scripts.filter(s => s.status === 'RENDERED').length;
    const published = scripts.filter(s => s.status === 'PUBLISHED').length;

    return `
        <div class="channel-card fade-in" style="--card-accent: ${theme.accent}"
             onclick="location.hash='#/channels/${channel.id}'">
            <div class="channel-card-header">
                <div class="channel-icon" style="background: ${theme.accent}20; color: ${theme.accent}">
                    ${theme.icon}
                </div>
                <div>
                    <div class="channel-name">${channel.name}</div>
                    <div class="channel-niche">${channel.niche.replace('_', ' ')}</div>
                </div>
            </div>

            <div class="channel-stats">
                <div class="channel-stat">
                    <div class="channel-stat-value" style="color: var(--status-draft)">${draft}</div>
                    <div class="channel-stat-label">Draft</div>
                </div>
                <div class="channel-stat">
                    <div class="channel-stat-value" style="color: var(--status-approved)">${approved}</div>
                    <div class="channel-stat-label">Onaylı</div>
                </div>
                <div class="channel-stat">
                    <div class="channel-stat-value" style="color: var(--status-rendered)">${rendered}</div>
                    <div class="channel-stat-label">Rendered</div>
                </div>
                <div class="channel-stat">
                    <div class="channel-stat-value" style="color: var(--status-published)">${published}</div>
                    <div class="channel-stat-label">Yayında</div>
                </div>
            </div>

            <div class="channel-actions" onclick="event.stopPropagation()">
                <button class="btn btn-warning btn-sm" onclick="doApproveAll('${channel.id}', '${channel.name}', ${draft})"
                    ${draft === 0 ? 'disabled' : ''}>
                    ✅ Hepsini Onayla (${draft})
                </button>
                <button class="btn btn-primary btn-sm" onclick="doBatchRender('${channel.id}', '${channel.name}')"
                    ${approved === 0 ? 'disabled' : ''}>
                    🎬 Toplu Render (${approved})
                </button>
                <button class="btn btn-success btn-sm" onclick="doBatchPublish('${channel.id}', '${channel.name}')"
                    ${rendered === 0 ? 'disabled' : ''}>
                    📤 Toplu Yayınla (${rendered})
                </button>
            </div>
        </div>
    `;
}

async function doApproveAll(channelId, name, count) {
    if (!confirm(`"${name}" kanalının ${count} draft script'i onaylanacak. Devam?`)) return;
    Toast.info('Script\'ler onaylanıyor...');
    try {
        const scriptsRes = await API.getScripts(channelId);
        const drafts = scriptsRes.data.filter(s => s.status === 'DRAFT');
        let ok = 0, fail = 0;
        for (const script of drafts) {
            try {
                await API.approveScript(script.id);
                ok++;
            } catch (e) {
                fail++;
                console.warn('Approve hatası:', script.id, e);
            }
        }
        Toast.success(`${ok}/${drafts.length} script onaylandı` + (fail > 0 ? `, ${fail} hata` : ''));
        renderOverview();
    } catch (err) {
        Toast.error('Onaylama hatası: ' + err.message);
    }
}

async function doBatchRender(channelId, name) {
    if (!confirm(`"${name}" kanalının tüm onaylı script'leri render edilecek. Devam?`)) return;
    Toast.info('Toplu render başlıyor...');
    try {
        const res = await API.batchRender(channelId);
        const report = res.data;
        Toast.success(`Render tamamlandı: ${report.successCount}/${report.totalScripts} başarılı`);
        renderOverview();
    } catch (err) {
        Toast.error('Render hatası: ' + err.message);
    }
}

async function doBatchPublish(channelId, name) {
    if (!confirm(`"${name}" kanalının tüm render edilmiş videoları yayınlanacak. Devam?`)) return;
    Toast.info('Toplu yayınlama başlıyor...');
    try {
        const res = await API.batchPublish(channelId);
        const report = res.data;
        Toast.success(`Yayınlama tamamlandı: ${report.successCount}/${report.totalScripts} başarılı`);
        renderOverview();
    } catch (err) {
        Toast.error('Yayınlama hatası: ' + err.message);
    }
}
