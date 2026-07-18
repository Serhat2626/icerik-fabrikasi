import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { useState } from 'react';
import Sidebar from './components/Sidebar';
import Dashboard from './pages/Dashboard';
import ChannelList from './pages/ChannelList';
import ChannelDetail from './pages/ChannelDetail';
import './App.css';

export default function App() {
  const [sidebarCollapsed, setSidebarCollapsed] = useState(false);

  return (
    <BrowserRouter>
      <div className="app-layout">
        <Sidebar
          collapsed={sidebarCollapsed}
          onToggle={() => setSidebarCollapsed((prev) => !prev)}
        />
        <main className={`main-content ${sidebarCollapsed ? 'sidebar-collapsed' : ''}`}>
          <Routes>
            <Route path="/" element={<Dashboard />} />
            <Route path="/channels" element={<ChannelList />} />
            <Route path="/channels/:id" element={<ChannelDetail />} />
            <Route path="/channels/:id/affiliate-links" element={<ChannelDetail />} />
          </Routes>
        </main>
      </div>
    </BrowserRouter>
  );
}
