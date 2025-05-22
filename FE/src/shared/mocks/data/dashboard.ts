export const mockDashboardData = {
  stats: [
    {
      title: 'AMR 가동 갯수',
      value: '16대',
      subtext: ['총 20대'],
      data: {
        current: 16,
        total: 20,
        status: [
          { label: '가동 중', value: 12 },
          { label: '충전 중', value: 2 },
          { label: '대기 중', value: 2 },
          { label: '정비 중', value: 4 },
        ],
      },
    },
    {
      title: 'AMR 평균 가동률',
      value: '81.25%',
      subtext: ['목표: 85%'],
      data: {
        current: 81.25,
        total: 100,
        timeData: [
          { time: '1시', value: 75 },
          { time: '2시', value: 78 },
          { time: '3시', value: 80 },
          { time: '4시', value: 85 },
          { time: '5시', value: 82 },
          { time: '6시', value: 81.25 },
        ],
      },
    },
    {
      title: '창고 내 자재 상태',
      value: '75%',
      subtext: ['적재 가능: 25%'],
      data: {
        current: 75,
        total: 100,
        status: [
          { label: '적재됨', value: 75 },
          { label: '가용공간', value: 25 },
        ],
      },
    },
    {
      title: '설비 가동 상태',
      value: '16대',
      subtext: ['총 20대'],
      data: {
        current: 16,
        total: 20,
        status: [
          { label: '정상', value: 14 },
          { label: '점검필요', value: 2 },
        ],
      },
    },
  ],
  chartData: {
    labels: ['1시', '2시', '3시', '4시', '5시', '6시'],
    datasets: [
      {
        label: '생산량',
        data: [12, 15, 18, 14, 16, 13],
        borderColor: 'rgb(75, 192, 192)',
        tension: 0.1,
      },
      {
        label: '목표량',
        data: [15, 15, 15, 15, 15, 15],
        borderColor: 'rgb(255, 99, 132)',
        tension: 0.1,
      },
    ],
  },
};
