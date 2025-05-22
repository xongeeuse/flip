import { useRef } from 'react';
import { useFrame } from '@react-three/fiber';
import * as THREE from 'three';
import { damp3, dampE } from 'maath/easing';
import { AMR_CURRENT_STATE } from '@/features/visualization';

const ANIMATION_SETTINGS = {
  rotationDamping: 0.1,
  positionDamping: 0.1,
};

export const useAMRAnimation = (amrInfo: AMR_CURRENT_STATE) => {
  const { locationX, locationY, dir } = amrInfo;

  const groupRef = useRef<THREE.Group>(null);
  const currentPosition = useRef(new THREE.Vector3(locationX, 0, locationY));
  const targetPosition = useRef(new THREE.Vector3(locationX, 0, locationY));
  const currentRotation = useRef(new THREE.Euler(0, dir, 0));
  const targetRotation = useRef(new THREE.Euler(0, dir, 0));

  // 위치나 회전이 변경되면 목표값 업데이트
  // useEffect(() => {
  //   targetPosition.current.set(locationX, 0, locationY);
  //   targetRotation.current.set(0, dir, 0);
  // }, [locationX, locationY, dir]);

  useFrame((state, delta) => {
    if (!groupRef.current) return;

    targetPosition.current.set(locationX, 0, locationY);
    targetRotation.current.set(0, dir, 0);
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

    // 모델에 현재 위치와 회전 적용
    groupRef.current.position.copy(currentPosition.current);
    groupRef.current.rotation.copy(currentRotation.current);
  });

  return { groupRef };
};
