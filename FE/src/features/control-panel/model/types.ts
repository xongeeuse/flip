import { AMRInfo } from '@/entities/amrModel';

export type AMR_CARD_STATUS = Pick<
  AMRInfo,
  | 'amrId'
  | 'state'
  | 'missionId'
  | 'missionType'
  | 'submissionId'
  | 'errorCode'
  | 'timestamp'
  | 'type'
  | 'startX'
  | 'startY'
  | 'targetX'
  | 'targetY'
  | 'expectedArrival'
  | 'startedAt'
  | 'battery'
  | 'realArrival'
>;

export type FACILITY_CARD_STATUS = {
  lineId: number;
  amount: number;
  status: boolean;
};

export type FACILITY_LINE_STATUS = {
  lineList: FACILITY_CARD_STATUS[];
  timestamp: string;
};
