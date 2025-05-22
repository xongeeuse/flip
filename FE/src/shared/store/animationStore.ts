import { AnimationAction } from 'three';
import { create } from 'zustand';

interface AnimationStore {
  factoryAnimations: (AnimationAction | null)[];
  setFactoryAnimation: (animation: (AnimationAction | null)[]) => void;
}

export const useAnimationStore = create<AnimationStore>((set) => ({
  factoryAnimations: [],
  setFactoryAnimation: (animation: (AnimationAction | null)[]) =>
    set({ factoryAnimations: animation }),
}));
