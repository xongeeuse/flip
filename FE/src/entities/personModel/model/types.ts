import { Group, Object3DEventMap } from 'three';

export enum PersonState {
  IDLE = 0,
  WALKING = 1,
  FIXING = 2,
  TURNLEFT = 3,
  TURNRIGHT = 4,
}

// 소켓으로부터 받는 원본 데이터 타입
export interface PersonSocketData {
  humanId: string;
  x: number;
  y: number;
  direction: number;
  state: PersonState;
}

export interface PersonPosition {
  position: [number, number, number];
  rotation: [number, number, number];
  state: PersonState;
}

export interface PersonInfo {
  personId: string;
  state: PersonState;
  locationX: number;
  locationY: number;
}

export interface PersonModelProps {
  personState: {
    loading: boolean;
    data?: PersonSocketData;
    position?: PersonPosition;
  };
  position?: [number, number, number];
  rotation?: [number, number, number];
  scale?: number;
  scene: Group<Object3DEventMap>;
}