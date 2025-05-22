import { Home, Inbox } from 'lucide-react';

import {
  Sidebar,
  SidebarContent,
  SidebarGroup,
  SidebarGroupContent,
  SidebarHeader,
  SidebarInset,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
  SidebarSeparator,
  SidebarTrigger,
} from '@/components/ui/sidebar';

import { type SidebarItem } from '../model/types';
import Link from 'next/link';
import { Button } from '@/components/ui/button';

const items: SidebarItem[] = [
  {
    title: 'Home',
    url: '/',
    icon: Home,
  },
  {
    title: 'AMR 관제',
    url: '/control',
    icon: Inbox,
  },
];

export function AppSidebar() {
  return (
    <Sidebar collapsible='icon'>
      <SidebarInset>
        <SidebarContent>
          <SidebarGroup>
            {/* Logo Section */}
            <SidebarHeader className="flex justify-between px-1.5">
              {/* <div className="flex items-center">
                <span className="text-xl font-bold text-white">FLIP</span>
              </div> */}
              <SidebarTrigger className='cursor-pointer' />
            </SidebarHeader>
            <SidebarSeparator className='my-4' />
            
            {/* Menu Section */}
            <SidebarGroupContent>
              <SidebarMenu className='flex flex-col'>
                {items.map((item) => (
                  <SidebarMenuItem key={item.title}>
                    <SidebarMenuButton asChild>
                      <Link href={item.url} className='flex items-center py-6 group-data-[collapsible=icon]:!py-6'>
                        <Button variant="ghost" className='px-1 cursor-pointer'>
                          <item.icon className='size-5'/>
                        </Button>
                        <span>{item.title}</span>
                      </Link>
                    </SidebarMenuButton>
                  </SidebarMenuItem>
                ))}
              </SidebarMenu>
            </SidebarGroupContent>
          </SidebarGroup>
        </SidebarContent>
      </SidebarInset>
    </Sidebar>
  );
}