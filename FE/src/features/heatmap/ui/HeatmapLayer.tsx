import { useMemo, useRef } from 'react';
import * as THREE from 'three';
import { useHeatmapData } from '../lib/useHeatmapData';

const WORLD_SIZE = 80;
const CANVAS_SIZE = 800;
const LINE_WIDTH = 10;
const MESH_Y_POSITION = 0.11;
const MESH_ROTATION = -Math.PI / 2;

interface Point {
  x: number;
  y: number;
}

function getHeatmapColor(intensity: number) {
  return `rgba(255,0,0,${intensity})`;
}

const convertToCanvasPoint = (x: number, y: number): Point => {
  return {
    x: (x / WORLD_SIZE) * CANVAS_SIZE,
    y: (y / WORLD_SIZE) * CANVAS_SIZE
  };
};

export const HeatmapLayer = () => {
  const meshRef = useRef<THREE.Mesh>(null);
  const heatmapData = useHeatmapData();

  const maxCount = useMemo(() => 
    Math.max(1, ...heatmapData.map(p => p.count))
  , [heatmapData]);

  const texture = useMemo(() => {
    const canvas = document.createElement('canvas');
    canvas.width = CANVAS_SIZE;
    canvas.height = CANVAS_SIZE;
    const ctx = canvas.getContext('2d')!;
    ctx.clearRect(0, 0, CANVAS_SIZE, CANVAS_SIZE);

    heatmapData.forEach(({ fromX, fromY, toX, toY, count }) => {
      const intensity = count / maxCount;
      
      const from = convertToCanvasPoint(fromX, fromY);
      const to = convertToCanvasPoint(toX, toY);

      ctx.save();
      ctx.beginPath();
      ctx.moveTo(from.x, from.y);
      ctx.lineTo(to.x, to.y);
      ctx.strokeStyle = getHeatmapColor(intensity);
      ctx.lineWidth = LINE_WIDTH;
      ctx.lineCap = 'round';
      ctx.stroke();
      ctx.restore();
    });

    const tex = new THREE.Texture(canvas);
    tex.needsUpdate = true;
    return tex;
  }, [heatmapData, maxCount]);

  return (
    <mesh 
      ref={meshRef} 
      position={[WORLD_SIZE / 2, MESH_Y_POSITION, WORLD_SIZE / 2]} 
      rotation={[MESH_ROTATION, 0, 0]} 
      scale={[WORLD_SIZE, WORLD_SIZE, 1]}
    >
      <planeGeometry args={[1, 1]} />
      <meshBasicMaterial map={texture} transparent opacity={0.6} />
    </mesh>
  );
}; 