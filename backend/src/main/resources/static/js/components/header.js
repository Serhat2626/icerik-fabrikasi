function renderHeader() {
    document.getElementById('main-header').innerHTML = `
        <div class="header">
            <div class="header-logo" onclick="location.hash='#/'">
                <div class="header-logo-icon">🏭</div>
                <span class="header-logo-text">İçerik Fabrikası</span>
            </div>
            <div class="header-nav">
                <button class="header-nav-btn active" onclick="location.hash='#/'">📊 Dashboard</button>
                <a class="header-nav-btn" href="/swagger-ui.html" target="_blank">📡 API Docs</a>
            </div>
        </div>
    `;
}
