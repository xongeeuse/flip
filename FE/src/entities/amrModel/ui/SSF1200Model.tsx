'use client';

import { BaseModel3D } from './BaseModel3D';
import { Model3DProps } from '../model/types';
import { useAnimations, useGLTF } from '@react-three/drei';
import { useEffect, useMemo } from 'react';
import * as THREE from 'three';

export const SSF1200Model = (props: Omit<Model3DProps, 'scene'>) => {
  const { scene, animations } = useGLTF('/SSF-650.glb');
  const {
    amrState: { loading },
  } = props;
  // 각 인스턴스마다 새로운 scene 클론 생성
  const instance = useMemo(() => {
    const clonedScene = scene.clone();
    clonedScene.traverse((child) => {
      if (child instanceof THREE.Mesh) {
        child.material = child.material.clone();
      }
    });
    return clonedScene;
  }, [scene]);
  const { actions } = useAnimations(animations, instance);

  useEffect(() => {
    // 이전 애니메이션 중지
    Object.values(actions).forEach((action) => {
      action?.stop();
    });

    if (loading) {
      actions['SSF-650 Load']?.reset().play();
    } else {
      actions['SSF-650 Unload']?.reset().play();
    }
  }, [loading, actions]);

  return <BaseModel3D scene={instance} {...props} />;
};
