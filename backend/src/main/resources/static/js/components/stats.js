function renderStatsRow(channels, allScripts) {
    const total = allScripts.length;
    const draft = allScripts.filter(s => s.status === 'DRAFT').length;
    const rendered = allScripts.filter(s => s.status === 'RENDERED').length;
    const published = allScripts.filter(s => s.status === 'PUBLISHED').length;

    return `
        <div class="stats-row fade-in">
            <div class="stat-card">
                <div class="stat-label">Toplam Kanal</div>
                <div class="stat-value purple">${channels.length}</div>
            </div>
            <div class="stat-card">
                <div class="stat-label">Toplam Script</div>
                <div class="stat-value cyan">${total}</div>
            </div>
            <div class="stat-card">
                <div class="stat-label">Render Bekliyor</div>
                <div class="stat-value gold">${draft}</div>
            </div>
            <div class="stat-card">
                <div class="stat-label">Yayınlandı</div>
                <div class="stat-value green">${published}</div>
            </div>
        </div>
    `;
}
