export function ThumbnailAMRLegend() {
  return (
    <div className="flex gap-4 bg-black/30 rounded-lg p-1">
      <div className="flex items-center gap-2">
        <span className="w-3 h-3 rounded-full bg-[#2196F3]" />
        <span className="text-xs text-white">작업중</span>
      </div>
      <div className="flex items-center gap-2">
        <span className="w-3 h-3 rounded-full bg-[#4CAF50]" />
        <span className="text-xs text-white">대기</span>
      </div>
      <div className="flex items-center gap-2">
        <span className="w-3 h-3 rounded-full bg-[#FFC107]" />
        <span className="text-xs text-white">충전중</span>
      </div>
      <div className="flex items-center gap-2">
        <span className="w-3 h-3 rounded-full bg-[#F44336]" />
        <span className="text-xs text-white">에러</span>
      </div>
    </div>
  );
} 