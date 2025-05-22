import { create } from 'zustand';

interface PersonModelStore {
  isRepairing: boolean;
  setRepairing: (isRepairing: boolean) => void;
}

export const usePersonModelStore = create<PersonModelStore>((set) => ({
  isRepairing: false,
  setRepairing: (isRepairing) => set({ isRepairing }),
})); 