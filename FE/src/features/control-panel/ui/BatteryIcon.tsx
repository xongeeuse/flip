import { cn } from '@/lib/utils';

interface BatteryIconProps {
  percentage: number;
  className?: string;
}

export const BatteryIcon = ({ percentage, className }: BatteryIconProps) => {
  const getBatteryColor = (percentage: number) => {
    if (percentage <= 20) return 'bg-red-500';
    if (percentage <= 50) return 'bg-yellow-400';
    return 'bg-green-500';
  };

  return (
    <div className={cn('relative flex items-center', className)}>
      <div className='relative w-6 h-3 overflow-hidden bg-white border-2 border-gray-400 rounded-full'>
        <div
          className={cn(
            'h-full w-full origin-left transition-transform duration-300',
            getBatteryColor(percentage),
          )}
          style={{ transform: `scaleX(${percentage / 100})` }}
        />
      </div>
      <div className='w-1 h-2 bg-gray-400 rounded-r-full ml-[-2px]' />
    </div>
  );
};
