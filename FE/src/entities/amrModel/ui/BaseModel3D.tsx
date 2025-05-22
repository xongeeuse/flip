'use client';

import { useFrame } from '@react-three/fiber';
import { useRef, useEffect } from 'react';
import { Model3DProps } from '../model/types';
import * as THREE from 'three';
import { damp3, dampE } from 'maath/easing';
import { useSelectedAMRStore } from '@/shared/store/selected-amr-store';
import { Html } from '@react-three/drei';

const ANIMATION_SETTINGS = {
  rotationDamping: 0.25,
  positionDamping: 0.25,
};

export const BaseModel3D = ({
  scene,
  position,
  rotation,
  amrState,
  showAmrId = true,
}: Model3DProps & { showAmrId?: boolean }) => {
  const selectedAmrId = useSelectedAMRStore((state) => state.selectedAmrId);
  const { amrId } = amrState;

  const modelRef = useRef<THREE.Group>(null);

  // 현재 위치와 회전 상태 관리
  const currentPosition = useRef(new THREE.Vector3(...position));
  const targetPosition = useRef(new THREE.Vector3(...position));
  const currentRotation = useRef(new THREE.Euler(...rotation));
  const targetRotation = useRef(new THREE.Euler(...rotation));

  // Html 위치 부드럽게 이동
  const currentHtmlPosition = useRef(
    new THREE.Vector3(position[0] - 0.3, position[1] + 2.5, position[2] + 0.3),
  );
  const targetHtmlPosition = useRef(
    new THREE.Vector3(position[0] - 0.3, position[1] + 2.5, position[2] + 0.3),
  );

  // 위치나 회전이 변경되면 목표값 업데이트
  useEffect(() => {
    targetPosition.current.set(...position);
    targetRotation.current.set(...rotation);
    targetHtmlPosition.current.set(position[0] - 0.3, position[1] + 2.5, position[2] + 0.3);
  }, [position, rotation]);

  useFrame((state, delta) => {
    if (!modelRef.current) return;

    // 매 프레임마다 회전과 위치를 부드럽게 업데이트
    dampE(
      currentRotation.current,
      targetRotation.current,
      ANIMATION_SETTINGS.rotationDamping,
      delta,
    );

    damp3(
      currentPosition.current,
      targetPosition.current,
      ANIMATION_SETTINGS.positionDamping,
      delta,
    );

    // Html 위치도 부드럽게 이동
    damp3(currentHtmlPosition.current, targetHtmlPosition.current, 0.08, delta);

    // 모델에 현재 위치와 회전 적용
    modelRef.current.position.copy(currentPosition.current);
    modelRef.current.rotation.copy(currentRotation.current);
  });

  // click 효과 적용
  useEffect(() => {
    scene.traverse((child) => {
      if (child instanceof THREE.Mesh) {
        if (selectedAmrId === amrId) {
          child.material.emissive = new THREE.Color(0x0000ff);
          child.material.emissiveIntensity = 0.2;
          child.material.opacity = 0.9;
        } else {
          child.material.emissive = new THREE.Color(0x000000);
          child.material.emissiveIntensity = 0;
          child.material.opacity = 1;
        }
      }
    });
  }, [selectedAmrId, amrId]);

  return (
    <>
      <group ref={modelRef}>
        {showAmrId && (
          <Html position={[0.5, 2.5, 0.5]}>
            <div
              className='text-base font-bold text-white'
              style={{ fontSize: '18px', minWidth: 32, textAlign: 'center' }}
            >
              {amrId}
            </div>
          </Html>
        )}
        <primitive object={scene} className='cursor-pointer' />
      </group>
    </>
  );
};
