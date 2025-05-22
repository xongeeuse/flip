'use client';

import { Canvas, useFrame, useThree } from '@react-three/fiber';
import { MapControls } from '@react-three/drei';
import { useModelStore } from '@/shared/model/store';
import { Html } from '@react-three/drei';
import { AMR_CURRENT_STATE } from '@/features/visualization';
import { useSelectedAMRStore } from '@/shared/store/selected-amr-store';
import { Map3D } from '@/entities/map';
import { useAMRAnimation, useCameraFollow, AMR_STATE_COLORS } from '../lib';
import { HeatmapLayer } from '@/features/heatmap/ui/HeatmapLayer';
import { Button } from '@/components/ui/button';
import { useState } from 'react';
import { Eye, EyeOff } from 'lucide-react';
import { HeatmapLegend } from '@/features/heatmap/ui/HeatmapLegend';
import { AMRLegend } from './AMRLegend';
import { RoutePath } from './RoutePath';

// AMR 2D 렌더러 컴포넌트
const AMR2DRenderer = ({
  amrInfo,
  showAmrId = true,
}: {
  amrInfo: AMR_CURRENT_STATE;
  showAmrId?: boolean;
}) => {
  const { state, amrId } = amrInfo;
  const color = AMR_STATE_COLORS[state] || '#9E9E9E';
  const { groupRef } = useAMRAnimation(amrInfo);
  const { selectedAmrId, setSelectedAmrId } = useSelectedAMRStore();
  const isSelected = selectedAmrId === amrId;

  const handleClick = (event: React.MouseEvent) => {
    event.stopPropagation(); // 상위로 이벤트 전파 방지
    console.log('click', amrId);
    setSelectedAmrId(amrId);
  };

  return (
    <group ref={groupRef} onClick={handleClick}>
      {/* 선택된 AMR 주변 하이라이트 */}
      {isSelected && (
        <mesh position={[0, 0, 0]}>
          <boxGeometry args={[1.6, 0.7, 1.2]} />
          <meshBasicMaterial color='#00ff00' transparent opacity={0.2} />
        </mesh>
      )}
      {/* AMR 본체 - 방향에 맞게 길쭉한 형태로 변경 */}
      <mesh>
        <boxGeometry args={[1.2, 0.5, 0.8]} />
        <meshStandardMaterial color={isSelected ? '#00ff00' : color} />
      </mesh>

      {/* 방향 표시기 - 본체와 일치하도록 수정 */}
      <group position={[0, 0.3, 0]}>
        {/* 화살표 본체 */}
        <mesh>
          <boxGeometry args={[1, 0.1, 0.2]} />
          <meshStandardMaterial color={isSelected ? '#00ff00' : color} />
        </mesh>
        {/* 화살표 머리 */}
        <mesh position={[0.6, 0, 0]}>
          <coneGeometry args={[0.3, 0.6, 4]} />
          <meshStandardMaterial color={isSelected ? '#00ff00' : color} />
        </mesh>
      </group>

      {/* AMR ID 표시 */}
      {showAmrId && (
        <mesh position={[0, 1, 0]}>
          <Html position={[0, 0.1, 0]} center>
            <div className='text-xs font-bold text-black'>{amrId}</div>
          </Html>
        </mesh>
      )}
    </group>
  );
};

// 2D 맵과 AMR을 포함하는 컴포넌트
const Scene2DContent = ({ showHeatmap }: { showHeatmap: boolean }) => {
  const { models } = useModelStore();
  const { controlsRef } = useCameraFollow();
  const { camera } = useThree();
  // zoom이 9 이하일 때만 라벨 표시
  const showAmrId = camera.type === 'OrthographicCamera' ? camera.zoom >= 9 : false;

  const BOUNDS = {
    MIN: 0,
    MAX: 80,
  } as const;

  const clamp = (value: number, min: number, max: number) => {
    return Math.max(min, Math.min(max, value));
  };

  useFrame(() => {
    if (!controlsRef.current) return;

    const camera = controlsRef.current.object;
    const target = controlsRef.current.target;

    // X, Z축 제한
    target.x = clamp(target.x, BOUNDS.MIN, BOUNDS.MAX);
    target.z = clamp(target.z, BOUNDS.MIN, BOUNDS.MAX);

    // 카메라 위치도 제한
    camera.position.x = clamp(camera.position.x, BOUNDS.MIN, BOUNDS.MAX);
    camera.position.z = clamp(camera.position.z, BOUNDS.MIN, BOUNDS.MAX);
  });

  return (
    <>
      {models.map((amrInfo) => (
        <AMR2DRenderer
          key={amrInfo.amrId}
          amrInfo={{ ...amrInfo, dir: amrInfo.dir - Math.PI / 2 }}
          showAmrId={showAmrId}
        />
      ))}
      {showHeatmap && <HeatmapLayer />}
      {/* 조명 설정 */}
      <ambientLight intensity={1} />
      {/* 카메라 컨트롤 */}
      <MapControls
        ref={controlsRef}
        enableZoom={true}
        minDistance={5}
        maxDistance={60}
        maxPolarAngle={0}
        minPolarAngle={0}
        enableRotate={false}
        target={[40, 0, 40]}
      />
    </>
  );
};

// 2D 시각화 컴포넌트
const Scene2DViewer = () => {
  const { reset } = useSelectedAMRStore();
  const [showHeatmap, setShowHeatmap] = useState(false);

  const handleCanvasClick = (event: React.MouseEvent) => {
    // event.defaultPrevented가 true이면 하위 컴포넌트에서 이벤트를 처리한 것
    if (!event.defaultPrevented) {
      reset();
    }
  };

  return (
    <div
      className='relative w-full h-full cursor-grab active:cursor-grabbing'
      onClick={handleCanvasClick}
    >
      <div className='absolute z-10 top-4 right-4'>
        <Button
          variant='outline'
          onClick={(e: React.MouseEvent) => {
            e.stopPropagation();
            setShowHeatmap(!showHeatmap);
          }}
          className='flex items-center gap-2 text-white cursor-pointer bg-black/50 hover:bg-white/50'
        >
          {showHeatmap ? <Eye className='w-4 h-4' /> : <EyeOff className='w-4 h-4' />}
          <span>{showHeatmap ? '이동 빈도 OFF' : '이동 빈도 ON'}</span>
        </Button>
      </div>
      {/* 히트맵 범례 */}
      {showHeatmap && <HeatmapLegend />}
      <AMRLegend />
      <Canvas
        orthographic
        camera={{
          position: [40, 60, 40],
          zoom: 9,
        }}
      >
        <Map3D />
        <RoutePath />
        <Scene2DContent showHeatmap={showHeatmap} />
      </Canvas>
    </div>
  );
};

export default Scene2DViewer;
