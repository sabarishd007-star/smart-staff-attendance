import React, { useEffect, useState } from 'react';

const AttendanceHistory = ({ token: propToken }) => {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchHistory();
  }, []);

  const fetchHistory = async () => {
    try {
      const token = propToken || localStorage.getItem('jwt_token') || localStorage.getItem('token');
      const baseUrl = typeof API_BASE_URL !== 'undefined'
        ? API_BASE_URL
        : 'https://vitalscan-api-y891.onrender.com/api/v1';

      const res = await fetch(`${baseUrl}/attendance/history`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      if (res.ok) {
        const result = await res.json();
        setData(result);
      }
    } catch (err) {
      console.error("Failed to load attendance history", err);
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <div className="p-4 text-center text-sm text-slate-500">Loading attendance history...</div>;
  if (!data || !data.records || data.records.length === 0) return <div className="p-4 text-center text-sm text-slate-500">No attendance records found.</div>;

  return (
    <div className="mt-6 space-y-6">
      {/* Summary Cards */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        <div className="p-4 bg-blue-50 border border-blue-200 rounded-lg text-center">
          <p className="text-xs text-blue-600 font-semibold">TOTAL DAYS</p>
          <p className="text-2xl font-bold text-blue-900">{data.totalDays}</p>
        </div>
        <div className="p-4 bg-green-50 border border-green-200 rounded-lg text-center">
          <p className="text-xs text-green-600 font-semibold">PRESENT</p>
          <p className="text-2xl font-bold text-green-900">{data.presentDays}</p>
        </div>
        <div className="p-4 bg-yellow-50 border border-yellow-200 rounded-lg text-center">
          <p className="text-xs text-yellow-600 font-semibold">PENDING</p>
          <p className="text-2xl font-bold text-yellow-900">{data.pendingDays}</p>
        </div>
        <div className="p-4 bg-purple-50 border border-purple-200 rounded-lg text-center">
          <p className="text-xs text-purple-600 font-semibold">ATTENDANCE %</p>
          <p className="text-2xl font-bold text-purple-900">{data.attendancePercentage}%</p>
        </div>
      </div>

      {/* History Table */}
      <div className="overflow-x-auto border border-slate-200 rounded-lg bg-white shadow-sm">
        <table className="w-full text-left text-sm text-slate-600">
          <thead className="bg-slate-100 text-xs font-semibold text-slate-700 uppercase">
            <tr>
              <th className="p-3">Date</th>
              <th className="p-3">Time</th>
              <th className="p-3">Status</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-200">
            {data.records.map((rec, index) => (
              <tr key={rec.attendanceId || rec.id || index} className="hover:bg-slate-50">
                <td className="p-3">{rec.date}</td>
                <td className="p-3">{rec.timeMarked || rec.time || 'N/A'}</td>
                <td className="p-3 font-semibold">
                  <span className={`px-2.5 py-1 rounded text-xs font-bold ${
                    rec.status === 'VERIFIED' || rec.status === 'APPROVED' ? 'bg-green-100 text-green-800' :
                    rec.status === 'REJECTED' ? 'bg-red-100 text-red-800' :
                    'bg-yellow-100 text-yellow-800'
                  }`}>
                    {rec.status}
                  </span>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default AttendanceHistory;
