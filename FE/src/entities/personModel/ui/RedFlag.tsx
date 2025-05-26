import { useGLTF } from '@react-three/drei';

export const RedFlag = () => {
  const { scene } = useGLTF('/Flag_Red.glb');

  return <primitive position={[67, 1, 22]} rotation={[0, -Math.PI / 2, 0]} object={scene} />;
};

useGLTF.preload('/Flag_Red.glb');
