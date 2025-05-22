import { HeatmapEdge } from '../model/types';

const API_BASE_URL = process.env.NEXT_PUBLIC_BACKEND_SERVER;

export const fetchHeatmapData = async (): Promise<HeatmapEdge[]> => {
  try {
    const response = await fetch(`${API_BASE_URL}/api/v1/status/heatmap`);
    if (!response.ok) {
      throw new Error('Failed to fetch heatmap data');
    }
    const data = await response.json();
    return data.data;
  } catch (error) {
    console.error('Error fetching heatmap data:', error);
    return [];
  }
}; 