async function renderScriptPage(scriptId) {
    const content = document.getElementById('main-content');
    content.innerHTML = '<div class="loading-screen"><div class="loader"></div></div>';

    try {
        const res = await API.getScript(scriptId);
        const script = res.data;

        const scenes = script.scenes || [];
        const totalDuration = scenes.reduce((sum, s) => sum + (s.durationSeconds || 0), 0) + 8; // +hook+cta

        content.innerHTML = `
            <button class="back-btn" onclick="history.back()">← Geri Dön</button>

            <div class="breadcrumb fade-in">
                <a href="#/">Dashboard</a>
                <span class="breadcrumb-sep">›</span>
                <a href="#/channels/${script.channelId}">Kanal</a>
                <span class="breadcrumb-sep">›</span>
                <span>${script.title}</span>
            </div>

            <div class="script-detail fade-in">
                <div>
                    <!-- Title + Status -->
                    <div style="display:flex;align-items:center;gap:12px;margin-bottom:24px;flex-wrap:wrap">
                        <h1 class="page-title" style="margin-bottom:0;flex:1">${script.title}</h1>
                        <span class="badge badge-${script.status.toLowerCase()}" style="font-size:13px;padding:6px 16px">
                            ${statusIcon(script.status)} ${script.status}
                        </span>
                    </div>

                    <!-- Hook -->
                    <div class="script-meta-card" style="margin-bottom:16px;border-left:3px solid var(--accent-gold)">
                        <h3>🪝 Hook</h3>
                        <p style="font-size:16px;font-weight:500">${script.hook || '-'}</p>
                    </div>

                    <!-- Scenes Timeline -->
                    <div class="script-meta-card" style="margin-bottom:16px">
                        <h3>🎬 Sahneler (${scenes.length})</h3>
                        <div class="scene-timeline">
                            ${scenes.map((scene, i) => `
                                <div class="scene-item ${i === 0 ? 'hook' : ''}">
                                    <div class="scene-number">${i + 1}</div>
                                    <div class="scene-text">
                                        ${scene.text}
                                        ${scene.visualHint ? `<div style="font-size:12px;color:var(--text-muted);margin-top:4px">🖼️ ${scene.visualHint}</div>` : ''}
                                    </div>
                                    <div class="scene-duration">${scene.durationSeconds}s</div>
                                </div>
                            `).join('')}
                        </div>
                    </div>

                    <!-- CTA -->
                    ${script.cta ? `
                        <div class="script-meta-card" style="border-left:3px solid var(--accent-green)">
                            <h3>📢 Call to Action</h3>
                            <p>${script.cta}</p>
                        </div>
                    ` : ''}

                    <!-- Video Player -->
                    ${script.videoUrl ? `
                        <div class="script-meta-card" style="margin-top:16px;border-left:3px solid var(--accent-cyan)">
                            <h3>🎥 Video Önizleme</h3>
                            <div style="margin-top:12px;border-radius:12px;overflow:hidden;background:#000;position:relative;max-width:360px;margin:12px auto 0">
                                <video id="video-player" controls playsinline
                                    style="width:100%;max-height:640px;display:block;border-radius:12px"
                                    src="${script.videoUrl}">
                                    Tarayıcınız video oynatmayı desteklemiyor.
                                </video>
                            </div>
                            <div style="text-align:center;margin-top:8px">
                                <a href="${script.videoUrl}"
                                   download style="font-size:12px;color:var(--accent-cyan);margin-top:4px;display:inline-block">
                                    ⬇️ Videoyu İndir
                                </a>
                            </div>
                        </div>
                    ` : ''}


                </div>

                <!-- Sidebar -->
                <div>
                    <!-- Actions -->
                    <div class="script-meta-card" style="margin-bottom:16px">
                        <h3>⚡ Aksiyonlar</h3>
                        <div style="display:flex;flex-direction:column;gap:8px">
                            ${script.status === 'DRAFT' ? `
                                <button class="btn btn-primary" style="width:100%" onclick="doApprove('${script.id}')">✅ Onayla</button>
                            ` : ''}
                            ${script.status === 'APPROVED' ? `
                                <button class="btn btn-warning" style="width:100%" onclick="doRender('${script.id}')">🎬 Render Et</button>
                            ` : ''}
                            ${script.status === 'RENDERED' ? `
                                <button class="btn btn-success" style="width:100%" onclick="doPublish('${script.id}')">📤 YouTube'a Yayınla</button>
                            ` : ''}
                            ${script.status === 'PUBLISHED' ? `
                                <div style="text-align:center;padding:12px;color:var(--accent-green)">
                                    ✅ Yayınlandı
                                </div>
                            ` : ''}
                        </div>
                    </div>

                    <!-- Info -->
                    <div class="script-meta-card" style="margin-bottom:16px">
                        <h3>📋 Bilgiler</h3>
                        <div class="sidebar-section">
                            <div class="info-row">
                                <span class="info-label">Durum</span>
                                <span class="info-value">${script.status}</span>
                            </div>
                            <div class="info-row">
                                <span class="info-label">Sahne Sayısı</span>
                                <span class="info-value">${scenes.length}</span>
                            </div>
                            <div class="info-row">
                                <span class="info-label">Tahmini Süre</span>
                                <span class="info-value">~${totalDuration}s</span>
                            </div>
                            <div class="info-row">
                                <span class="info-label">Konu</span>
                                <span class="info-value">${script.topic || '-'}</span>
                            </div>
                        </div>
                    </div>

                    <!-- Hashtags -->
                    ${script.hashtags ? `
                        <div class="script-meta-card" style="margin-bottom:16px">
                            <h3>#️⃣ Hashtag'ler</h3>
                            <div style="display:flex;flex-wrap:wrap;gap:6px">
                                ${script.hashtags.split(',').map(tag => `
                                    <span style="background:rgba(124,77,255,0.1);color:var(--accent-purple);padding:4px 10px;border-radius:12px;font-size:12px">${tag.trim()}</span>
                                `).join('')}
                            </div>
                        </div>
                    ` : ''}




                    <!-- Status Flow -->
                    <div class="script-meta-card">
                        <h3>📊 Durum Akışı</h3>
                        <div style="display:flex;flex-direction:column;gap:8px">
                            ${['DRAFT','APPROVED','RENDERED','PUBLISHED'].map(s => {
                                const isActive = s === script.status;
                                const isPast = ['DRAFT','APPROVED','RENDERED','PUBLISHED'].indexOf(s) < ['DRAFT','APPROVED','RENDERED','PUBLISHED'].indexOf(script.status);
                                return `
                                    <div style="display:flex;align-items:center;gap:8px;padding:6px;border-radius:6px;
                                        ${isActive ? 'background:rgba(124,77,255,0.15);border:1px solid var(--accent-purple)' : ''}
                                        ${isPast ? 'opacity:0.5' : ''}">
                                        <span>${isPast ? '✅' : isActive ? '▶️' : '⬜'}</span>
                                        <span style="font-size:13px;${isActive ? 'font-weight:600;color:var(--accent-purple)' : 'color:var(--text-muted)'}">${s}</span>
                                    </div>
                                `;
                            }).join('')}
                        </div>
                    </div>
                </div>
            </div>
        `;
    } catch (err) {
        content.innerHTML = `<div class="empty-state"><div class="empty-state-icon">⚠️</div><div class="empty-state-text">${err.message}</div></div>`;
    }
}
