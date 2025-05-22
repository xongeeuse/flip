'use client';

import { PersonState, PersonSocketData } from '../model/types';
import { useGLTF, useAnimations } from '@react-three/drei';
import { useEffect, useState, useRef } from 'react';
import { useAmrSocketStore } from '@/shared/store/amrSocket';
import * as THREE from 'three';
import { useFrame } from '@react-three/fiber';

const ANIMATION_MAP = {
  [PersonState.IDLE]: '',  // IDLE 상태일 때는 애니메이션 없음
  [PersonState.WALKING]: 'Walk',
  [PersonState.FIXING]: 'Fix',
  [PersonState.TURNLEFT]: 'Turn_Left',
  [PersonState.TURNRIGHT]: 'Turn_Right'
} as const;

const LERP_FACTOR = 0.1; // 보간 속도 조절 (0~1 사이 값)

export const PersonModel = () => {
  const { scene, animations } = useGLTF('/Person.glb');
  const { actions } = useAnimations(animations, scene);
  const { amrSocket, isConnected } = useAmrSocketStore();
  const previousState = useRef<PersonState>(PersonState.IDLE);
  
  // 현재 위치와 회전값
  const currentPosition = useRef<[number, number, number]>([1000, 0, 1000]);
  const currentRotation = useRef<[number, number, number]>([0, 0, 0]);
  
  // 목표 위치와 회전값
  const targetPosition = useRef<[number, number, number]>([1000, 0, 1000]);
  const targetRotation = useRef<[number, number, number]>([0, 0, 0]);
  
  const [state, setState] = useState<PersonState>(PersonState.IDLE);

  const playAnimation = (state: PersonState) => {
    // 이전 애니메이션 중지
    const prevAnimation = ANIMATION_MAP[previousState.current];
    if (prevAnimation && actions[prevAnimation]) {
      actions[prevAnimation].stop();
    }

    // IDLE 상태면 애니메이션 실행하지 않고 이전 상태 업데이트
    if (state === PersonState.IDLE) {
      previousState.current = state;
      return;
    }

    // 새로운 애니메이션 재생
    const newAnimation = ANIMATION_MAP[state];
    if (newAnimation && actions[newAnimation]) {
      actions[newAnimation].reset();
      actions[newAnimation].play();

    }

    previousState.current = state;
  };

  useEffect(() => {
    if (state !== previousState.current) {
      playAnimation(state);
    }
  }, [state]);
  
  useEffect(() => {
    if (!amrSocket || !isConnected) return;
    
    const destination = '/app/amr/human';
    amrSocket.publish({ destination });
    
    amrSocket.subscribe('/amr/human', (data) => {
      const message: PersonSocketData = JSON.parse(data.body);
      
      // 목표 위치와 회전값 업데이트
      targetPosition.current = [message.x, 0, message.y];
      targetRotation.current = [0, (message.direction * Math.PI) / 180, 0];
      setState(message.state);
    });

    return () => {
      amrSocket.publish({destination: destination + '/unsubscribe'})
    }
  }, [amrSocket, isConnected]);

  // useFrame을 사용하여 부드러운 보간 적용
  useFrame(() => {
    // 위치 보간
    currentPosition.current = currentPosition.current.map((current, index) => {
      return THREE.MathUtils.lerp(current, targetPosition.current[index], LERP_FACTOR);
    }) as [number, number, number];

    // 회전 보간
    currentRotation.current = currentRotation.current.map((current, index) => {
      return THREE.MathUtils.lerp(current, targetRotation.current[index], 0.6);
    }) as [number, number, number];

    // scene의 위치와 회전 업데이트
    scene.position.set(...currentPosition.current);
    scene.rotation.set(...currentRotation.current);
  });

  return (
    <primitive
      object={scene}
    />
  );
}; 

useGLTF.preload('/Person.glb');