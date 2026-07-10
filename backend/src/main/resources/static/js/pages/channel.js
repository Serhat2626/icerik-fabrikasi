async function renderChannelPage(channelId) {
    const content = document.getElementById('main-content');
    content.innerHTML = '<div class="loading-screen"><div class="loader"></div></div>';

    try {
        const [channelRes, scriptsRes] = await Promise.all([
            API.getChannel(channelId),
            API.getScripts(channelId)
        ]);

        const channel = channelRes.data;
        const scripts = scriptsRes.data || [];
        const theme = CHANNEL_THEMES[channel.niche] || { icon: '📺', accent: '#7C4DFF' };

        let currentFilter = 'ALL';

        function renderContent() {
            const filtered = currentFilter === 'ALL'
                ? scripts
                : scripts.filter(s => s.status === currentFilter);

            const statusCounts = {
                ALL: scripts.length,
                DRAFT: scripts.filter(s => s.status === 'DRAFT').length,
                APPROVED: scripts.filter(s => s.status === 'APPROVED').length,
                RENDERED: scripts.filter(s => s.status === 'RENDERED').length,
                PUBLISHED: scripts.filter(s => s.status === 'PUBLISHED').length
            };

            content.innerHTML = `
                <button class="back-btn" onclick="location.hash='#/'">← Dashboard'a Dön</button>

                <div class="fade-in" style="display:flex;align-items:center;gap:16px;margin-bottom:24px">
                    <div class="channel-icon" style="width:56px;height:56px;font-size:28px;background:${theme.accent}20;color:${theme.accent};border-radius:12px">
                        ${theme.icon}
                    </div>
                    <div>
                        <h1 class="page-title" style="margin-bottom:0">${channel.name}</h1>
                        <p class="page-subtitle" style="margin-bottom:0">${channel.description || channel.niche.replace('_', ' ')}</p>
                    </div>
                </div>

                <div class="stats-row fade-in">
                    <div class="stat-card"><div class="stat-label">Toplam</div><div class="stat-value cyan">${scripts.length}</div></div>
                    <div class="stat-card"><div class="stat-label">Draft</div><div class="stat-value gold">${statusCounts.DRAFT}</div></div>
                    <div class="stat-card"><div class="stat-label">Rendered</div><div class="stat-value" style="color:var(--status-rendered)">${statusCounts.RENDERED}</div></div>
                    <div class="stat-card"><div class="stat-label">Yayında</div><div class="stat-value green">${statusCounts.PUBLISHED}</div></div>
                </div>

                <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px;flex-wrap:wrap;gap:8px">
                    <div class="filter-bar" id="filter-bar">
                        ${['ALL','DRAFT','APPROVED','RENDERED','PUBLISHED'].map(s => `
                            <button class="filter-btn ${currentFilter === s ? 'active' : ''}"
                                    onclick="window._channelFilter('${s}')">
                                ${s === 'ALL' ? '📋 Tümü' : statusIcon(s) + ' ' + s}
                                <span class="filter-count">${statusCounts[s]}</span>
                            </button>
                        `).join('')}
                    </div>

                    <div style="display:flex;gap:8px">
                        <button class="btn btn-outline btn-sm" onclick="doApproveAll('${channelId}', ${JSON.stringify(scripts.filter(s=>s.status==='DRAFT').map(s=>s.id)).replace(/"/g, '&quot;')})">
                            ✅ Hepsini Onayla
                        </button>
                        <button class="btn btn-primary btn-sm" onclick="doBatchRender('${channelId}','${channel.name}')">
                            🎬 Toplu Render
                        </button>
                        <button class="btn btn-success btn-sm" onclick="doBatchPublish('${channelId}','${channel.name}')">
                            📤 Toplu Yayınla
                        </button>
                    </div>
                </div>

                <table class="scripts-table fade-in">
                    <thead>
                        <tr>
                            <th>#</th>
                            <th>Başlık</th>
                            <th>Durum</th>
                            <th>Sahne</th>
                            <th>Süre</th>
                            <th>Aksiyonlar</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${filtered.map((s, i) => `
                            <tr onclick="location.hash='#/scripts/${s.id}'">
                                <td style="color:var(--text-muted)">${i + 1}</td>
                                <td class="script-title-cell">${s.title}</td>
                                <td><span class="badge badge-${s.status.toLowerCase()}">${statusIcon(s.status)} ${s.status}</span></td>
                                <td style="color:var(--text-muted)">${s.sceneCount || '-'}</td>
                                <td style="color:var(--text-muted)">${s.targetDurationSeconds || '-'}s</td>
                                <td onclick="event.stopPropagation()">
                                    ${renderScriptActions(s)}
                                </td>
                            </tr>
                        `).join('')}
                    </tbody>
                </table>

                ${filtered.length === 0 ? `
                    <div class="empty-state">
                        <div class="empty-state-icon">📭</div>
                        <div class="empty-state-text">Bu durumda script yok</div>
                    </div>
                ` : ''}
            `;
        }

        window._channelFilter = (status) => {
            currentFilter = status;
            renderContent();
        };

        renderContent();
    } catch (err) {
        content.innerHTML = `<div class="empty-state"><div class="empty-state-icon">⚠️</div><div class="empty-state-text">${err.message}</div></div>`;
    }
}

function statusIcon(status) {
    return { DRAFT: '🟡', APPROVED: '🔵', RENDERED: '🟠', PUBLISHED: '🟢', IN_REVIEW: '🔍' }[status] || '⚪';
}

function renderScriptActions(script) {
    const actions = [];
    switch (script.status) {
        case 'DRAFT':
            actions.push(`<button class="btn btn-primary btn-sm" onclick="doApprove('${script.id}')">✅ Onayla</button>`);
            break;
        case 'APPROVED':
            actions.push(`<button class="btn btn-warning btn-sm" onclick="doRender('${script.id}')">🎬 Render</button>`);
            break;
        case 'RENDERED':
            actions.push(`<button class="btn btn-success btn-sm" onclick="doPublish('${script.id}')">📤 Yayınla</button>`);
            break;
        case 'PUBLISHED':
            actions.push(`<span style="color:var(--accent-green);font-size:12px">✓ Yayında</span>`);
            break;
    }
    return actions.join('');
}

async function doApprove(scriptId) {
    try {
        await API.approveScript(scriptId);
        Toast.success('Script onaylandı');
        location.hash = location.hash; // refresh
        window.dispatchEvent(new HashChangeEvent('hashchange'));
    } catch (e) { Toast.error(e.message); }
}

async function doRender(scriptId) {
    Toast.info('Render başlıyor...');
    try {
        await API.renderScript(scriptId);
        Toast.success('Render tamamlandı');
        window.dispatchEvent(new HashChangeEvent('hashchange'));
    } catch (e) { Toast.error(e.message); }
}

async function doPublish(scriptId) {
    try {
        await API.publishScript(scriptId);
        Toast.success('Video yayınlandı');
        window.dispatchEvent(new HashChangeEvent('hashchange'));
    } catch (e) { Toast.error(e.message); }
}

async function doApproveAll(channelId, draftIds) {
    if (!draftIds.length) { Toast.info('Onaylanacak draft script yok'); return; }
    if (!confirm(`${draftIds.length} script onaylanacak. Devam?`)) return;
    let ok = 0;
    for (const id of draftIds) {
        try { await API.approveScript(id); ok++; } catch(e) { /* skip */ }
    }
    Toast.success(`${ok}/${draftIds.length} script onaylandı`);
    window.dispatchEvent(new HashChangeEvent('hashchange'));
}
