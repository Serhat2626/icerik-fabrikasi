import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  fetchChannel,
  fetchAffiliateLinks,
  addAffiliateLink,
  updateAffiliateLink,
  deactivateAffiliateLink,
  activateAffiliateLink,
  activateChannel,
  pauseChannel,
} from '../api';
import StatusBadge from '../components/StatusBadge';
import './ChannelDetail.css';

const TABS = ['Overview', 'Affiliate Links', 'Videos'];

export default function ChannelDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [channel, setChannel] = useState(null);
  const [links, setLinks] = useState([]);
  const [activeTab, setActiveTab] = useState('Overview');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [showAddModal, setShowAddModal] = useState(false);
  const [togglingLink, setTogglingLink] = useState(null);
  const [togglingChannel, setTogglingChannel] = useState(false);

  useEffect(() => {
    loadChannel();
  }, [id]);

  useEffect(() => {
    if (activeTab === 'Affiliate Links') {
      loadLinks();
    }
  }, [activeTab, id]);

  async function loadChannel() {
    try {
      setLoading(true);
      setError(null);
      const data = await fetchChannel(id);
      setChannel(data);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  async function loadLinks() {
    try {
      const data = await fetchAffiliateLinks(id);
      setLinks(data || []);
    } catch (err) {
      // Links may not exist yet, that's ok
      setLinks([]);
    }
  }

  async function handleToggleChannel() {
    if (!channel) return;
    try {
      setTogglingChannel(true);
      if (channel.status === 'ACTIVE') {
        await pauseChannel(id);
      } else {
        await activateChannel(id);
      }
      await loadChannel();
    } catch (err) {
      setError(err.message);
    } finally {
      setTogglingChannel(false);
    }
  }

  async function handleToggleLink(link) {
    try {
      setTogglingLink(link.id);
      if (link.active) {
        await deactivateAffiliateLink(id, link.id);
      } else {
        await activateAffiliateLink(id, link.id);
      }
      await loadLinks();
    } catch (err) {
      setError(err.message);
    } finally {
      setTogglingLink(null);
    }
  }

  async function handleAddLink(linkData) {
    try {
      await addAffiliateLink(id, linkData);
      setShowAddModal(false);
      await loadLinks();
    } catch (err) {
      setError(err.message);
    }
  }

  const accentColor = channel?.visualIdentity?.accentColor || '#00D4FF';

  if (loading) {
    return (
      <div className="channel-detail">
        <div className="channel-detail__loading">
          <div className="skeleton" style={{ height: 32, width: '40%', marginBottom: 16 }} />
          <div className="skeleton" style={{ height: 16, width: '25%', marginBottom: 32 }} />
          <div className="skeleton" style={{ height: 300, width: '100%' }} />
        </div>
      </div>
    );
  }

  if (error && !channel) {
    return (
      <div className="channel-detail">
        <div className="dashboard__error">
          <span>⚠️ {error}</span>
          <button className="btn-ghost" onClick={loadChannel}>Retry</button>
        </div>
        <button className="btn-secondary" onClick={() => navigate('/channels')}>← Back to Channels</button>
      </div>
    );
  }

  if (!channel) return null;

  return (
    <div className="channel-detail" style={{ '--channel-accent': accentColor }}>
      {/* Header */}
      <header className="channel-detail__header">
        <button className="btn-ghost" onClick={() => navigate('/channels')}>← Channels</button>
        <div className="channel-detail__header-main">
          <div className="channel-detail__title-row">
            <div className="channel-detail__avatar" style={{
              background: `linear-gradient(135deg, ${accentColor}44, ${accentColor}11)`,
              borderColor: `${accentColor}33`,
            }}>
              📺
            </div>
            <div>
              <h1 className="channel-detail__name">{channel.channelName}</h1>
              <div className="channel-detail__subtitle">
                <span className="channel-detail__niche">{channel.niche}</span>
                <StatusBadge status={channel.status} />
              </div>
            </div>
          </div>
          <div className="channel-detail__actions">
            <button
              className={channel.status === 'ACTIVE' ? 'btn-secondary' : 'btn-primary'}
              onClick={handleToggleChannel}
              disabled={togglingChannel}
            >
              {togglingChannel ? '...' : channel.status === 'ACTIVE' ? '⏸ Pause' : '▶ Activate'}
            </button>
          </div>
        </div>
      </header>

      {/* Error banner */}
      {error && (
        <div className="dashboard__error" style={{ marginBottom: 'var(--space-lg)' }}>
          <span>⚠️ {error}</span>
          <button className="btn-ghost" onClick={() => setError(null)}>Dismiss</button>
        </div>
      )}

      {/* Tabs */}
      <nav className="channel-detail__tabs">
        {TABS.map((tab) => (
          <button
            key={tab}
            className={`channel-detail__tab ${activeTab === tab ? 'channel-detail__tab--active' : ''}`}
            onClick={() => setActiveTab(tab)}
          >
            {tab}
            {tab === 'Videos' && <span className="sidebar__link-badge">Soon</span>}
          </button>
        ))}
      </nav>

      {/* Tab Content */}
      <div className="channel-detail__content">
        {activeTab === 'Overview' && <OverviewTab channel={channel} />}
        {activeTab === 'Affiliate Links' && (
          <AffiliateLinksTab
            links={links}
            onToggle={handleToggleLink}
            onAdd={() => setShowAddModal(true)}
            togglingLink={togglingLink}
          />
        )}
        {activeTab === 'Videos' && <VideosTab />}
      </div>

      {/* Add Link Modal */}
      {showAddModal && (
        <AddLinkModal
          onClose={() => setShowAddModal(false)}
          onSubmit={handleAddLink}
        />
      )}
    </div>
  );
}

/* ── Overview Tab ── */
function OverviewTab({ channel }) {
  const vi = channel.visualIdentity || {};
  const voice = channel.voiceConfig || {};

  return (
    <div className="overview-tab">
      {/* Channel Info */}
      <section className="detail-section glass-card">
        <h3 className="detail-section__title">📋 Channel Information</h3>
        <div className="detail-grid">
          <InfoItem label="Channel ID" value={channel.id} mono />
          <InfoItem label="Channel Name" value={channel.channelName} />
          <InfoItem label="Niche" value={channel.niche} />
          <InfoItem label="Target Language" value={channel.targetLanguage} />
          <InfoItem label="Status" value={<StatusBadge status={channel.status} />} />
          {channel.youtubeChannelId && (
            <InfoItem label="YouTube Channel ID" value={channel.youtubeChannelId} mono />
          )}
        </div>
      </section>

      {/* Visual Identity */}
      {(vi.accentColor || vi.fontFamily) && (
        <section className="detail-section glass-card">
          <h3 className="detail-section__title">🎨 Visual Identity</h3>
          <div className="detail-grid">
            {vi.accentColor && (
              <InfoItem
                label="Accent Color"
                value={
                  <div className="color-preview">
                    <span className="color-swatch" style={{ background: vi.accentColor }} />
                    <span className="mono">{vi.accentColor}</span>
                  </div>
                }
              />
            )}
            {vi.secondaryColor && (
              <InfoItem
                label="Secondary Color"
                value={
                  <div className="color-preview">
                    <span className="color-swatch" style={{ background: vi.secondaryColor }} />
                    <span className="mono">{vi.secondaryColor}</span>
                  </div>
                }
              />
            )}
            {vi.fontFamily && <InfoItem label="Font Family" value={vi.fontFamily} />}
            {vi.thumbnailStyle && <InfoItem label="Thumbnail Style" value={vi.thumbnailStyle} />}
          </div>
        </section>
      )}

      {/* Voice Config */}
      {(voice.ttsProvider || voice.voiceId) && (
        <section className="detail-section glass-card">
          <h3 className="detail-section__title">🎙️ Voice Configuration</h3>
          <div className="detail-grid">
            {voice.ttsProvider && <InfoItem label="TTS Provider" value={voice.ttsProvider} />}
            {voice.voiceId && <InfoItem label="Voice ID" value={voice.voiceId} mono />}
            {voice.speed && <InfoItem label="Speed" value={voice.speed} />}
            {voice.pitch && <InfoItem label="Pitch" value={voice.pitch} />}
          </div>
        </section>
      )}
    </div>
  );
}

function InfoItem({ label, value, mono }) {
  return (
    <div className="info-item">
      <span className="info-item__label">{label}</span>
      <span className={`info-item__value ${mono ? 'mono' : ''}`}>{value || '—'}</span>
    </div>
  );
}

/* ── Affiliate Links Tab ── */
function AffiliateLinksTab({ links, onToggle, onAdd, togglingLink }) {
  return (
    <div className="affiliate-tab">
      <div className="affiliate-tab__header">
        <h3>Affiliate Links ({links.length})</h3>
        <button className="btn-primary" onClick={onAdd}>
          + Add Link
        </button>
      </div>

      {links.length === 0 ? (
        <div className="dashboard__empty glass-card">
          <span className="dashboard__empty-icon">🔗</span>
          <h3>No affiliate links yet</h3>
          <p>Add affiliate links to include in your video descriptions.</p>
          <button className="btn-primary" onClick={onAdd} style={{ marginTop: 16 }}>
            + Add First Link
          </button>
        </div>
      ) : (
        <div className="affiliate-grid">
          {links.map((link, index) => (
            <div
              key={link.id || index}
              className="affiliate-card glass-card"
              style={{ animationDelay: `${index * 50}ms` }}
            >
              <div className="affiliate-card__header">
                <div className="affiliate-card__emoji">
                  {link.emoji || '🔗'}
                </div>
                <div className="affiliate-card__info">
                  <h4 className="affiliate-card__name">{link.productName || link.title}</h4>
                  {link.merchantName && (
                    <span className="affiliate-card__merchant">{link.merchantName}</span>
                  )}
                </div>
                <div className="affiliate-card__status">
                  <button
                    className={`toggle-btn ${link.active ? 'toggle-btn--active' : ''}`}
                    onClick={() => onToggle(link)}
                    disabled={togglingLink === link.id}
                    title={link.active ? 'Deactivate' : 'Activate'}
                  >
                    <span className="toggle-btn__track">
                      <span className="toggle-btn__thumb" />
                    </span>
                  </button>
                </div>
              </div>

              {link.url && (
                <a
                  href={link.url}
                  className="affiliate-card__url"
                  target="_blank"
                  rel="noopener noreferrer"
                  onClick={(e) => e.stopPropagation()}
                >
                  {link.url.length > 50 ? link.url.substring(0, 50) + '...' : link.url}
                  <span className="affiliate-card__external">↗</span>
                </a>
              )}

              {/* Commission info */}
              {(link.commissionRate || link.commissionType) && (
                <div className="affiliate-card__commission">
                  <span className="commission-badge">
                    💰 {link.commissionRate}{link.commissionType === 'PERCENTAGE' ? '%' : ' TL'} commission
                  </span>
                </div>
              )}

              {/* Keywords */}
              {link.keywords && link.keywords.length > 0 && (
                <div className="affiliate-card__keywords">
                  {link.keywords.map((kw, i) => (
                    <span key={i} className="keyword-badge">{kw}</span>
                  ))}
                </div>
              )}

              {/* Priority & Category */}
              <div className="affiliate-card__footer">
                {link.priority != null && (
                  <span className="affiliate-card__meta-item">Priority: {link.priority}</span>
                )}
                {link.category && (
                  <span className="affiliate-card__meta-item">{link.category}</span>
                )}
                <span className={`affiliate-card__active-label ${link.active ? 'active' : 'inactive'}`}>
                  {link.active ? '● Active' : '○ Inactive'}
                </span>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

/* ── Videos Tab (Placeholder) ── */
function VideosTab() {
  return (
    <div className="videos-tab">
      <div className="dashboard__empty glass-card">
        <span className="dashboard__empty-icon">🎬</span>
        <h3>Videos — Coming Soon</h3>
        <p>Video management will be available in a future update.</p>
      </div>
    </div>
  );
}

/* ── Add Link Modal ── */
function AddLinkModal({ onClose, onSubmit }) {
  const [form, setForm] = useState({
    productName: '',
    url: '',
    emoji: '🔗',
    merchantName: '',
    commissionRate: '',
    commissionType: 'PERCENTAGE',
    keywords: '',
    priority: 0,
    category: '',
    customDescription: '',
  });
  const [submitting, setSubmitting] = useState(false);

  function handleChange(e) {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setSubmitting(true);
    const data = {
      ...form,
      commissionRate: form.commissionRate ? parseFloat(form.commissionRate) : undefined,
      priority: parseInt(form.priority, 10) || 0,
      keywords: form.keywords
        ? form.keywords.split(',').map((k) => k.trim()).filter(Boolean)
        : [],
    };
    await onSubmit(data);
    setSubmitting(false);
  }

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h2>Add Affiliate Link</h2>
          <button className="btn-icon" onClick={onClose}>✕</button>
        </div>

        <form onSubmit={handleSubmit}>
          <div className="form-row">
            <div className="form-group" style={{ flex: '0 0 80px' }}>
              <label>Emoji</label>
              <input name="emoji" value={form.emoji} onChange={handleChange} />
            </div>
            <div className="form-group" style={{ flex: 1 }}>
              <label>Product Name *</label>
              <input name="productName" value={form.productName} onChange={handleChange} required />
            </div>
          </div>

          <div className="form-group">
            <label>URL *</label>
            <input name="url" type="url" value={form.url} onChange={handleChange} required placeholder="https://..." />
          </div>

          <div className="form-group">
            <label>Merchant Name</label>
            <input name="merchantName" value={form.merchantName} onChange={handleChange} placeholder="e.g. Amazon" />
          </div>

          <div className="form-row">
            <div className="form-group" style={{ flex: 1 }}>
              <label>Commission Rate</label>
              <input name="commissionRate" type="number" step="0.01" value={form.commissionRate} onChange={handleChange} placeholder="e.g. 10" />
            </div>
            <div className="form-group" style={{ flex: 1 }}>
              <label>Commission Type</label>
              <select name="commissionType" value={form.commissionType} onChange={handleChange}>
                <option value="PERCENTAGE">Percentage (%)</option>
                <option value="FIXED">Fixed (TL)</option>
              </select>
            </div>
          </div>

          <div className="form-group">
            <label>Keywords (comma-separated)</label>
            <input name="keywords" value={form.keywords} onChange={handleChange} placeholder="e.g. tech, laptop, gaming" />
          </div>

          <div className="form-row">
            <div className="form-group" style={{ flex: 1 }}>
              <label>Category</label>
              <input name="category" value={form.category} onChange={handleChange} placeholder="e.g. Electronics" />
            </div>
            <div className="form-group" style={{ flex: '0 0 100px' }}>
              <label>Priority</label>
              <input name="priority" type="number" value={form.priority} onChange={handleChange} />
            </div>
          </div>

          <div className="form-group">
            <label>Custom Description</label>
            <textarea name="customDescription" value={form.customDescription} onChange={handleChange} placeholder="Optional description for the link..." />
          </div>

          <div className="form-actions">
            <button type="button" className="btn-secondary" onClick={onClose}>Cancel</button>
            <button type="submit" className="btn-primary" disabled={submitting}>
              {submitting ? 'Adding...' : '+ Add Link'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
