import React, { useEffect, useState } from 'react';

const HodDashboard = ({ token: propToken }) => {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchHodData();
  }, []);

  const fetchHodData = async () => {
    try {
      const token = propToken || localStorage.getItem('jwt_token') || localStorage.getItem('token');
      const baseUrl = typeof API_BASE_URL !== 'undefined'
        ? API_BASE_URL
        : 'https://vitalscan-api-y891.onrender.com/api/v1';

      const res = await fetch(`${baseUrl}/hod/dashboard`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      if (res.ok) {
        const result = await res.json();
        setData(result);
      }
    } catch (err) {
      console.error("Failed to load HOD dashboard", err);
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <div className="p-4 text-center text-xs text-slate-500 font-medium">Loading HOD Dashboard...</div>;
  if (!data) return <div className="p-4 text-center text-xs text-rose-500 font-medium">Failed to load department data.</div>;

  return (
    <div className="p-6 space-y-6 max-w-5xl mx-auto bg-white rounded-xl border border-slate-200 shadow-sm">
      <div className="border-b border-slate-100 pb-4">
        <h1 className="text-xl font-bold text-slate-800">{data.department} Department Overview</h1>
        <p className="text-xs text-slate-500">HOD Departmental Analytics & Attendance Logs</p>
      </div>

      {/* Metric Cards */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        <div className="p-4 bg-slate-50 border border-slate-200 rounded-lg text-center">
          <p className="text-[10px] font-bold text-slate-500 uppercase tracking-wider">TOTAL STAFF</p>
          <p className="text-2xl font-bold text-slate-800 mt-1">{data.totalStaff}</p>
        </div>
        <div className="p-4 bg-emerald-50 border border-emerald-200 rounded-lg text-center">
          <p className="text-[10px] font-bold text-emerald-600 uppercase tracking-wider">PRESENT</p>
          <p className="text-2xl font-bold text-emerald-900 mt-1">{data.presentCount}</p>
        </div>
        <div className="p-4 bg-amber-50 border border-amber-200 rounded-lg text-center">
          <p className="text-[10px] font-bold text-amber-600 uppercase tracking-wider">PENDING</p>
          <p className="text-2xl font-bold text-amber-900 mt-1">{data.pendingCount}</p>
        </div>
        <div className="p-4 bg-rose-50 border border-rose-200 rounded-lg text-center">
          <p className="text-[10px] font-bold text-rose-600 uppercase tracking-wider">REJECTED</p>
          <p className="text-2xl font-bold text-rose-900 mt-1">{data.rejectedCount}</p>
        </div>
      </div>

      {/* Logs Table */}
      <div className="border border-slate-200 rounded-lg overflow-hidden">
        <h3 className="bg-slate-100 px-4 py-3 font-semibold text-xs text-slate-700 uppercase tracking-wider">Department Attendance Records</h3>
        <table className="w-full text-left text-xs text-slate-600">
          <thead className="bg-slate-50 text-[10px] font-bold text-slate-700 uppercase border-b">
            <tr>
              <th className="p-3">Staff Name</th>
              <th className="p-3">Date</th>
              <th className="p-3">Time</th>
              <th className="p-3">Status</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-200 bg-white">
            {data.recentLogs && data.recentLogs.length > 0 ? (
              data.recentLogs.map((log, idx) => (
                <tr key={log.attendanceId || log.id || idx} className="hover:bg-slate-50">
                  <td className="p-3 font-medium text-slate-900">{log.staff ? (log.staff.fullName || log.staff.name) : 'N/A'}</td>
                  <td className="p-3 text-slate-600">{log.date}</td>
                  <td className="p-3 text-slate-500">{log.timeMarked || log.time || 'N/A'}</td>
                  <td className="p-3 font-semibold">
                    <span className={`px-2 py-0.5 rounded text-[10px] font-bold tracking-wide ${
                      log.status === 'VERIFIED' || log.status === 'APPROVED' ? 'bg-emerald-100 text-emerald-800' :
                      log.status === 'REJECTED' ? 'bg-rose-100 text-rose-800' :
                      'bg-amber-100 text-amber-800'
                    }`}>
                      {log.status}
                    </span>
                  </td>
                </tr>
              ))
            ) : (
              <tr>
                <td colSpan="4" className="p-4 text-center text-slate-400">No departmental attendance logs found.</td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default HodDashboard;
