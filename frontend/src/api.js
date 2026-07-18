const API_BASE = 'http://localhost:8082/api/admin';

/**
 * Generic fetch wrapper for the İçerik Fabrikası API.
 * Extracts `data` from the ApiResponse wrapper { success, data, message }.
 */
async function request(endpoint, options = {}) {
  const url = `${API_BASE}${endpoint}`;

  const config = {
    headers: {
      'Content-Type': 'application/json',
      ...options.headers,
    },
    ...options,
  };

  // Remove content-type for GET/HEAD
  if (!options.body) {
    delete config.headers['Content-Type'];
  }

  const response = await fetch(url, config);

  if (!response.ok) {
    let errorMessage = `HTTP ${response.status}`;
    try {
      const errorData = await response.json();
      errorMessage = errorData.message || errorMessage;
    } catch {
      // response wasn't JSON
    }
    throw new Error(errorMessage);
  }

  // Some endpoints may return 204 No Content
  if (response.status === 204) {
    return null;
  }

  const json = await response.json();

  // Unwrap the ApiResponse wrapper
  if (json && typeof json === 'object' && 'success' in json) {
    if (!json.success) {
      throw new Error(json.message || 'API request failed');
    }
    return json.data;
  }

  return json;
}

// ── Channel Endpoints ──

export async function fetchChannels() {
  return request('/channels');
}

export async function fetchChannel(channelId) {
  return request(`/channels/${channelId}`);
}

export async function activateChannel(channelId) {
  return request(`/channels/${channelId}/activate`, { method: 'POST' });
}

export async function pauseChannel(channelId) {
  return request(`/channels/${channelId}/pause`, { method: 'POST' });
}

// ── Affiliate Link Endpoints ──

export async function fetchAffiliateLinks(channelId) {
  return request(`/channels/${channelId}/affiliate-links`);
}

export async function fetchActiveAffiliateLinks(channelId) {
  return request(`/channels/${channelId}/affiliate-links/active`);
}

export async function addAffiliateLink(channelId, linkData) {
  return request(`/channels/${channelId}/affiliate-links`, {
    method: 'POST',
    body: JSON.stringify(linkData),
  });
}

export async function updateAffiliateLink(channelId, linkId, linkData) {
  return request(`/channels/${channelId}/affiliate-links/${linkId}`, {
    method: 'PUT',
    body: JSON.stringify(linkData),
  });
}

export async function deactivateAffiliateLink(channelId, linkId) {
  return request(`/channels/${channelId}/affiliate-links/${linkId}/deactivate`, {
    method: 'POST',
  });
}

export async function activateAffiliateLink(channelId, linkId) {
  return request(`/channels/${channelId}/affiliate-links/${linkId}/activate`, {
    method: 'POST',
  });
}
