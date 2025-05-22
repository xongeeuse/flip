import { http, HttpResponse } from 'msw';
import { mockDashboardData } from '../data/dashboard';

export const dashboardHandlers = [
  // 대시보드 통계 데이터
  http.get('http://localhost:3000/api/dashboard/stats', () => {
    return HttpResponse.json(mockDashboardData.stats);
  }),

  // 대시보드 차트 데이터
  http.get('http://localhost:3000/api/dashboard/chart', () => {
    return HttpResponse.json(mockDashboardData.chartData);
  }),

  // 전체 대시보드 데이터
  http.get('http://localhost:3000/api/dashboard', () => {
    return HttpResponse.json(mockDashboardData);
  }),
];
