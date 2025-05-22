import { FacilityCard } from './FacilityCard';
import { useLineStore } from '@/shared/store/lineStore';

export default function FacilityControl() {
  const { lines } = useLineStore();

  return (
    <div className='flex flex-col grow p-2 bg-[#0B1120]'>
      {lines && lines.length > 0 ? (
        <div className='flex flex-col h-full mt-2 overflow-y-auto hide-scrollbar'>
          {lines.map((line) => (
            <FacilityCard key={line.lineId} data={line} />
          ))}
        </div>
      ) : (
        <div className='flex min-w-[320px] w-full grow p-4 items-center justify-center h-full'>
          <span className='text-lg text-white'>설비 준비 중...</span>
        </div>
      )}
    </div>
  );
}
