import { NavLink, useLocation } from 'react-router-dom';
import { useState } from 'react';
import './Sidebar.css';

const NAV_ITEMS = [
  { to: '/', icon: '◆', label: 'Dashboard', exact: true },
  { to: '/channels', icon: '📺', label: 'Channels' },
  { to: '/videos', icon: '🎬', label: 'Videos', disabled: true, badge: 'Soon' },
];

export default function Sidebar({ collapsed, onToggle }) {
  return (
    <aside className={`sidebar ${collapsed ? 'sidebar--collapsed' : ''}`}>
      {/* Brand */}
      <div className="sidebar__brand">
        <div className="sidebar__logo">
          <span className="sidebar__logo-icon">⚡</span>
          {!collapsed && (
            <div className="sidebar__logo-text">
              <span className="sidebar__logo-title neon-text">İçerik Fabrikası</span>
              <span className="sidebar__logo-subtitle">Content Factory</span>
            </div>
          )}
        </div>
      </div>

      {/* Navigation */}
      <nav className="sidebar__nav">
        <div className="sidebar__nav-label">
          {!collapsed && 'MENU'}
        </div>
        {NAV_ITEMS.map((item) => (
          <NavLink
            key={item.to}
            to={item.disabled ? '#' : item.to}
            end={item.exact}
            className={({ isActive }) =>
              `sidebar__link ${isActive && !item.disabled ? 'sidebar__link--active' : ''} ${item.disabled ? 'sidebar__link--disabled' : ''}`
            }
            onClick={(e) => item.disabled && e.preventDefault()}
          >
            <span className="sidebar__link-icon">{item.icon}</span>
            {!collapsed && (
              <>
                <span className="sidebar__link-label">{item.label}</span>
                {item.badge && (
                  <span className="sidebar__link-badge">{item.badge}</span>
                )}
              </>
            )}
            <span className="sidebar__link-indicator" />
          </NavLink>
        ))}
      </nav>

      {/* Collapse toggle */}
      <div className="sidebar__footer">
        <button className="sidebar__toggle" onClick={onToggle} title={collapsed ? 'Expand' : 'Collapse'}>
          <span className={`sidebar__toggle-icon ${collapsed ? 'rotated' : ''}`}>‹</span>
          {!collapsed && <span>Collapse</span>}
        </button>
      </div>
    </aside>
  );
}
