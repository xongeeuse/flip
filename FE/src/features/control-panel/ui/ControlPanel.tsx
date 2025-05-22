'use client';

import { Tabs, TabsList, TabsTrigger, TabsContent } from '@/components/ui/tabs';
import FacilityControl from './FacilityControl';
import AMRControl from './AMRControl';
import { useSelectedAMRStore } from '@/shared/store/selected-amr-store';

export const ControlPanel = () => {
  const { setSelectedAmrId } = useSelectedAMRStore();

  const handleResetSelectedAmr = () => {
    setSelectedAmrId(null);
  };

  return (
    <Tabs defaultValue='amr' className='min-w-[320px] w-1/4 h-dvh p-2'>
      <TabsList variant='control' className='flex justify-start w-full mb-2 bg-transparent'>
        <TabsTrigger variant='control' value='amr' className='px-6 text-lg cursor-pointer'>
          AMR
        </TabsTrigger>
        <TabsTrigger
          onClick={handleResetSelectedAmr}
          variant='control'
          value='equipment'
          className='px-6 text-lg cursor-pointer'
        >
          설비
        </TabsTrigger>
      </TabsList>
      <TabsContent value='amr' className='w-full h-full overflow-y-auto hide-scrollbar'>
        <AMRControl />
      </TabsContent>
      <TabsContent value='equipment' className='w-full h-full overflow-y-auto hide-scrollbar'>
        <FacilityControl />
      </TabsContent>
    </Tabs>
  );
};
