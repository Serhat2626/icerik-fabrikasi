import './StatCard.css';

export default function StatCard({ icon, label, value, trend, color = 'primary' }) {
  return (
    <div className={`stat-card stat-card--${color}`}>
      <div className="stat-card__icon-wrap">
        <span className="stat-card__icon">{icon}</span>
      </div>
      <div className="stat-card__content">
        <span className="stat-card__label">{label}</span>
        <span className="stat-card__value">{value ?? '—'}</span>
        {trend !== undefined && (
          <span className={`stat-card__trend ${trend >= 0 ? 'trend-up' : 'trend-down'}`}>
            {trend >= 0 ? '↑' : '↓'} {Math.abs(trend)}%
          </span>
        )}
      </div>
      <div className="stat-card__glow" />
    </div>
  );
}
