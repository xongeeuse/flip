import { AMR_CURRENT_STATE } from '@/features/visualization';
import { Group, Object3DEventMap } from 'three';

// AMR 상태 정의
export enum AMRState {
  ERROR = 0,
  IDLE = 1,
  PROCESSING = 2,
  CHARGING = 3,
}

// 모델 타입 정의
export type ModelType = 'amr' | 'ssf250' | 'ssf1200';

export type MissionType = 'MOVING' | 'CHARGING' | 'LOADING' | 'UNLOADING';

// AMR 정보 인터페이스
export interface AMRInfo {
  amrId: string;
  state: number; // AMRState enum 값 (0: ERROR, 1: IDLE, 2: PROCESSING, 3: CHARGING)
  locationX: number;
  locationY: number;
  dir: number;
  battery: number;
  currentNode: number;
  loading: boolean;
  missionId: number;
  missionType: MissionType;
  submissionId: number;
  linearVelocity: number;
  errorList: string[];
  errorCode: string; // 에러 발생 시 에러 코드
  timestamp: string;
  type: string;
  startX: number;
  startY: number;
  targetX: number;
  targetY: number;
  prevX: number;
  prevY: number;
  expectedArrival: number;
  startedAt: string;
  realArrival: number;
}

// 모델 정보 인터페이스 (3D 렌더링용)
export interface ModelInfo {
  position: [number, number, number];
  rotation: [number, number, number];
  type: ModelType;
  id: string;
  // AMR 관련 추가 정보
  state?: AMRState;
  battery?: number;
  loading?: boolean;
  linearVelocity?: number;
}

export interface Model3DProps {
  position: [number, number, number];
  rotation: [number, number, number];
  scene: Group<Object3DEventMap>;
  className?: string;
  amrState: AMR_CURRENT_STATE;
  showAmrId?: boolean;
}
