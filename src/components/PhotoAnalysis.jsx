import React, { useState, useRef, useEffect } from 'react';

export const PhotoAnalysis = ({ token: propToken, onClose }) => {
  const [selectedImage, setSelectedImage] = useState(null);
  const [loading, setLoading] = useState(false);
  const [analysisResult, setAnalysisResult] = useState(null);
  const [error, setError] = useState(null);
  const [activeTab, setActiveTab] = useState('mesh'); // 'mesh' | 'zones'
  const [history, setHistory] = useState([]);
  const [showHistory, setShowHistory] = useState(false);
  const [loadingHistory, setLoadingHistory] = useState(false);
  const [routineTab, setRoutineTab] = useState('am'); // 'am' | 'pm'

  const fileInputRef = useRef(null);
  const canvasRef = useRef(null);

  const fetchHistory = async () => {
    setLoadingHistory(true);
    try {
      const token = propToken || localStorage.getItem('jwt_token') || localStorage.getItem('token');
      const baseUrl = typeof API_BASE_URL !== 'undefined' ? API_BASE_URL : '/api/v1';

      const res = await fetch(`${baseUrl}/skin/history`, {
        headers: token ? { Authorization: `Bearer ${token}` } : {}
      });
      if (res.ok) {
        const data = await res.json();
        setHistory(data);
      }
    } catch (err) {
      console.error("Failed to load skin analysis history", err);
    } finally {
      setLoadingHistory(false);
    }
  };

  useEffect(() => {
    fetchHistory();
  }, []);

  const handleFileChange = (e) => {
    const file = e.target.files?.[0];
    if (file) {
      processPhoto(file);
    }
  };

  const processPhoto = async (file) => {
    setLoading(true);
    setError(null);
    setAnalysisResult(null);

    const imageUrl = URL.createObjectURL(file);
    setSelectedImage(imageUrl);

    const formData = new FormData();
    formData.append("file", file);

    try {
      const token = propToken || localStorage.getItem('jwt_token') || localStorage.getItem('token');
      const baseUrl = typeof API_BASE_URL !== 'undefined'
        ? API_BASE_URL
        : '/api/v1';

      // Submit photo to Spring Boot proxy endpoint
      const response = await fetch(`${baseUrl}/skin/analyze-photo`, {
        method: "POST",
        headers: token ? { Authorization: `Bearer ${token}` } : {},
        body: formData,
      });

      if (!response.ok) {
        const errText = await response.text();
        throw new Error(errText || `Server responded with ${response.status}`);
      }

      const data = await response.json();

      if (!data.success) {
        setError(data.message || "Face detection failed. Ensure face is directly facing camera.");
      } else {
        setAnalysisResult(data);
        renderOverlay(imageUrl, data);
        fetchHistory(); // Refresh history list after successful persistence
      }
    } catch (err) {
      console.error("Skin analysis error:", err);
      setError(err.message || "Failed to connect to the skin diagnostic service.");
    } finally {
      setLoading(false);
    }
  };

  const renderOverlay = (imageSrc, data) => {
    const canvas = canvasRef.current;
    if (!canvas) return;

    const ctx = canvas.getContext('2d');
    const img = new Image();
    img.src = imageSrc;

    img.onload = () => {
      canvas.width = img.width;
      canvas.height = img.height;
      if (!ctx) return;

      ctx.clearRect(0, 0, canvas.width, canvas.height);

      if (activeTab === 'mesh' && data.landmarks) {
        // Draw facial mesh nodes
        ctx.fillStyle = "rgba(6, 182, 212, 0.75)";
        data.landmarks.forEach((lm) => {
          const x = lm.x * canvas.width;
          const y = lm.y * canvas.height;
          ctx.beginPath();
          ctx.arc(x, y, 2.5, 0, 2 * Math.PI);
          ctx.fill();
        });

        // Draw callout nodes with indicator rings
        if (data.callout_nodes) {
          ctx.fillStyle = "#f43f5e";
          ctx.strokeStyle = "rgba(255, 255, 255, 0.9)";
          ctx.lineWidth = 2;
          data.callout_nodes.forEach((callout) => {
            const lm = data.landmarks[callout.landmark_index];
            if (lm) {
              const cx = lm.x * canvas.width;
              const cy = lm.y * canvas.height;
              ctx.beginPath();
              ctx.arc(cx, cy, 6, 0, 2 * Math.PI);
              ctx.fill();
              ctx.stroke();
            }
          });
        }
      } else if (activeTab === 'zones' && data.bounding_boxes) {
        const colors = {
          forehead: "#38bdf8",
          left_cheek: "#4ade80",
          right_cheek: "#a78bfa",
          nose: "#facc15",
          under_eye_left: "#f43f5e",
          under_eye_right: "#fb7185",
          chin: "#fb923c"
        };

        ctx.lineWidth = 3;
        Object.entries(data.bounding_boxes).forEach(([name, box]) => {
          ctx.strokeStyle = colors[name] || "#38bdf8";
          ctx.fillStyle = (colors[name] || "#38bdf8") + "22";
          ctx.fillRect(box.x, box.y, box.width, box.height);
          ctx.strokeRect(box.x, box.y, box.width, box.height);

          ctx.fillStyle = colors[name] || "#38bdf8";
          ctx.font = "bold 14px sans-serif";
          ctx.fillText(name.replace(/_/g, ' ').toUpperCase(), box.x + 4, Math.max(16, box.y - 6));
        });
      }
    };
  };

  const getStatusBadge = (status) => {
    switch (status) {
      case 'Excellent':
        return 'bg-emerald-500/15 text-emerald-400 border-emerald-500/30';
      case 'Good':
        return 'bg-cyan-500/15 text-cyan-400 border-cyan-500/30';
      case 'Fair':
        return 'bg-amber-500/15 text-amber-400 border-amber-500/30';
      default:
        return 'bg-rose-500/15 text-rose-400 border-rose-500/30';
    }
  };

  return (
    <div className="rounded-2xl border border-slate-800 bg-slate-950 p-6 text-slate-100 shadow-2xl">
      <div className="flex flex-wrap items-center justify-between gap-4 border-b border-slate-800 pb-5">
        <div>
          <h2 className="text-xl font-bold tracking-tight text-cyan-400 flex items-center gap-2">
            <span className="inline-block w-2.5 h-2.5 rounded-full bg-cyan-400 animate-pulse"></span>
            Facial Dermatological Analysis
          </h2>
          <p className="text-xs text-slate-400 mt-0.5">
            Static photo diagnostics via MediaPipe 468-point mesh, GLCM texture analysis, and CIE L*a*b* colorimetry.
          </p>
        </div>

        <div className="flex items-center gap-2">
          <button
            onClick={() => setShowHistory(!showHistory)}
            className={`rounded-lg px-3 py-1.5 text-xs transition border ${
              showHistory
                ? 'bg-cyan-500/20 text-cyan-300 border-cyan-500/40'
                : 'bg-slate-900 text-slate-300 border-slate-700 hover:bg-slate-800'
            }`}
          >
            📊 {showHistory ? 'Hide History' : `Scan History (${history.length})`}
          </button>

          {onClose && (
            <button
              onClick={onClose}
              className="rounded-lg bg-slate-800 px-3 py-1.5 text-xs text-slate-300 hover:bg-slate-700"
            >
              Close
            </button>
          )}
        </div>
      </div>

      {/* HISTORY TABLE / DRAWER */}
      {showHistory && (
        <div className="mt-4 rounded-xl border border-slate-800 bg-slate-900/80 p-4">
          <h3 className="text-xs font-semibold text-slate-300 uppercase tracking-wider mb-2">
            Historical Scan Records
          </h3>
          {loadingHistory ? (
            <p className="text-xs text-slate-400">Loading scan history...</p>
          ) : history.length === 0 ? (
            <p className="text-xs text-slate-500">No past scan records found for this account.</p>
          ) : (
            <div className="overflow-x-auto max-h-48">
              <table className="w-full text-left text-xs text-slate-300">
                <thead className="border-b border-slate-800 text-[11px] uppercase text-slate-400">
                  <tr>
                    <th className="py-1.5 pr-2">Date</th>
                    <th className="py-1.5 px-2">Acne</th>
                    <th className="py-1.5 px-2">Pores</th>
                    <th className="py-1.5 px-2">Texture</th>
                    <th className="py-1.5 px-2">Dark Circles</th>
                    <th className="py-1.5 px-2">Pigment</th>
                    <th className="py-1.5 px-2">Hydration</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-800/50">
                  {history.map((rec) => (
                    <tr key={rec.id} className="hover:bg-slate-800/40">
                      <td className="py-1.5 pr-2 text-slate-400">
                        {rec.createdAt ? new Date(rec.createdAt).toLocaleDateString() : 'N/A'}
                      </td>
                      <td className="py-1.5 px-2 font-mono text-cyan-400">{rec.acneScore ?? '-'}</td>
                      <td className="py-1.5 px-2 font-mono text-cyan-400">{rec.poreScore ?? '-'}</td>
                      <td className="py-1.5 px-2 font-mono text-cyan-400">{rec.textureScore ?? '-'}</td>
                      <td className="py-1.5 px-2 font-mono text-cyan-400">{rec.darkCircleScore ?? '-'}</td>
                      <td className="py-1.5 px-2 font-mono text-cyan-400">{rec.pigmentationScore ?? '-'}</td>
                      <td className="py-1.5 px-2 font-mono text-cyan-400">{rec.hydrationScore ?? '-'}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 mt-6">
        {/* LEFT / CENTER: Upload & Preview Stage */}
        <div className="lg:col-span-6 flex flex-col items-center justify-center rounded-xl border border-slate-800 bg-slate-900/60 p-4 min-h-[420px] relative">
          {selectedImage ? (
            <div className="relative w-full flex flex-col items-center">
              <div className="relative inline-block max-w-full overflow-hidden rounded-xl border border-slate-700 shadow-inner">
                <img
                  src={selectedImage}
                  alt="Uploaded face diagnostic target"
                  className="max-h-[380px] w-auto object-contain block"
                />
                <canvas
                  ref={canvasRef}
                  className="absolute inset-0 w-full h-full pointer-events-none"
                />
              </div>

              {analysisResult && (
                <div className="mt-3 flex items-center gap-2 bg-slate-950/80 px-3 py-1.5 rounded-lg border border-slate-800 text-xs">
                  <span className="text-slate-400">View Overlay:</span>
                  <button
                    onClick={() => {
                      setActiveTab('mesh');
                      renderOverlay(selectedImage, analysisResult);
                    }}
                    className={`px-2.5 py-1 rounded transition ${activeTab === 'mesh' ? 'bg-cyan-500/20 text-cyan-300 font-semibold' : 'text-slate-400 hover:text-white'}`}
                  >
                    Mesh Landmarks
                  </button>
                  <button
                    onClick={() => {
                      setActiveTab('zones');
                      renderOverlay(selectedImage, analysisResult);
                    }}
                    className={`px-2.5 py-1 rounded transition ${activeTab === 'zones' ? 'bg-cyan-500/20 text-cyan-300 font-semibold' : 'text-slate-400 hover:text-white'}`}
                  >
                    Diagnostic Zones
                  </button>
                </div>
              )}
            </div>
          ) : (
            <div
              onClick={() => fileInputRef.current?.click()}
              className="w-full flex-1 flex flex-col items-center justify-center border-2 border-dashed border-slate-700 hover:border-cyan-500/70 rounded-xl p-8 text-center cursor-pointer transition-all hover:bg-slate-800/30"
            >
              <div className="w-14 h-14 rounded-2xl bg-cyan-500/10 border border-cyan-500/20 flex items-center justify-center text-cyan-400 text-2xl mb-3">
                📷
              </div>
              <p className="font-medium text-slate-200 text-sm">Upload Face Diagnostic Photo</p>
              <p className="text-xs text-slate-500 mt-1 max-w-xs">
                Supports JPG, PNG, or WEBP. Ensure face is well-lit and directly facing camera.
              </p>
              <span className="mt-4 inline-block bg-cyan-500 text-slate-950 font-semibold px-4 py-1.5 rounded-lg text-xs shadow hover:bg-cyan-400">
                Browse Photo
              </span>
            </div>
          )}

          <input
            type="file"
            ref={fileInputRef}
            onChange={handleFileChange}
            accept="image/*"
            className="hidden"
          />

          {selectedImage && (
            <button
              onClick={() => fileInputRef.current?.click()}
              className="mt-3 text-xs text-slate-400 hover:text-cyan-400 transition"
            >
              🔄 Choose Different Photo
            </button>
          )}
        </div>

        {/* RIGHT: Diagnostic Metrics & Scoring Panel */}
        <div className="lg:col-span-6 flex flex-col justify-between rounded-xl border border-slate-800 bg-slate-900/60 p-5">
          <div>
            <h3 className="text-sm font-semibold uppercase tracking-wider text-slate-300 mb-3 flex items-center justify-between">
              <span>Diagnostic Ratings</span>
              {analysisResult && (
                <span className="text-[11px] font-normal text-emerald-400 bg-emerald-500/10 px-2 py-0.5 rounded border border-emerald-500/20">
                  Analysis Complete & Saved
                </span>
              )}
            </h3>

            {loading && (
              <div className="flex flex-col items-center justify-center py-20 space-y-3">
                <div className="w-10 h-10 border-4 border-cyan-400/30 border-t-cyan-400 rounded-full animate-spin"></div>
                <p className="text-xs text-cyan-300 font-medium">Extracting MediaPipe mesh & calculating GLCM...</p>
              </div>
            )}

            {error && (
              <div className="rounded-xl border border-rose-500/30 bg-rose-500/10 p-4 text-xs text-rose-300">
                <p className="font-semibold mb-1">Diagnostic Failed</p>
                <p>{error}</p>
              </div>
            )}

            {analysisResult && !loading && (
              <div className="space-y-4">
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                  {Object.entries(analysisResult.analysis).map(([key, data]) => (
                    <div
                      key={key}
                      className="rounded-xl border border-slate-800 bg-slate-950/80 p-3.5 flex flex-col justify-between shadow-sm hover:border-slate-700 transition"
                    >
                      <div>
                        <div className="flex items-center justify-between">
                          <span className="text-xs font-semibold text-slate-300 capitalize">
                            {key.replace(/_/g, ' ')}
                          </span>
                          <span className={`text-[10px] px-2 py-0.5 rounded-full border font-medium ${getStatusBadge(data.status)}`}>
                            {data.status}
                          </span>
                        </div>
                        <p className="text-[10px] text-slate-500 mt-1 line-clamp-1" title={data.details}>
                          {data.details}
                        </p>
                      </div>

                      <div className="mt-3 flex items-baseline justify-between border-t border-slate-800/80 pt-2">
                        <span className="text-xs text-slate-400">Score</span>
                        <span className="text-lg font-bold text-cyan-400">
                          {data.score} <span className="text-xs font-normal text-slate-500">/ 10</span>
                        </span>
                      </div>
                    </div>
                  ))}
                </div>

                {/* PERSONALIZED SKINCARE ROUTINE RECOMMENDATION ENGINE */}
                {analysisResult.routine && (
                  <div className="mt-4 rounded-xl border border-cyan-500/20 bg-slate-950/90 p-4 shadow-lg">
                    <div className="flex items-center justify-between border-b border-slate-800 pb-3">
                      <div>
                        <h4 className="text-xs font-bold uppercase tracking-wider text-cyan-400 flex items-center gap-1.5">
                          <span>✨</span> Personalized Skincare Protocol
                        </h4>
                        <p className="text-[11px] text-slate-400 mt-0.5">Formulated based on detected tissue biomarkers</p>
                      </div>
                      <div className="flex gap-1.5 bg-slate-900 p-1 rounded-lg border border-slate-800 text-[11px]">
                        <button
                          onClick={() => setRoutineTab('am')}
                          className={`px-2.5 py-1 rounded transition ${routineTab === 'am' ? 'bg-cyan-500 text-slate-950 font-bold' : 'text-slate-400 hover:text-white'}`}
                        >
                          ☀️ AM Protocol
                        </button>
                        <button
                          onClick={() => setRoutineTab('pm')}
                          className={`px-2.5 py-1 rounded transition ${routineTab === 'pm' ? 'bg-cyan-500 text-slate-950 font-bold' : 'text-slate-400 hover:text-white'}`}
                        >
                          🌙 PM Protocol
                        </button>
                      </div>
                    </div>

                    {/* ROUTINE STEPS */}
                    <div className="mt-3 space-y-2">
                      {(routineTab === 'am' ? analysisResult.routine.am_routine : analysisResult.routine.pm_routine)?.map((step, idx) => (
                        <div key={idx} className="flex items-start gap-3 rounded-lg border border-slate-800/80 bg-slate-900/60 p-2.5">
                          <span className="flex-shrink-0 w-5 h-5 rounded-full bg-cyan-500/15 text-cyan-400 border border-cyan-500/30 flex items-center justify-center text-[10px] font-bold">
                            {idx + 1}
                          </span>
                          <div className="flex-1 min-w-0">
                            <div className="flex items-center justify-between">
                              <span className="text-[11px] font-bold text-slate-200">{step.step}: {step.product_type}</span>
                            </div>
                            <p className="text-[10px] text-slate-400 mt-0.5 leading-relaxed">{step.instruction}</p>
                          </div>
                        </div>
                      ))}
                    </div>

                    {/* KEY ACTIVE INGREDIENTS BADGES */}
                    {analysisResult.routine.key_active_ingredients?.length > 0 && (
                      <div className="mt-3 pt-2.5 border-t border-slate-800/80">
                        <span className="text-[10px] uppercase font-semibold text-slate-400 block mb-1.5">Prescribed Key Actives:</span>
                        <div className="flex flex-wrap gap-1.5">
                          {analysisResult.routine.key_active_ingredients.map((act, i) => (
                            <span key={i} className="text-[10px] bg-cyan-500/10 text-cyan-300 border border-cyan-500/25 px-2 py-0.5 rounded-full font-medium">
                              {act}
                            </span>
                          ))}
                        </div>
                      </div>
                    )}
                  </div>
                )}
              </div>
            )}

            {!selectedImage && !loading && (
              <div className="text-center py-20 text-slate-500 text-xs">
                Upload a clear face photo on the left to generate clinical diagnostic scores and routine.
              </div>
            )}
          </div>

          {analysisResult && (
            <div className="mt-4 pt-3 border-t border-slate-800/80 text-[11px] text-slate-500 flex justify-between items-center">
              <span>Image size: {analysisResult.image_dimensions?.width}×{analysisResult.image_dimensions?.height}px</span>
              <span>Zones evaluated: 7 regions</span>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default PhotoAnalysis;
