import { useState, useEffect } from 'react';
import { fetchHeatmapData } from '@/features/heatmap/api/heatmap';
import { HeatmapEdge } from '../model/types';

export const useHeatmapData = () => {
  const [heatmapData, setHeatmapData] = useState<HeatmapEdge[]>([]);

  useEffect(() => {
    const loadHeatmapData = async () => {
      try {
        const data = await fetchHeatmapData();
        setHeatmapData(data);
      } catch (error) {
        console.error('Failed to load heatmap data:', error);
        setHeatmapData([]);
      }
    };

    loadHeatmapData();
  }, []);

  return heatmapData;
};
