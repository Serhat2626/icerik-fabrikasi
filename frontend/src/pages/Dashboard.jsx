import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { fetchChannels } from '../api';
import StatCard from '../components/StatCard';
import StatusBadge from '../components/StatusBadge';
import './Dashboard.css';

export default function Dashboard() {
  const [channels, setChannels] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    loadChannels();
  }, []);

  async function loadChannels() {
    try {
      setLoading(true);
      const data = await fetchChannels();
      setChannels(data || []);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  const totalChannels = channels.length;
  const activeChannels = channels.filter((c) => c.status === 'ACTIVE').length;
  const totalVideos = channels.reduce((sum, c) => sum + (c.videoCount || 0), 0);

  return (
    <div className="dashboard">
      {/* Welcome Header */}
      <header className="dashboard__header">
        <div className="dashboard__welcome">
          <h1>
            <span className="gradient-text">İçerik Fabrikası</span>
          </h1>
          <p className="dashboard__subtitle">
            Your content factory dashboard — manage channels, videos, and affiliate links.
          </p>
        </div>
        <div className="dashboard__header-actions">
          <button className="btn-primary" onClick={() => navigate('/channels')}>
            📺 View Channels
          </button>
        </div>
      </header>

      {/* Stats Grid */}
      <section className="dashboard__stats" aria-label="Statistics">
        <StatCard
          icon="📺"
          label="Total Channels"
          value={loading ? '...' : totalChannels}
          color="primary"
        />
        <StatCard
          icon="✅"
          label="Active Channels"
          value={loading ? '...' : activeChannels}
          color="success"
        />
        <StatCard
          icon="🎬"
          label="Total Videos"
          value={loading ? '...' : totalVideos}
          color="secondary"
        />
        <StatCard
          icon="🔗"
          label="Affiliate Links"
          value={loading ? '...' : '—'}
          color="warning"
        />
      </section>

      {/* Error */}
      {error && (
        <div className="dashboard__error">
          <span>⚠️ {error}</span>
          <button className="btn-ghost" onClick={loadChannels}>Retry</button>
        </div>
      )}

      {/* Channel Cards */}
      <section className="dashboard__channels">
        <div className="section-header">
          <h2>Channels</h2>
          <span className="section-header__count">{totalChannels}</span>
        </div>

        {loading ? (
          <div className="dashboard__grid">
            {[1, 2, 3].map((i) => (
              <div key={i} className="channel-card-skeleton glass-card">
                <div className="skeleton" style={{ height: 6, width: '60%', marginBottom: 12 }} />
                <div className="skeleton" style={{ height: 14, width: '80%', marginBottom: 8 }} />
                <div className="skeleton" style={{ height: 10, width: '40%' }} />
              </div>
            ))}
          </div>
        ) : channels.length === 0 ? (
          <div className="dashboard__empty glass-card">
            <span className="dashboard__empty-icon">📺</span>
            <h3>No channels yet</h3>
            <p>Channels will appear here once configured in the backend.</p>
          </div>
        ) : (
          <div className="dashboard__grid">
            {channels.map((channel, index) => (
              <article
                key={channel.id}
                className="channel-card glass-card"
                onClick={() => navigate(`/channels/${channel.id}`)}
                style={{
                  '--card-accent': channel.visualIdentity?.accentColor || '#00D4FF',
                  animationDelay: `${index * 60}ms`,
                }}
              >
                <div className="channel-card__accent-bar" />
                <div className="channel-card__header">
                  <div className="channel-card__info">
                    <h3 className="channel-card__name">{channel.channelName}</h3>
                    <span className="channel-card__niche">{channel.niche}</span>
                  </div>
                  <StatusBadge status={channel.status} size="sm" />
                </div>
                <div className="channel-card__meta">
                  {channel.targetLanguage && (
                    <span className="channel-card__tag">🌐 {channel.targetLanguage}</span>
                  )}
                  {channel.videoCount != null && (
                    <span className="channel-card__tag">🎬 {channel.videoCount} videos</span>
                  )}
                </div>
                <div className="channel-card__footer">
                  <span className="channel-card__id">ID: {channel.id}</span>
                  <span className="channel-card__arrow">→</span>
                </div>
              </article>
            ))}
          </div>
        )}
      </section>
    </div>
  );
}
