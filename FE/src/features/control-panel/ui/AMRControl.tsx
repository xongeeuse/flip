'use client';

import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { useReducer } from 'react';
import { useSelectedAMRStore } from '@/shared/store/selected-amr-store';
import { MissionCardList } from './MissionCardList';

const FILTER_ALL = 'ALL';

const initialFilter = {
  mission: FILTER_ALL,
  amr: FILTER_ALL,
  state: FILTER_ALL,
};

type FilterState = typeof initialFilter;
type FilterAction = { type: string; value: string };

function filterReducer(state: FilterState, action: FilterAction): FilterState {
  switch (action.type) {
    case 'SET_MISSION':
      return { ...state, mission: action.value };
    case 'SET_AMR':
      return { ...state, amr: action.value };
    case 'SET_STATE':
      return { ...state, state: action.value };
    default:
      return state;
  }
}

export default function AMRControl() {
  const [filter, dispatch] = useReducer(filterReducer, initialFilter);
  const { selectedAmrId, toggleSelectedAMR } = useSelectedAMRStore();

  return (
    <div className='flex flex-col grow p-2 bg-[#0B1120] '>
      <div className='flex w-full gap-2 mb-2 overscroll-none'>
        <Select
          onValueChange={(value) => dispatch({ type: 'SET_MISSION', value })}
          defaultValue={FILTER_ALL}
        >
          <SelectTrigger className='w-full text-white'>
            <SelectValue placeholder='Mission' />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value={FILTER_ALL}>미션</SelectItem>
            <SelectItem value='MOVING'>이동</SelectItem>
            <SelectItem value='CHARGING'>충전</SelectItem>
            <SelectItem value='LOADING'>적재</SelectItem>
            <SelectItem value='UNLOADING'>하역</SelectItem>
          </SelectContent>
        </Select>

        <Select
          onValueChange={(value) => dispatch({ type: 'SET_AMR', value })}
          defaultValue={FILTER_ALL}
        >
          <SelectTrigger className='w-full text-white'>
            <SelectValue placeholder='AMR' />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value={FILTER_ALL}>전체 AMR</SelectItem>
            {['Type-A', 'Type-B', 'Type-C'].map((amr) => (
              <SelectItem key={amr} value={amr}>
                {amr}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>

        <Select
          onValueChange={(value) => dispatch({ type: 'SET_STATE', value })}
          defaultValue={FILTER_ALL}
        >
          <SelectTrigger className='w-full text-white'>
            <SelectValue placeholder='State' />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value={FILTER_ALL}>상태</SelectItem>
            <SelectItem value='0'>에러</SelectItem>
            <SelectItem value='1'>대기</SelectItem>
            <SelectItem value='2'>작업중</SelectItem>
            <SelectItem value='3'>충전중</SelectItem>
          </SelectContent>
        </Select>
      </div>

      <MissionCardList
        filter={filter}
        selectedAmrId={selectedAmrId}
        toggleSelectedAMR={toggleSelectedAMR}
      />
    </div>
  );
}
