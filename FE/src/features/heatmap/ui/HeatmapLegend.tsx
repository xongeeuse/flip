import { useEffect, useRef } from 'react';

export const HeatmapLegend = () => {
  const canvasRef = useRef<HTMLCanvasElement>(null);

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    // 빨간색 투명도 그라데이션 (왼쪽: 투명, 오른쪽: 불투명)
    const grad = ctx.createLinearGradient(0, 0, canvas.width, 0);
    grad.addColorStop(0, 'rgba(255,0,0,0)');
    grad.addColorStop(1, 'rgba(255,0,0,1)');
    ctx.fillStyle = grad;
    ctx.fillRect(0, 0, canvas.width, canvas.height);
  }, []);

  return (
    <div className="absolute bottom-4 left-4 z-20 p-3 flex flex-col items-center bg-black/20 rounded-lg min-w-[180px]">
      <span className="font-bold text-sm text-white mb-1">빈도</span>
      <canvas
        ref={canvasRef}
        width={140}
        height={10}
        className="rounded"
        style={{ display: 'block' }}
      />
      <div className="flex justify-between w-full text-xs mt-1 px-1 text-white">
        <span>적음</span>
        <span>많음</span>
      </div>
    </div>
  );
}; 