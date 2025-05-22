'use client';

import { useLineStore } from '@/shared/store/lineStore';
import { useAnimations, useGLTF } from '@react-three/drei';
import { useEffect, useRef } from 'react';

export const Map3D = () => {
  const group = useRef(null);
  const { scene, animations } = useGLTF('/Factory.glb');
  const { actions } = useAnimations(animations, group);
  const { lines } = useLineStore();

  useEffect(() => {
    const factoryAnimations = Object.values(actions).filter((action) =>
      action?.getClip().name.includes('Line'),
    );
    lines.forEach((line) => {
      const action = factoryAnimations[line.lineId - 1];
      if (line.amount === 0 || !line.status) {
        action?.stop();
      } else {
        action?.play();
      }
    });
  }, [actions, lines]);

  useEffect(() => {
    Object.values(actions).forEach((action) => {
      if (!action?.getClip().name.includes('Line')) {
        action?.play();
      }
    });
  }, [actions]);

  return (
    <>
      {/* 공장 모델 */}
      <primitive
        ref={group}
        object={scene}
        scale={1}
        position={[65.5, 8, 9]}
        rotation={[0, 0, 0]}
      />

      {/* <axesHelper args={[100]} /> */}
      {/* <gridHelper args={[100, 100]} /> */}

      {/* 조명 설정 */}
      <ambientLight intensity={2} />
      <directionalLight position={[5, 8, 5]} intensity={1.5} />
    </>
  );
};

useGLTF.preload('/Factory.glb');
