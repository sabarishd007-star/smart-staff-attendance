import React, { useEffect, useState } from 'react';
import { PieChart, Pie, Cell, Tooltip, Legend, ResponsiveContainer } from 'recharts';

const COLORS = ['#10B981', '#F59E0B', '#EF4444'];

const AttendanceAnalytics = ({ token: propToken }) => {
  const [chartData, setChartData] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchAnalytics();
  }, []);

  const fetchAnalytics = async () => {
    try {
      const token = propToken || localStorage.getItem('jwt_token') || localStorage.getItem('token');
      const baseUrl = typeof API_BASE_URL !== 'undefined'
        ? API_BASE_URL
        : 'https://vitalscan-api-y891.onrender.com/api/v1';

      const res = await fetch(`${baseUrl}/admin/analytics/summary`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      if (res.ok) {
        const result = await res.json();
        setChartData(result.statusBreakdown || []);
      }
    } catch (err) {
      console.error("Failed to load analytics data", err);
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <div className="p-4 text-center text-xs text-slate-500 font-medium">Loading Analytics...</div>;

  return (
    <div className="p-6 bg-white rounded-xl border border-slate-200 shadow-sm space-y-4">
      <div>
        <h2 className="text-lg font-bold text-slate-800">Attendance Analytics & Breakdown</h2>
        <p className="text-xs text-slate-500">Visual status breakdown across all staff records</p>
      </div>
      <div className="w-full h-64">
        <ResponsiveContainer width="100%" height="100%">
          <PieChart>
            <Pie
              data={chartData}
              cx="50%"
              cy="50%"
              innerRadius={60}
              outerRadius={80}
              paddingAngle={5}
              dataKey="value"
            >
              {chartData.map((entry, index) => (
                <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
              ))}
            </Pie>
            <Tooltip />
            <Legend />
          </PieChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
};

export default AttendanceAnalytics;
