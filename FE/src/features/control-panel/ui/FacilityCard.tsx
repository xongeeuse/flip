import { Card } from '@/components/ui/card';
import { FACILITY_CARD_STATUS } from '../model';
import { BadgeAlert, Wrench } from 'lucide-react';
import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from '@/components/ui/tooltip';
import { usePersonModelStore } from '@/shared/store/person-model-store';
import { toast } from 'sonner';
import { useEffect, useRef } from 'react';

interface FacilityCardProps {
  data: FACILITY_CARD_STATUS;
}

export function FacilityCard({ data }: FacilityCardProps) {
  const { lineId, amount, status } = data;
  const { setRepairing } = usePersonModelStore();
  const prevStatusRef = useRef(status);

  const getStatusColor = (status: boolean) => (status ? 'text-green-400' : 'text-red-400');
  const getStatusText = (status: boolean) => (status ? '정상' : '이상');

  const handleRepair = () => {
    setRepairing(true);
    toast.success(`${lineId}번 라인 수리를 시작합니다.`, {
      duration: 3000,
    });
  };

  useEffect(() => {
    if (prevStatusRef.current && !status) {
      toast.error(`${lineId}번 라인에 이상이 발생했습니다!`, {
        duration: 10000,
        action: {
          label: (
            <div className="flex items-center gap-1">
              <Wrench className="w-4 h-4" />
              <span>수리</span>
            </div>
          ),
          onClick: handleRepair,
        },
        actionButtonStyle: {
          background: '#ef4444',
          color: 'white',
          display: 'flex',
          alignItems: 'center',
          gap: '0.5rem',
          fontWeight: '600',
          padding: '0.5rem 1rem',
          borderRadius: '0.375rem',
        },
        style: {
          background: '#fee2e2',
          border: '2px solid #ef4444',
          color: '#991b1b',
          fontWeight: '600',
        },
      });
    }
    prevStatusRef.current = status;
  }, [status, lineId]);


  return (
    <Card className='w-full rounded-2xl bg-[#393E4B] p-6 pr-12 mb-6 flex flex-row items-center shadow-lg relative'>
      {/* 오른쪽 상단: 상태 아이콘 */}
      <div className='absolute top-4 right-4'>
        <TooltipProvider>
          <Tooltip delayDuration={0}>
            <TooltipTrigger>
              <BadgeAlert className={`size-4 ${getStatusColor(status)} cursor-pointer`} />
            </TooltipTrigger>
            <TooltipContent className='flex flex-col items-center gap-2'>
              <p>{getStatusText(status)}</p>
              {!status && (
                <button
                  className='bg-blue-500 hover:bg-blue-600 text-white px-3 py-1 rounded flex items-center gap-1 text-sm'
                  onClick={handleRepair}
                >
                  <Wrench className='w-4 h-4' />
                </button>
              )}
            </TooltipContent>
          </Tooltip>
        </TooltipProvider>
      </div>
      {/* 좌측: 라인 ID */}
      <div className='flex flex-col justify-center flex-1'>
        <span className='mb-1 text-sm text-gray-300'>라인 ID</span>
        <span className='text-3xl font-bold tracking-wider text-white'>{lineId}</span>
      </div>
      {/* 우측: 잔여 자재량 */}
      <div className='flex flex-col items-end justify-center flex-1'>
        <span className='mb-1 text-sm text-gray-300'>잔여 자재량</span>
        <span className='mb-1 text-xl font-bold text-white'>{amount}%</span>
        <div className='w-32 h-2 overflow-hidden bg-gray-600 rounded-full'>
          <div className='h-full transition-all bg-blue-400' style={{ width: `${amount}%` }} />
        </div>
      </div>
    </Card>
  );
}
