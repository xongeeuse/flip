import { create } from 'zustand';
import { AMR_CURRENT_STATE } from '@/features/visualization/model/types';

interface ModelStore {
  models: AMR_CURRENT_STATE[];
  // 전체 모델 업데이트 함수 추가
  updateModels: (models: AMR_CURRENT_STATE[]) => void;
  getSelectedModel: (selectedAmrId: string | null) => AMR_CURRENT_STATE | undefined;
}

export const useModelStore = create<ModelStore>((set, get) => ({
  models: [],
  // 전체 모델 업데이트 함수 구현
  updateModels: (models) =>
    set(() => ({
      models,
    })),
  getSelectedModel: (selectedAmrId) => {
    if (!selectedAmrId) return undefined;
    return get().models.find((model) => model.amrId === selectedAmrId);
  },
}));
