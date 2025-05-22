import { useRef, useEffect } from 'react';
import { MapControls as MapControlsImpl } from 'three-stdlib';
import { useModelStore } from '@/shared/model/store';
import { useSelectedAMRStore } from '@/shared/store/selected-amr-store';
import { useFrame } from '@react-three/fiber';
import * as THREE from 'three';
import { damp3 } from 'maath/easing';

export const useCameraFollow = () => {
  const { getSelectedModel } = useModelStore();
  const selectedAmrId = useSelectedAMRStore((state) => state.selectedAmrId);
  const controlsRef = useRef<MapControlsImpl>(null);
  const targetPosition = useRef(new THREE.Vector3());
  const cameraPosition = useRef(new THREE.Vector3());

  // 초기 카메라 설정 저장
  useEffect(() => {
    if (!controlsRef.current) return;

    const controls = controlsRef.current;
    targetPosition.current.copy(controls.target);
    cameraPosition.current.copy(controls.object.position);

    return () => {
      controls.dispose();
    };
  }, []);

  useFrame((_, delta) => {
    if (!controlsRef.current) return;

    const selectedAMR = getSelectedModel(selectedAmrId);

    if (selectedAMR) {
      const controls = controlsRef.current;
      const camera = controls.object;
      const currentTarget = controls.target;

      // 현재 카메라의 상대적 위치 계산
      const offsetX = camera.position.x - currentTarget.x;
      const offsetZ = camera.position.z - currentTarget.z;

      // 새로운 타겟 위치 설정
      targetPosition.current.set(selectedAMR.locationX, 0, selectedAMR.locationY);

      // 새로운 카메라 위치 계산 (상대적 거리 유지)
      cameraPosition.current.set(
        targetPosition.current.x + offsetX,
        camera.position.y,
        targetPosition.current.z + offsetZ,
      );

      // damp3를 사용하여 부드럽게 이동
      const damping = 0.25; // 감쇠 계수 (실험적으로 조정)
      damp3(currentTarget, targetPosition.current, damping, delta);
      damp3(camera.position, cameraPosition.current, damping, delta);

      controls.update();
    }
  });

  return { controlsRef };
};
