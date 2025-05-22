'use client';

import { useEffect, useState, useRef, useCallback } from 'react';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';
import { Canvas, useFrame } from '@react-three/fiber';
import { MapControls } from '@react-three/drei';
import { MapControls as MapControlsImpl } from 'three-stdlib';
import { Map3D } from '@/entities/map';
import { AMR_CURRENT_STATE } from '@/features/visualization';
import * as THREE from 'three';
import { degreeToRadian } from '@/shared/hooks/useMathUtils';

const SOCKET_URL = process.env.NEXT_PUBLIC_BACKEND_SERVER + '/ws';

const AMR_STATE_COLORS: Record<number, string> = {
  0: '#F44336', // ERROR
  1: '#4CAF50', // IDLE
  2: '#2196F3', // PROCESSING
  3: '#FFC107', // CHARGING
};

// 썸네일용 AMR 렌더러 (선택/상호작용 없이 단순 렌더)
function AMR2DThumbnailRenderer({ amrInfo }: { amrInfo: AMR_CURRENT_STATE }) {
  const groupRef = useRef<THREE.Group>(null);

  useEffect(() => {
    if (!groupRef.current) return;
    groupRef.current.position.set(amrInfo.locationX, 0, amrInfo.locationY);
    groupRef.current.rotation.set(0, amrInfo.dir - Math.PI / 2, 0);
  }, [amrInfo]);

  const color = AMR_STATE_COLORS[amrInfo.state] || '#9E9E9E';

  return (
    <group ref={groupRef}>
      <mesh>
        <boxGeometry args={[1.2, 0.5, 0.8]} />
        <meshStandardMaterial color={color} />
      </mesh>
      <group position={[0, 0.3, 0]}>
        <mesh>
          <boxGeometry args={[1, 0.1, 0.2]} />
          <meshStandardMaterial color={color} />
        </mesh>
        <mesh position={[0.6, 0, 0]}>
          <coneGeometry args={[0.3, 0.6, 4]} />
          <meshStandardMaterial color={color} />
        </mesh>
      </group>
    </group>
  );
}

const ThumnailContent = () => {
  const [models, setModels] = useState<AMR_CURRENT_STATE[]>([]);

  useEffect(() => {
    const socket = new SockJS(SOCKET_URL + '/status');
    const client = new Client({
      webSocketFactory: () => socket,
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      onConnect: () => {
        client.subscribe('/amr/real-time', (message) => {
          const data = JSON.parse(message.body).amrRealTimeList;
          setModels(data.map((d: AMR_CURRENT_STATE) => ({ ...d, dir: degreeToRadian(d.dir) })));
        });
      },
    });

    client.activate();

    return () => {
      client.deactivate();
      socket.close();
    };
  }, []);

  // 카메라 컨트롤 제한
  const controlsRef = useRef<MapControlsImpl>(null);
  useFrame(() => {
    if (!controlsRef.current) return;
    const camera = controlsRef.current.object;
    const target = controlsRef.current.target;
    const clamp = (v: number, min: number, max: number) => Math.max(min, Math.min(max, v));
    target.x = clamp(target.x, 0, 80);
    target.z = clamp(target.z, 0, 80);
    camera.position.x = clamp(camera.position.x, 0, 80);
    camera.position.z = clamp(camera.position.z, 0, 80);
  });

  return (
    <>
      {models.map((amrInfo) => (
        <AMR2DThumbnailRenderer key={amrInfo.amrId} amrInfo={amrInfo} />
      ))}
      <ambientLight intensity={1} />
      <directionalLight position={[0, 10, 0]} intensity={0} />
      <MapControls
        ref={controlsRef}
        enableZoom={true}
        minDistance={5}
        maxDistance={60}
        maxPolarAngle={0}
        minPolarAngle={0}
        enableRotate={false}
        target={[42, 0, 42]}
      />
    </>
  );
};

// 썸네일 전용 2D 뷰어
export default function Scene2DThumbnailViewer() {
  const [canvasKey, setCanvasKey] = useState(() => Date.now());
  const containerRef = useRef<HTMLDivElement | null>(null);

  // context lost 핸들러
  const handleContextLost = useCallback((e: Event) => {
    e.preventDefault();
    setCanvasKey(Date.now());
  }, []);

  useEffect(() => {
    const container = containerRef.current;
    if (!container) return;
    // 실제 <canvas> DOM을 찾음
    const canvas = container.querySelector('canvas');
    if (!canvas) return;
    canvas.addEventListener('webglcontextlost', handleContextLost, false);
    return () => {
      canvas.removeEventListener('webglcontextlost', handleContextLost, false);
    };
  }, [handleContextLost, canvasKey]);

  return (
    <div ref={containerRef} className='relative w-full h-full cursor-grab active:cursor-grabbing'>
      <Canvas key={canvasKey} orthographic camera={{ position: [42, 60, 42], zoom: 5.5 }}>
        <Map3D />
        <ThumnailContent />
      </Canvas>
    </div>
  );
}
