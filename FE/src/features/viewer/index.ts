'use client';

import dynamic from 'next/dynamic';

const Scene2DViewer = dynamic(() => import('./ui/Scene2DViewer'), {
  ssr: false,
});

const Scene3DViewer = dynamic(() => import('./ui/Scene3DViewer'), {
  ssr: false,
});

const ThumbnailViewer = dynamic(() => import('./ui/ThumbnailViewer'), {
  ssr: false,
});

export { Scene2DViewer, Scene3DViewer, ThumbnailViewer };
