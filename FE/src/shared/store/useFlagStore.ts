import { create } from 'zustand';

interface FlagStore {
  isFlag: boolean;
  setIsFlag: (isFlag: boolean) => void;
}

export const useFlagStore = create<FlagStore>((set) => ({
  isFlag: false,
  setIsFlag: (isFlag) => set({ isFlag }),
}));
