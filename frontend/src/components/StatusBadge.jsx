import './StatusBadge.css';

const STATUS_CONFIG = {
  ACTIVE: {
    label: 'Active',
    className: 'status-active',
    icon: '●',
  },
  PAUSED: {
    label: 'Paused',
    className: 'status-paused',
    icon: '❚❚',
  },
  SETUP: {
    label: 'Setup',
    className: 'status-setup',
    icon: '◐',
  },
  ARCHIVED: {
    label: 'Archived',
    className: 'status-archived',
    icon: '◌',
  },
};

export default function StatusBadge({ status, size = 'default' }) {
  const config = STATUS_CONFIG[status] || STATUS_CONFIG.SETUP;

  return (
    <span className={`status-badge ${config.className} status-${size}`}>
      <span className="status-indicator">{config.icon}</span>
      <span className="status-label">{config.label}</span>
    </span>
  );
}
