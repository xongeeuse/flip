export const AMRLegend = () => {
  return (
    <div className="absolute bottom-4 right-4 z-20 p-3 bg-black/20 rounded-lg min-w-[180px]">
      <span className="font-bold text-sm text-white mb-2 block text-center">AMR 상태</span>
      <div className="grid grid-cols-2 gap-2">
        <div className="flex items-center justify-center gap-2">
          <div className="w-3 h-3 rounded-sm bg-[#2196F3]" />
          <span className="text-xs text-white">작업중</span>
        </div>
        <div className="flex items-center justify-center gap-2">
          <div className="w-3 h-3 rounded-sm bg-[#4CAF50]" />
          <span className="text-xs text-white">대기</span>
        </div>
        <div className="flex items-center justify-center gap-2">
          <div className="w-3 h-3 rounded-sm bg-[#FFC107]" />
          <span className="text-xs text-white">충전중</span>
        </div>
        <div className="flex items-center justify-center gap-2">
          <div className="w-3 h-3 rounded-sm bg-[#F44336]" />
          <span className="text-xs text-white">에러</span>
        </div>
      </div>
    </div>
  );
}; 