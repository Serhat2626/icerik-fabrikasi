import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { fetchChannels, activateChannel, pauseChannel } from '../api';
import StatusBadge from '../components/StatusBadge';
import './ChannelList.css';

export default function ChannelList() {
  const [channels, setChannels] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [togglingId, setTogglingId] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    loadChannels();
  }, []);

  async function loadChannels() {
    try {
      setLoading(true);
      setError(null);
      const data = await fetchChannels();
      setChannels(data || []);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  async function handleToggleStatus(e, channel) {
    e.stopPropagation();
    try {
      setTogglingId(channel.id);
      if (channel.status === 'ACTIVE') {
        await pauseChannel(channel.id);
      } else {
        await activateChannel(channel.id);
      }
      await loadChannels();
    } catch (err) {
      setError(err.message);
    } finally {
      setTogglingId(null);
    }
  }

  return (
    <div className="channel-list-page">
      <header className="page-header">
        <div>
          <h1><span className="gradient-text">Channels</span></h1>
          <p>Manage your YouTube channels and their configurations.</p>
        </div>
      </header>

      {error && (
        <div className="dashboard__error">
          <span>⚠️ {error}</span>
          <button className="btn-ghost" onClick={loadChannels}>Retry</button>
        </div>
      )}

      {loading ? (
        <div className="channel-list__grid">
          {[1, 2, 3, 4].map((i) => (
            <div key={i} className="channel-list-card glass-card">
              <div className="skeleton" style={{ height: 8, width: '50%', marginBottom: 16 }} />
              <div className="skeleton" style={{ height: 16, width: '70%', marginBottom: 10 }} />
              <div className="skeleton" style={{ height: 10, width: '35%', marginBottom: 16 }} />
              <div className="skeleton" style={{ height: 32, width: '100%' }} />
            </div>
          ))}
        </div>
      ) : channels.length === 0 ? (
        <div className="dashboard__empty glass-card">
          <span className="dashboard__empty-icon">📺</span>
          <h3>No channels found</h3>
          <p>Add channels through the backend configuration.</p>
        </div>
      ) : (
        <div className="channel-list__grid">
          {channels.map((channel, index) => (
            <article
              key={channel.id}
              className="channel-list-card glass-card"
              onClick={() => navigate(`/channels/${channel.id}`)}
              style={{
                '--card-accent': channel.visualIdentity?.accentColor || '#00D4FF',
                animationDelay: `${index * 70}ms`,
              }}
            >
              <div className="channel-list-card__accent" />

              {/* Header */}
              <div className="channel-list-card__top">
                <div className="channel-list-card__avatar" style={{
                  background: `linear-gradient(135deg, ${channel.visualIdentity?.accentColor || '#00D4FF'}44, ${channel.visualIdentity?.accentColor || '#00D4FF'}11)`,
                  borderColor: `${channel.visualIdentity?.accentColor || '#00D4FF'}33`,
                }}>
                  📺
                </div>
                <StatusBadge status={channel.status} />
              </div>

              {/* Info */}
              <h3 className="channel-list-card__name">{channel.channelName}</h3>
              <span className="channel-list-card__niche">{channel.niche}</span>

              {/* Meta */}
              <div className="channel-list-card__meta">
                {channel.targetLanguage && (
                  <div className="channel-list-card__meta-item">
                    <span className="meta-icon">🌐</span>
                    <span>{channel.targetLanguage}</span>
                  </div>
                )}
                <div className="channel-list-card__meta-item">
                  <span className="meta-icon">🎬</span>
                  <span>{channel.videoCount ?? 0} videos</span>
                </div>
              </div>

              {/* Actions */}
              <div className="channel-list-card__actions">
                <button
                  className={channel.status === 'ACTIVE' ? 'btn-secondary' : 'btn-primary'}
                  onClick={(e) => handleToggleStatus(e, channel)}
                  disabled={togglingId === channel.id}
                  style={{ flex: 1 }}
                >
                  {togglingId === channel.id
                    ? '...'
                    : channel.status === 'ACTIVE'
                      ? '⏸ Pause'
                      : '▶ Activate'
                  }
                </button>
                <button
                  className="btn-secondary"
                  onClick={(e) => {
                    e.stopPropagation();
                    navigate(`/channels/${channel.id}`);
                  }}
                >
                  Details →
                </button>
              </div>
            </article>
          ))}
        </div>
      )}
    </div>
  );
}
