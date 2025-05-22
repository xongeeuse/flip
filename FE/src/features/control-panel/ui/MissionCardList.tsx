import { useEffect, useState, useMemo } from 'react';
import { MissionCard } from './MissionCard';
import type { AMR_CARD_STATUS } from '../model';
import { useAmrSocketStore } from '@/shared/store/amrSocket';
import { useSelectedAMRStore } from '@/shared/store/selected-amr-store';

interface MissionCardListProps {
  filter: {
    mission: string;
    amr: string;
    state: string;
  };
  selectedAmrId: string | null;
  toggleSelectedAMR: (amr: AMR_CARD_STATUS) => void;
}

const FILTER_ALL = 'ALL';

export function MissionCardList({
  filter,
  selectedAmrId,
  toggleSelectedAMR,
}: MissionCardListProps) {
  const [data, setData] = useState<AMR_CARD_STATUS[]>([]);
  const { amrSocket, isConnected } = useAmrSocketStore();
  const store = useSelectedAMRStore();

  useEffect(() => {
    if (!isConnected || !amrSocket) return;
    const subscription = amrSocket.subscribe('/amr/mission', (message) => {
      const data = JSON.parse(message.body) as AMR_CARD_STATUS[];
      setData(data);
    });
    return () => {
      if (subscription && typeof subscription.unsubscribe === 'function') {
        subscription.unsubscribe();
      }
    };
  }, [amrSocket, isConnected]);

  const filteredData = useMemo(
    () =>
      data.filter((amr) => {
        if (filter.mission !== FILTER_ALL && amr.missionType !== filter.mission) return false;
        if (filter.amr !== FILTER_ALL && amr.type !== filter.amr) return false;
        if (filter.state !== FILTER_ALL && amr.state !== Number(filter.state)) return false;
        return true;
      }),
    [data, filter],
  );

  useEffect(() => {
    if (!selectedAmrId) return;
    const selected = filteredData.find((amr) => amr.amrId === selectedAmrId);
    if (selected) {
      if (store.startX !== selected.startX || store.startY !== selected.startY) {
        store.setStartPosition(selected.startX, selected.startY);
      }
      if (store.targetX !== selected.targetX || store.targetY !== selected.targetY) {
        store.setTargetPosition(selected.targetX, selected.targetY);
      }
    }
  }, [selectedAmrId, filteredData, store]);

  return (
    <div className='flex flex-col h-full p-1 mt-1 overflow-y-auto hide-scrollbar'>
      {filteredData.map((amr) => (
        <MissionCard
          key={amr.amrId}
          data={amr}
          isSelected={selectedAmrId === amr.amrId}
          onClick={() => toggleSelectedAMR(amr)}
        />
      ))}
    </div>
  );
}
