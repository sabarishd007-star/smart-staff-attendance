import React, { useEffect, useState } from 'react';

const NotificationCenter = ({ token: propToken }) => {
  const [alerts, setAlerts] = useState([]);
  const [dismissed, setDismissed] = useState({});

  useEffect(() => {
    fetchNotifications();
  }, []);

  const fetchNotifications = async () => {
    try {
      const token = propToken || localStorage.getItem('jwt_token') || localStorage.getItem('token');
      const baseUrl = typeof API_BASE_URL !== 'undefined'
        ? API_BASE_URL
        : 'https://vitalscan-api-y891.onrender.com/api/v1';

      const res = await fetch(`${baseUrl}/notifications/my-alerts`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      if (res.ok) {
        const data = await res.json();
        setAlerts(data);
      }
    } catch (err) {
      console.error("Failed to load notifications", err);
    }
  };

  const visibleAlerts = alerts.filter((_, idx) => !dismissed[idx]);
  if (visibleAlerts.length === 0) return null;

  return (
    <div className="space-y-2 mb-4">
      {alerts.map((alert, idx) => {
        if (dismissed[idx]) return null;

        const styles = {
          SUCCESS: {
            wrapper: 'bg-emerald-50 border border-emerald-200 text-emerald-900',
            icon: '✅',
            badge: 'bg-emerald-100 text-emerald-700',
          },
          DANGER: {
            wrapper: 'bg-rose-50 border border-rose-200 text-rose-900',
            icon: '❌',
            badge: 'bg-rose-100 text-rose-700',
          },
          INFO: {
            wrapper: 'bg-blue-50 border border-blue-200 text-blue-900',
            icon: '🕐',
            badge: 'bg-blue-100 text-blue-700',
          },
        }[alert.type] || {
          wrapper: 'bg-slate-50 border border-slate-200 text-slate-800',
          icon: 'ℹ️',
          badge: 'bg-slate-100 text-slate-700',
        };

        return (
          <div key={idx} className={`flex items-start gap-3 px-4 py-3 rounded-xl shadow-sm ${styles.wrapper}`}>
            <span className="text-lg leading-none mt-0.5">{styles.icon}</span>
            <div className="flex-1 min-w-0">
              <p className="text-xs font-bold leading-tight">{alert.title}</p>
              <p className="text-xs mt-0.5 leading-relaxed opacity-90">{alert.message}</p>
            </div>
            <button
              onClick={() => setDismissed(d => ({ ...d, [idx]: true }))}
              className="text-xs opacity-60 hover:opacity-100 transition ml-2 shrink-0 font-bold"
              aria-label="Dismiss notification"
            >
              ✕
            </button>
          </div>
        );
      })}
    </div>
  );
};

export default NotificationCenter;
