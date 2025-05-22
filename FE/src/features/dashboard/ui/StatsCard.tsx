'use client';

import { PieChart, Pie, Cell, ResponsiveContainer, Tooltip, TooltipProps } from 'recharts';
import { DashboardData } from '../model/types';

const COLORS = ['#00B7E3', '#FF5252', '#7A89D6', '#8BC34A'];

const formatTime = (minutes: number) => {
  const hours = Math.floor(minutes / 60);
  const mins = minutes % 60;
  return `${hours}h ${mins}m`;
};

const CustomTooltip = ({ active, payload }: TooltipProps<number, string>) => {
  if (active && payload && payload.length) {
    const data = payload[0].payload;
    const indexName = payload[0].name;
    let percentage = 0;

    // 카드 타입에 따른 퍼센트 계산
    if (data.total) {
      // AMR 가동 갯수, 설비 가동 상태
      percentage = (data.value / data.total) * 100;
    } else if (data.isTime) {
      // AMR 평균 가동률
      percentage = data.value;
    } else if (data.storageTotal) {
      // 창고 내 자재 상태
      percentage = (data.value / data.storageTotal) * 100;
    }

    return (
      <div className="bg-[#020817] p-2 rounded-lg border border-blue-900/20">
        <p className="text-white text-sm">{`${indexName}: ${Math.floor(percentage)}%`}</p>
      </div>
    );
  }
  return null;
};

export default function StatsCard({ data }: { data: DashboardData }) {
  const {title, value, subtext, data: {status : statusData}} = data;

  return (
    <div className='p-6 bg-[#020817]/50 backdrop-blur-md rounded-xl border border-blue-900/20'>
      <h3 className='text-lg font-semibold text-white mb-4'>{title}</h3>
      <div className='flex items-center justify-between'>
        <div className='flex-1'>
          <p className='text-4xl font-bold text-white'>{value}</p>
          <div className='mt-1'>
            {subtext.map((text, i) => (
              <p key={i} className='text-sm text-gray-400'>{text}</p>
            ))}
          </div>
        </div>
        <div className='w-30 h-30'>
          <ResponsiveContainer width='100%' height='100%'>
            <PieChart>
              <Pie
                data={statusData}
                cx='50%'
                cy='50%'
                innerRadius={30}
                outerRadius={50}
                cornerRadius={3}
                // paddingAngle={6}
                dataKey='value'
                nameKey='label'
                startAngle={180}
                endAngle={0}
                stroke="none"
              >
                {statusData && statusData.map((entry, index) => (
                  <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                ))}
              </Pie>
              <Tooltip content={<CustomTooltip />} />
            </PieChart>
          </ResponsiveContainer>
        </div>
      </div>
      {statusData && (
        <div className='grid grid-cols-2 gap-2 mt-4'>
          {statusData.map((stat, index) => {
            const displayValue = stat.isTime 
              ? formatTime(stat.timeValue || 0)  // 실제 시간 값으로 변환
              : stat.value;

            return (
              <div key={index} className='flex items-center text-xs text-gray-400'>
                <div
                  className='w-2 h-2 mr-2 rounded-full'
                  style={{ backgroundColor: COLORS[index % COLORS.length] }}
                />
                <span>{stat.label}: {displayValue}</span>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}
