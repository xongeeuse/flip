import { useSelectedAMRStore } from '@/shared/store/selected-amr-store';
import { MapPointer } from '@/entities/3dPointer';
import { RoutePath } from './RoutePath';
import React from 'react';

const SelectedAMRMarkers = () => {
  const selectedAmrId = useSelectedAMRStore((state) => state.selectedAmrId);
  const startX = useSelectedAMRStore((state) => state.startX);
  const startY = useSelectedAMRStore((state) => state.startY);
  const targetX = useSelectedAMRStore((state) => state.targetX);
  const targetY = useSelectedAMRStore((state) => state.targetY);
  return (
    <>
      {selectedAmrId && startX && startY && (
        <MapPointer position={[startX, 3, startY]} color='Blue' />
      )}
      {selectedAmrId && targetX && targetY && (
        <MapPointer position={[targetX, 3, targetY]} color='Red' />
      )}
      <RoutePath />
    </>
  );
};

export default SelectedAMRMarkers;
