import { AMRInfo } from '@/entities/amrModel/model/types';

export type AMR_CURRENT_STATE = Pick<
  AMRInfo,
  | 'amrId'
  | 'state'
  | 'locationX'
  | 'locationY'
  | 'dir'
  | 'currentNode'
  | 'loading'
  | 'linearVelocity'
  | 'errorCode'
  | 'type'
>;
