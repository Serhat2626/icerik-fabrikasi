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

    try {
        const scriptsRes = await API.getScripts(channelId);
        const approved = scriptsRes.data.filter(s => s.status === 'APPROVED');

        if (approved.length === 0) {
            Toast.warning('Render edilecek onaylı script yok. Önce script\'leri onaylayın.');
            return;
        }

        // Progress bar oluştur
        showProgressBar(`🎬 ${name} — Render`, approved.length);

        let ok = 0, fail = 0;
        for (let i = 0; i < approved.length; i++) {
            const script = approved[i];
            updateProgressBar(i, approved.length, `Render: "${script.title}"`);
            try {
                await API.renderScript(script.id);
                ok++;
            } catch (e) {
                fail++;
                console.warn('Render hatası:', script.id, e);
            }
        }

        hideProgressBar();
        Toast.success(`Render tamamlandı: ${ok}/${approved.length} başarılı` + (fail > 0 ? `, ${fail} hata` : ''));
        renderOverview();
    } catch (err) {
        hideProgressBar();
        Toast.error('Render hatası: ' + err.message);
    }
}

async function doBatchPublish(channelId, name) {
    if (!confirm(`"${name}" kanalının tüm render edilmiş videoları yayınlanacak. Devam?`)) return;

    try {
        const scriptsRes = await API.getScripts(channelId);
        const rendered = scriptsRes.data.filter(s => s.status === 'RENDERED');

        if (rendered.length === 0) {
            Toast.warning('Yayınlanacak render edilmiş video yok.');
            return;
        }

        showProgressBar(`📤 ${name} — Yayınlama`, rendered.length);

        let ok = 0, fail = 0;
        for (let i = 0; i < rendered.length; i++) {
            const script = rendered[i];
            updateProgressBar(i, rendered.length, `Yayınlanıyor: "${script.title}"`);
            try {
                await API.publishScript(script.id);
                ok++;
            } catch (e) {
                fail++;
                console.warn('Publish hatası:', script.id, e);
            }
        }

        hideProgressBar();
        Toast.success(`Yayınlama tamamlandı: ${ok}/${rendered.length} başarılı` + (fail > 0 ? `, ${fail} hata` : ''));
        renderOverview();
    } catch (err) {
        hideProgressBar();
        Toast.error('Yayınlama hatası: ' + err.message);
    }
}

// ── Progress Bar ──
function showProgressBar(title, total) {
    let bar = document.getElementById('batch-progress');
    if (!bar) {
        bar = document.createElement('div');
        bar.id = 'batch-progress';
        bar.innerHTML = `
            <div class="progress-title"></div>
            <div class="progress-status"></div>
            <div class="progress-track"><div class="progress-fill"></div></div>
        `;
        bar.style.cssText = `
            position: fixed; bottom: 30px; left: 50%; transform: translateX(-50%);
            background: rgba(15,15,30,0.95); border: 1px solid var(--accent-primary);
            border-radius: 12px; padding: 20px 30px; z-index: 10000; min-width: 450px;
            box-shadow: 0 8px 32px rgba(0,0,0,0.5), 0 0 15px rgba(108,99,255,0.3);
            backdrop-filter: blur(10px);
        `;
        document.body.appendChild(bar);
    }
    bar.querySelector('.progress-title').textContent = title;
    bar.querySelector('.progress-title').style.cssText = 'font-weight:700;font-size:16px;margin-bottom:8px;color:white;';
    bar.querySelector('.progress-status').style.cssText = 'font-size:13px;color:#aaa;margin-bottom:10px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;max-width:400px;';
    bar.querySelector('.progress-track').style.cssText = 'height:8px;background:rgba(255,255,255,0.1);border-radius:4px;overflow:hidden;';
    bar.querySelector('.progress-fill').style.cssText = 'height:100%;background:linear-gradient(90deg,var(--accent-primary),var(--accent-secondary));border-radius:4px;transition:width 0.3s ease;width:0%';
    bar.style.display = 'block';
}

function updateProgressBar(current, total, statusText) {
    const bar = document.getElementById('batch-progress');
    if (!bar) return;
    const pct = Math.round(((current + 1) / total) * 100);
    bar.querySelector('.progress-fill').style.width = pct + '%';
    bar.querySelector('.progress-status').textContent = `${current + 1}/${total} — ${statusText}`;
}

function hideProgressBar() {
    const bar = document.getElementById('batch-progress');
    if (bar) {
        bar.querySelector('.progress-fill').style.width = '100%';
        bar.querySelector('.progress-status').textContent = '✅ Tamamlandı!';
        setTimeout(() => bar.remove(), 2000);
    }
}
