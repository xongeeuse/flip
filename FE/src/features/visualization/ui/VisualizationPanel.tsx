'use client';

import { Tabs, TabsList, TabsTrigger, TabsContent } from '@/components/ui/tabs';
import { Loading } from '@/components/ui/loading';
import { Suspense, useEffect } from 'react';
import { useModelStore } from '@/shared/model/store';
import { Scene2DViewer, Scene3DViewer } from '@/features/viewer';
import { AMR_CURRENT_STATE } from '../model/types';
import { degreeToRadian } from '@/shared/hooks/useMathUtils';
import { useAmrSocketStore } from '@/shared/store/amrSocket';
import { useLineStore } from '@/shared/store/lineStore';
import { useSearchParams } from 'next/navigation';

export const VisualizationPanel = () => {
  const { updateModels } = useModelStore();
  const { setLines } = useLineStore();
  const { amrSocket, isConnected } = useAmrSocketStore();
  const searchParams = useSearchParams();
  const initialView = searchParams.get('view') || '3d';

  useEffect(() => {
    if (!isConnected || !amrSocket) return;

    amrSocket.subscribe('/amr/real-time', (message) => {
      const data = JSON.parse(message.body).amrRealTimeList;
      updateModels(data.map((d: AMR_CURRENT_STATE) => ({ ...d, dir: degreeToRadian(d.dir) })));
    });

    amrSocket.subscribe('/amr/line', (message) => {
      const data = JSON.parse(message.body);
      setLines(data.lineList);
    });
  }, [isConnected, amrSocket, updateModels, setLines]);

  return (
    <div className='w-3/4 p-4 h-dvh'>
      <div className='h-full'>
        <Tabs defaultValue={initialView} className='w-full h-full'>
          <TabsList className='grid w-full grid-cols-2 cursor-pointer bg-white/10'>
            <TabsTrigger className='cursor-pointer' value='3d'>
              공장 전체 View
            </TabsTrigger>
            <TabsTrigger className='cursor-pointer' value='2d'>
              Top View
            </TabsTrigger>
          </TabsList>
          <div className='h-full mt-4 cursor-grab'>
            <Suspense fallback={<Loading />}>
              <TabsContent value='3d' className='h-full m-0 data-[state=active]:block'>
                <Scene3DViewer />
              </TabsContent>
              <TabsContent value='2d' className='h-full m-0 data-[state=active]:block'>
                <Scene2DViewer />
              </TabsContent>
            </Suspense>
          </div>
        </Tabs>
      </div>
    </div>
  );
};
