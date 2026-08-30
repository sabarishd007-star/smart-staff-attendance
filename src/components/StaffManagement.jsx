import React, { useEffect, useState } from 'react';

const StaffManagement = ({ token: propToken }) => {
  const [staffList, setStaffList] = useState([]);
  const [loading, setLoading] = useState(true);
  const [resetModalId, setResetModalId] = useState(null);
  const [newPassword, setNewPassword] = useState('');
  const [resetFeedback, setResetFeedback] = useState('');

  useEffect(() => {
    fetchStaff();
  }, []);

  const fetchStaff = async () => {
    try {
      const token = propToken || localStorage.getItem('jwt_token') || localStorage.getItem('token');
      const baseUrl = typeof API_BASE_URL !== 'undefined'
        ? API_BASE_URL
        : 'https://vitalscan-api-y891.onrender.com/api/v1';

      const res = await fetch(`${baseUrl}/admin/staff`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      if (res.ok) {
        const data = await res.json();
        setStaffList(data);
      }
    } catch (err) {
      console.error("Failed to fetch staff list", err);
    } finally {
      setLoading(false);
    }
  };

  const handleToggleStatus = async (id) => {
    try {
      const token = propToken || localStorage.getItem('jwt_token') || localStorage.getItem('token');
      const baseUrl = typeof API_BASE_URL !== 'undefined'
        ? API_BASE_URL
        : 'https://vitalscan-api-y891.onrender.com/api/v1';

      const res = await fetch(`${baseUrl}/admin/staff/${id}/toggle-status`, {
        method: 'PATCH',
        headers: { Authorization: `Bearer ${token}` }
      });
      if (res.ok) fetchStaff();
    } catch (err) {
      alert("Failed to change staff status.");
    }
  };

  const handleResetPasswordSubmit = async (e) => {
    e.preventDefault();
    if (!newPassword.trim()) return;
    try {
      const token = propToken || localStorage.getItem('jwt_token') || localStorage.getItem('token');
      const baseUrl = typeof API_BASE_URL !== 'undefined'
        ? API_BASE_URL
        : 'https://vitalscan-api-y891.onrender.com/api/v1';

      const res = await fetch(`${baseUrl}/admin/staff/${resetModalId}/reset-password`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`
        },
        body: JSON.stringify({ newPassword: newPassword.trim() })
      });
      if (res.ok) {
        setResetFeedback('Password reset successfully!');
        setTimeout(() => {
          setResetModalId(null);
          setNewPassword('');
          setResetFeedback('');
        }, 1200);
      } else {
        const err = await res.json().catch(() => ({}));
        setResetFeedback(err.message || 'Failed to reset password.');
      }
    } catch (err) {
      setResetFeedback('Failed to reset password.');
    }
  };

  if (loading) return <div className="p-4 text-center text-xs text-slate-500 font-medium">Loading staff records...</div>;

  return (
    <div className="p-4 space-y-4 bg-white rounded-xl border border-slate-200 shadow-sm">
      <div className="flex justify-between items-center">
        <div>
          <h2 className="text-lg font-bold text-slate-800">Staff Management</h2>
          <p className="text-xs text-slate-500">Manage employee accounts, active status, and credentials</p>
        </div>
        <button onClick={fetchStaff} className="text-xs font-semibold text-indigo-600 hover:text-indigo-800">
          Refresh List
        </button>
      </div>

      <div className="overflow-x-auto border border-slate-200 rounded-lg">
        <table className="w-full text-left text-xs text-slate-600">
          <thead className="bg-slate-100 text-[10px] font-bold text-slate-700 uppercase tracking-wider">
            <tr>
              <th className="p-3">ID</th>
              <th className="p-3">Name</th>
              <th className="p-3">Email</th>
              <th className="p-3">Department</th>
              <th className="p-3">Status</th>
              <th className="p-3">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-200 bg-white">
            {staffList.length === 0 ? (
              <tr>
                <td colSpan="6" className="p-4 text-center text-slate-400">No staff members found.</td>
              </tr>
            ) : (
              staffList.map((s) => (
                <tr key={s.id} className="hover:bg-slate-50">
                  <td className="p-3 font-mono text-slate-500">#{s.id}</td>
                  <td className="p-3 font-medium text-slate-900">{s.fullName || s.name}</td>
                  <td className="p-3 text-slate-600">{s.email}</td>
                  <td className="p-3 text-slate-600">{s.department || 'Unassigned'}</td>
                  <td className="p-3">
                    <span className={`px-2 py-0.5 rounded text-[10px] font-bold tracking-wide ${
                      s.active !== false ? 'bg-emerald-100 text-emerald-800' : 'bg-rose-100 text-rose-800'
                    }`}>
                      {s.active !== false ? 'Active' : 'Deactivated'}
                    </span>
                  </td>
                  <td className="p-3 space-x-2">
                    <button 
                      onClick={() => handleToggleStatus(s.id)}
                      className={`text-[10px] font-bold px-2.5 py-1 rounded transition ${
                        s.active !== false
                          ? 'bg-rose-50 text-rose-700 hover:bg-rose-100 border border-rose-200'
                          : 'bg-emerald-50 text-emerald-700 hover:bg-emerald-100 border border-emerald-200'
                      }`}>
                      {s.active !== false ? 'Deactivate' : 'Activate'}
                    </button>
                    <button
                      onClick={() => setResetModalId(s.id)}
                      className="text-[10px] font-bold bg-slate-100 text-slate-700 hover:bg-slate-200 px-2.5 py-1 rounded border border-slate-200 transition">
                      Reset Password
                    </button>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {/* Password Reset Modal */}
      {resetModalId && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm p-4">
          <div className="bg-white rounded-xl shadow-xl border border-slate-200 p-6 w-full max-w-sm">
            <h3 className="text-sm font-bold text-slate-800 mb-2">Reset Password (Staff #{resetModalId})</h3>
            {resetFeedback && <p className="text-xs font-semibold text-indigo-600 mb-2">{resetFeedback}</p>}
            <form onSubmit={handleResetPasswordSubmit} className="space-y-3">
              <input
                type="password"
                required
                placeholder="Enter new password"
                value={newPassword}
                onChange={(e) => setNewPassword(e.target.value)}
                className="w-full rounded border border-slate-300 px-3 py-2 text-xs outline-none focus:border-indigo-500"
              />
              <div className="flex justify-end gap-2">
                <button
                  type="button"
                  onClick={() => { setResetModalId(null); setNewPassword(''); setResetFeedback(''); }}
                  className="px-3 py-1.5 rounded text-xs font-semibold text-slate-600 hover:bg-slate-100">
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-3 py-1.5 rounded text-xs font-bold bg-indigo-600 text-white hover:bg-indigo-700">
                  Save New Password
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default StaffManagement;
