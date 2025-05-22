import { useGLTF } from '@react-three/drei';
import React from 'react';

export default function MapPointer(props: { color: 'Red' | 'Blue'; position: number[] }) {
  const { color } = props;
  const { scene } = useGLTF(`/Pointer_${color}-v1.glb`);

  return <primitive object={scene} {...props} rotation={[0, -Math.PI / 4, 0]} autoRotate={true} />;
}
