/**
 * İçerik Fabrikası — REST API Client
 */
const API = {
    BASE: '/api/admin',

    async get(path) {
        const res = await fetch(this.BASE + path);
        if (!res.ok) throw new Error(`API Hata: ${res.status}`);
        return res.json();
    },

    async post(path, body) {
        const res = await fetch(this.BASE + path, {
            method: 'POST',
            headers: body ? { 'Content-Type': 'application/json' } : {},
            body: body ? JSON.stringify(body) : undefined
        });
        if (!res.ok) throw new Error(`API Hata: ${res.status}`);
        return res.json();
    },

    // ── Channels ──
    async getChannels() {
        return this.get('/channels');
    },

    async getChannel(id) {
        return this.get(`/channels/${id}`);
    },

    async activateChannel(id) {
        return this.post(`/channels/${id}/activate`);
    },

    async pauseChannel(id) {
        return this.post(`/channels/${id}/pause`);
    },

    // ── Scripts ──
    async getScripts(channelId, status) {
        let path = `/shorts-scripts?channelId=${channelId}`;
        if (status) path += `&status=${status}`;
        return this.get(path);
    },

    async getScript(id) {
        return this.get(`/shorts-scripts/${id}`);
    },

    async approveScript(id) {
        return this.post(`/shorts-scripts/${id}/approve`);
    },

    async renderScript(id) {
        return this.post(`/shorts-scripts/${id}/render`);
    },

    async publishScript(id) {
        return this.post(`/shorts-scripts/${id}/publish`);
    },

    async requestRevision(id, notes) {
        return this.post(`/shorts-scripts/${id}/request-revision`, notes);
    },

    // ── Batch Operations ──
    async batchRender(channelId) {
        return this.post(`/channels/${channelId}/batch-render`);
    },

    async batchPublish(channelId) {
        return this.post(`/channels/${channelId}/batch-publish`);
    }
};
