'use client';

import React, { useEffect, useState } from 'react';
import { StatsCard, OverviewChart } from '@/features/dashboard';
import { useFetchDashboardStats } from '@/features/dashboard/api/dashboard';
import { ThumbnailViewer } from '@/features/viewer';
import { ThumbnailAMRLegend } from '@/features/viewer/ui/ThumbnailAMRLegend';
import Link from 'next/link';
import { Maximize2 } from 'lucide-react';

export default function Home() {
  const { data: cardData, isLoading, error, dataUpdatedAt } = useFetchDashboardStats();
  const [formattedDate, setFormattedDate] = useState('');
  const [formattedTime, setFormattedTime] = useState('');

  useEffect(() => {
    const now = new Date();
    setFormattedDate(
      now.toLocaleDateString('ko-KR', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        weekday: 'long',
      }),
    );
    setFormattedTime(
      now.toLocaleTimeString('ko-KR', {
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit',
        hour12: false,
      }),
    );
  }, [dataUpdatedAt]);

  if (isLoading)
    return <div className='flex items-center justify-center text-white h-dvh'>로딩 중...</div>;
  if (error)
    return (
      <div className='flex items-center justify-center text-red-500 h-dvh'>
        데이터를 불러오는 중 오류가 발생했습니다.
      </div>
    );
  if (!cardData)
    return <div className='flex items-center justify-center text-white h-dvh'>데이터 없음</div>;

  return (
    <div className='h-dvh overflow-y-auto p-6 bg-gradient-to-br from-[#0B1120] via-[#1D4ED8] to-[#0B1120]'>
      <div className='flex items-center justify-between mb-8'>
        <h1 className='text-2xl font-bold text-white'>Dashboard</h1>
        <div className='text-sm text-gray-400'>
          <span>
            데이터 갱신: {formattedDate} {formattedTime}
          </span>
        </div>
      </div>
      <div className='grid grid-cols-1 gap-6 mb-6 md:grid-cols-4'>
        {cardData.map((stat, index) => (
          <StatsCard key={index} data={stat} />
        ))}
      </div>
      <div className='max-h-[450px] grid grid-cols-3 gap-6'>
        <div className='h-full col-span-2'>
          <OverviewChart />
        </div>
        <div className='h-full'>
          <div className='h-full p-6 bg-[#020817]/50 backdrop-blur-md rounded-xl border border-blue-900/20'>
            <h2 className='mb-3 text-lg font-semibold text-white'>공장 지도</h2>
            <div className='bg-[#1F2937] h-[90%] rounded-lg flex items-center justify-center relative'>
              <ThumbnailViewer />
              <div className="absolute bottom-4">
                <ThumbnailAMRLegend />
              </div>
              <Link 
                href="/control?view=2d" 
                className="absolute right-2 top-2 p-2 bg-black/30 hover:bg-black/60 text-white rounded-lg hover:scale-105"
                title="상세 보기"
              >
                <Maximize2 size={15} />
              </Link>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
