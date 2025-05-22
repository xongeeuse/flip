import { useQuery } from '@tanstack/react-query';

const API_BASE_URL = process.env.NEXT_PUBLIC_BACKEND_SERVER;

export const useFetchDashboardStats = () => {
  return useQuery({
    queryKey: ['dashboardStats'],
    queryFn: async () => {
      const response = await fetch(`${API_BASE_URL}/api/v1/status/factory`);
      const statsData = await response.json();
      return [
        {
          title: 'AMR 운행 현황',
          value: `${statsData.amrWorking}대`,
          subtext: [`/ 총 ${statsData.amrMaxNum}대`],
          data: {
            current: statsData.amrWorking,
            total: statsData.amrMaxNum,
            status: [
              { label: '운행 중', value: statsData.amrWorking, total: statsData.amrMaxNum },
              { label: '대기 중', value: statsData.amrWaiting, total: statsData.amrMaxNum },
              { label: '충전 중', value: statsData.amrCharging, total: statsData.amrMaxNum },
              { label: '에러', value: statsData.amrError, total: statsData.amrMaxNum },
            ],
          },
        },
        {
          title: 'AMR 평균 운행률',
          value: `${Math.round((statsData.amrWorkTime / 360) * 100)}%`,
          subtext: [
            `${Math.floor(statsData.amrWorkTime / 60)}h ${statsData.amrWorkTime % 60}m / 6h`,
          ],
          data: {
            current: (statsData.amrWorkTime / 360) * 100,
            total: 100,
            status: [
              {
                label: '운행 시간',
                value: (statsData.amrWorkTime / 360) * 100,
                isTime: true,
                timeValue: statsData.amrWorkTime,
              },
              {
                label: '미운행 시간',
                value: 100 - (statsData.amrWorkTime / 360) * 100,
                isTime: true,
                timeValue: 360 - statsData.amrWorkTime,
              },
            ],
          },
        },
        {
          title: '자재 보유 현황',
          value: `${Math.floor((statsData.storageQuantity / statsData.storageMaxQuantity) * 100)}%`,
          subtext: [
            `${statsData.storageQuantity.toLocaleString()}개 / ${statsData.storageMaxQuantity.toLocaleString()}개`,
          ],
          data: {
            current: statsData.storageQuantity,
            total: statsData.storageMaxQuantity,
            status: [
              {
                label: '보유량',
                value: statsData.storageQuantity,
                storageTotal: statsData.storageMaxQuantity,
              },
              {
                label: '잔여 수용량',
                value: statsData.storageMaxQuantity - statsData.storageQuantity,
                storageTotal: statsData.storageMaxQuantity,
              },
            ],
          },
        },
        {
          title: '설비 가동 상태',
          value: `${statsData.lineWorking}개`,
          subtext: [`/총 ${statsData.lineMaxNum}개`],
          data: {
            current: statsData.lineWorking,
            total: statsData.lineMaxNum,
            status: [
              { label: '가동 중', value: statsData.lineWorking, total: statsData.lineMaxNum },
              {
                label: '미가동',
                value: statsData.lineMaxNum - statsData.lineWorking,
                total: statsData.lineMaxNum,
              },
            ],
          },
        },
      ];
    },
    refetchInterval: 5000,
  });
};

export const useFetchDashboardChart = () => {
  return useQuery({
    queryKey: ['dashboardChart'],
    queryFn: async () => {
      const response = await fetch(`${API_BASE_URL}/api/v1/status/production`);
      const result = await response.json();
      // 바로 차트용 객체 배열로 가공
      return result.data.map(
        (entry: { timestamp: number; production: number; target: number }) => ({
          name: `${entry.timestamp.toString().padStart(2, '0')}시`,
          production: entry.production,
          target: entry.target,
        }),
      );
    },
    refetchInterval: 5000,
  });
};
