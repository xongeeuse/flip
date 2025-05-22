import { Client } from '@stomp/stompjs';
import { create } from 'zustand';

interface AmrSocketStore {
  amrSocket: Client | null;
  isConnected: boolean;
  setAmrSocket: (amrSocket: Client | null) => void;
  setIsConnected: (isConnected: boolean) => void;
  reset: () => void;
}

export const useAmrSocketStore = create<AmrSocketStore>((set) => ({
  amrSocket: null,
  isConnected: false,
  setAmrSocket: (amrSocket: Client | null) => set({ amrSocket }),
  setIsConnected: (isConnected: boolean) => set({ isConnected }),
  reset: () => set({ amrSocket: null, isConnected: false }),
}));
