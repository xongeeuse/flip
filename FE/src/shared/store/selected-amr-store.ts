import { create } from 'zustand';
import { AMR_CARD_STATUS } from '@/features/control-panel/model';

interface SelectedAMRState {
  selectedAmrId: string | null;
  targetX: number | null;
  targetY: number | null;
  startX: number | null;
  startY: number | null;
  setSelectedAmrId: (id: string | null) => void;
  setTargetPosition: (x: number, y: number) => void;
  setStartPosition: (x: number, y: number) => void;
  reset: () => void;
  toggleSelectedAMR: (amr: AMR_CARD_STATUS) => void;
}

export const useSelectedAMRStore = create<SelectedAMRState>((set) => ({
  selectedAmrId: null,
  targetX: 0,
  targetY: 0,
  startX: 0,
  startY: 0,
  setSelectedAmrId: (id) => set({ selectedAmrId: id }),
  setTargetPosition: (x, y) => set({ targetX: x, targetY: y }),
  setStartPosition: (x, y) => set({ startX: x, startY: y }),
  reset: () =>
    set({ selectedAmrId: null, targetX: null, targetY: null, startX: null, startY: null }),
  toggleSelectedAMR: (amr) =>
    set((state) => {
      if (state.selectedAmrId === amr.amrId) {
        return {
          selectedAmrId: null,
          targetX: 0,
          targetY: 0,
          startX: 0,
          startY: 0,
        };
      } else {
        return {
          selectedAmrId: amr.amrId,
          targetX: amr.targetX,
          targetY: amr.targetY,
          startX: amr.startX,
          startY: amr.startY,
        };
      }
    }),
}));
