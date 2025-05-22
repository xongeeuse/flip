'use client';

import { AMRModel, SSF250Model, SSF1200Model } from '@/entities/amrModel';
import { useModelStore } from '@/shared/model/store';
import { useThree } from '@react-three/fiber';
import * as THREE from 'three';

const ANIMATION_SETTINGS = {
  height: 0,
};

export const Model3DRenderer = () => {
  const { models } = useModelStore();
  const { camera } = useThree();

  return (
    <>
      {models?.map((amrInfo) => {
        const position: [number, number, number] = [
          amrInfo.locationX,
          ANIMATION_SETTINGS.height,
          amrInfo.locationY,
        ];
        const rotation: [number, number, number] = [0, amrInfo.dir, 0];

        // PerspectiveCamera: 카메라와 모델 사이의 거리로 showAmrId 결정
        let showAmrId = false;
        if (camera.type === 'PerspectiveCamera') {
          const modelPos = new THREE.Vector3(...position);
          const cameraPos = camera.position;
          const distance = cameraPos.distanceTo(modelPos);
          showAmrId = distance < 30; // 예시: 30 이하일 때만 표시
        } else if (camera.type === 'OrthographicCamera') {
          showAmrId = camera.zoom >= 8;
        }

        switch (amrInfo.type) {
          case 'Type-A':
            return (
              <AMRModel
                key={amrInfo.amrId}
                position={position}
                rotation={rotation}
                amrState={amrInfo}
                showAmrId={showAmrId}
              />
            );
          case 'Type-B':
            return (
              <SSF250Model
                key={amrInfo.amrId}
                position={position}
                rotation={rotation}
                amrState={amrInfo}
                showAmrId={showAmrId}
              />
            );
          case 'Type-C':
          default:
            return (
              <SSF1200Model
                key={amrInfo.amrId}
                position={position}
                rotation={rotation}
                amrState={amrInfo}
                showAmrId={showAmrId}
              />
            );
        }
      })}
    </>
  );
};
