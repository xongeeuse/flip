import { FACILITY_CARD_STATUS } from '@/features/control-panel/model';
import { create } from 'zustand';

interface LineStore {
  lines: FACILITY_CARD_STATUS[];
  setLines: (lines: FACILITY_CARD_STATUS[]) => void;
}

export const useLineStore = create<LineStore>((set) => ({
  lines: [],
  setLines: (lines: FACILITY_CARD_STATUS[]) => set({ lines }),
}));
