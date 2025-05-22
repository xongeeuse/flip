export interface StatData {
  current: number;
  total: number;
  status?: { label: string; value: number; isTime?: boolean; timeValue?: number }[];
  timeData?: { time: string; value: number }[];
}

export interface StatsCardProps {
  amrMaxNum: number;
  amrWorking: number;
  amrWaiting: number;
  amrCharging: number;
  amrError: number;
  amrWorkTime: number;
  lineMaxNum: number;
  lineWorking: number;
  storageQuantity: number;
  storageMaxQuantity: number;
  timestamp: string;
}

export interface DashboardData {
  title: string;
  value: string;
  subtext: string[];
  data: StatData;
}

export interface ProductionOverviewData {
  timestamps: string[];
  production: number[];
  target: number[];
}
