'use client';

import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from 'recharts';
import { useFetchDashboardChart } from '@/features/dashboard/api/dashboard';

export default function OverviewChart() {
  const { data: chartData, isLoading } = useFetchDashboardChart();

  if (isLoading) return <div className='text-white'>차트 로딩 중...</div>;
  if (!chartData) return <div className='text-white'>차트 데이터 없음</div>;

  return (
    <div className='flex flex-col h-[460px] p-6 bg-[#020817]/50 backdrop-blur-md rounded-xl border border-blue-900/20'>
      <div className='flex items-center gap-4 mb-6'>
        <h2 className='text-lg font-semibold text-white'>생산 현황</h2>
      </div>
      <div className='flex-1 w-full min-h-0'>
        <ResponsiveContainer width='100%' height='100%'>
          <LineChart
            data={chartData}
            margin={{
              top: 5,
              right: 30,
              left: 20,
              bottom: 5,
            }}
          >
            <CartesianGrid strokeDasharray='3 3' stroke='#1F2937' />
            <XAxis dataKey='name' stroke='#6B7280' tick={{ fill: '#6B7280' }} />
            <YAxis stroke='#6B7280' tick={{ fill: '#6B7280' }} />
            <Tooltip
              contentStyle={{
                backgroundColor: '#1F2937',
                border: 'none',
                borderRadius: '0.5rem',
                color: '#F3F4F6',
              }}
            />
            <Legend
              wrapperStyle={{
                color: '#6B7280',
              }}
            />
            <Line
              type='monotone'
              dataKey='production'
              stroke='#00B7E3'
              name='생산량'
              strokeWidth={2}
              dot={{ fill: '#00B7E3', r: 4 }}
              activeDot={{ r: 6 }}
            />
            <Line
              type='monotone'
              dataKey='target'
              stroke='#FF5252'
              name='목표량'
              strokeWidth={2}
              dot={{ fill: '#FF5252', r: 4 }}
              activeDot={{ r: 6 }}
            />
          </LineChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
}
