/**
 * İçerik Fabrikası — SPA Router & Initialization
 */
(function() {
    // Render header
    renderHeader();

    // Hash-based router
    function route() {
        const hash = location.hash || '#/';
        const parts = hash.replace('#/', '').split('/');

        if (parts[0] === 'channels' && parts[1]) {
            renderChannelPage(parts[1]);
        } else if (parts[0] === 'scripts' && parts[1]) {
            renderScriptPage(parts[1]);
        } else {
            renderOverview();
        }
    }

    window.addEventListener('hashchange', route);
    route(); // initial load
})();
