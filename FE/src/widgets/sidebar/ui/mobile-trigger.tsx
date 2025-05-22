'use client';

import { SidebarTrigger } from '@/components/ui/sidebar';
import { useIsMobile } from '@/shared/hooks/use-mobile';

export function MobileSidebarTrigger() {
  const isMobile = useIsMobile();

  return (
    <>
      {isMobile && (
        <div className='fixed z-50 top-2 left-2'>
          <SidebarTrigger />
        </div>
      )}
    </>
  );
}
