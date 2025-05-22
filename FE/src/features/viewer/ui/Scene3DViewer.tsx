'use client';

import { Canvas } from '@react-three/fiber';
import { MapControls } from '@react-three/drei';
import { Suspense, useEffect, useRef } from 'react';
import { Map3D, MapLoading } from '@/entities/map';
import { Model3DRenderer } from '@/entities/amrModel';
import { useCameraFollow } from '../lib';
import * as THREE from 'three';
import { PersonModel } from '@/entities/personModel';
import { usePersonModelStore } from '@/shared/store/person-model-store';
import SelectedAMRMarkers from './SelectedAMRMarkers';

const Warehouse = () => {
  const { isRepairing } = usePersonModelStore();
  const { controlsRef } = useCameraFollow();
  const lastValidTarget = useRef<THREE.Vector3>(new THREE.Vector3(40, 0, 40));
  const isUpdating = useRef(false);

  useEffect(() => {
    if (!controlsRef.current) return;

    const controls = controlsRef.current;

    lastValidTarget.current.copy(controls.target);

    const handleControlChange = () => {
      if (isUpdating.current) return;

      const camera = controls.object;
      const currentTarget = controls.target;

      if (
        currentTarget.x < 5 ||
        currentTarget.x > 75 ||
        currentTarget.z < 5 ||
        currentTarget.z > 75
      ) {
        isUpdating.current = true;

        const cameraOffset = new THREE.Vector3().subVectors(camera.position, currentTarget);

        const newTargetX = Math.max(0, Math.min(80, currentTarget.x));
        const newTargetZ = Math.max(0, Math.min(80, currentTarget.z));
        const newTarget = new THREE.Vector3(newTargetX, currentTarget.y, newTargetZ);

        const newCameraPosition = new THREE.Vector3().addVectors(newTarget, cameraOffset);

        controls.target.copy(newTarget);
        camera.position.copy(newCameraPosition);

        camera.updateProjectionMatrix();
        controls.update();

        lastValidTarget.current.copy(newTarget);

        setTimeout(() => {
          isUpdating.current = false;
        }, 0);
      } else {
        lastValidTarget.current.copy(currentTarget);
      }
    };

    controls.addEventListener('change', handleControlChange);

    return () => {
      controls.removeEventListener('change', handleControlChange);
    };
  }, [controlsRef]);

  return (
    <>
      {/* 모델 렌더링 */}
      <Model3DRenderer />

      <SelectedAMRMarkers />

      {/* 정비 상태일 때만 PersonModel 렌더링 */}
      {isRepairing && <PersonModel />}

      <MapControls
        ref={controlsRef}
        enableZoom={true}
        minDistance={5}
        maxDistance={80}
        autoRotate={false}
        maxPolarAngle={Math.PI / 4}
        minPolarAngle={Math.PI / 4}
        target={[40, 0, 40]}
      />
    </>
  );
};

const Scene3DViewer = () => {
  return (
    <>
      <MapLoading />
      <Suspense fallback={null}>
        <Canvas camera={{ position: [60, 50, 60], fov: 60 }}>
          <Map3D />
          <Warehouse />
        </Canvas>
      </Suspense>
    </>
  );
};

export default Scene3DViewer;
