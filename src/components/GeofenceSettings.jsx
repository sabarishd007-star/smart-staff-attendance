import React, { useState } from 'react';

const CAMPUS_LATITUDE  = 13.0827;
const CAMPUS_LONGITUDE = 80.2707;

const GeofenceSettings = ({ token: propToken }) => {
  const [radius, setRadius]   = useState(500);
  const [lat, setLat]         = useState(CAMPUS_LATITUDE);
  const [lng, setLng]         = useState(CAMPUS_LONGITUDE);
  const [saved, setSaved]     = useState(false);

  const handleSave = (e) => {
    e.preventDefault();
    // Config is YAML-driven; this surfaces a visual confirmation for the admin.
    setSaved(true);
    setTimeout(() => setSaved(false), 3000);
  };

  return (
    <section className="rounded-xl border border-slate-200 bg-white p-6 shadow-sm space-y-4">
      <div>
        <h2 className="text-lg font-semibold text-slate-800">Campus Geofence Configuration</h2>
        <p className="text-xs text-slate-500">
          Current campus boundary settings — update <code className="bg-slate-100 px-1 rounded">application.yml</code> or environment variables to persist changes.
        </p>
      </div>

      <form onSubmit={handleSave} className="space-y-4 max-w-md">
        <div className="grid grid-cols-2 gap-3">
          <div>
            <label className="block text-xs font-semibold text-slate-600 uppercase tracking-wider mb-1">
              Campus Latitude
            </label>
            <input
              type="number"
              step="0.0001"
              value={lat}
              onChange={(e) => setLat(e.target.value)}
              className="w-full rounded-lg border border-slate-300 px-3 py-2 text-xs outline-none focus:border-indigo-500 focus:ring-2 focus:ring-indigo-200"
            />
          </div>
          <div>
            <label className="block text-xs font-semibold text-slate-600 uppercase tracking-wider mb-1">
              Campus Longitude
            </label>
            <input
              type="number"
              step="0.0001"
              value={lng}
              onChange={(e) => setLng(e.target.value)}
              className="w-full rounded-lg border border-slate-300 px-3 py-2 text-xs outline-none focus:border-indigo-500 focus:ring-2 focus:ring-indigo-200"
            />
          </div>
        </div>

        <div>
          <label className="block text-xs font-semibold text-slate-600 uppercase tracking-wider mb-1">
            Allowed Radius (Metres)
          </label>
          <div className="flex items-center gap-3">
            <input
              type="range"
              min="100"
              max="2000"
              step="50"
              value={radius}
              onChange={(e) => setRadius(Number(e.target.value))}
              className="flex-1 accent-indigo-600"
            />
            <span className="w-16 text-center text-xs font-bold text-indigo-700 bg-indigo-50 border border-indigo-200 rounded px-2 py-1">
              {radius}m
            </span>
          </div>
        </div>

        <div className="p-3 bg-amber-50 border border-amber-200 rounded-lg text-xs text-amber-800">
          <strong>📍 Current Campus:</strong> {lat}°N, {lng}°E &nbsp;|&nbsp;
          <strong>Radius:</strong> {radius}m boundary
        </div>

        <button
          type="submit"
          className="w-full rounded-lg bg-indigo-600 py-2.5 text-xs font-bold text-white shadow hover:bg-indigo-700 transition"
        >
          Preview Geofence Settings
        </button>

        {saved && (
          <p className="text-xs text-emerald-700 font-semibold text-center">
            ✅ Settings previewed! Update <code>application.yml</code> to apply permanently.
          </p>
        )}
      </form>
    </section>
  );
};

export default GeofenceSettings;
