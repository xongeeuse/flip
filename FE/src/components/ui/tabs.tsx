'use client';

import * as React from 'react';
import * as TabsPrimitive from '@radix-ui/react-tabs';
import { cn } from '@/lib/utils';
import { cva } from 'class-variance-authority';

interface TabsListProps extends React.ComponentProps<typeof TabsPrimitive.List> {
  variant?: 'default' | 'control';
}

interface TabsTriggerProps extends React.ComponentProps<typeof TabsPrimitive.Trigger> {
  variant?: 'default' | 'control';
}

interface TabsContentProps extends React.ComponentProps<typeof TabsPrimitive.Content> {
  variant?: 'default' | 'control';
}

const tabsListVariants = cva('', {
  variants: {
    variant: {
      default:
        'bg-muted text-muted-foreground inline-flex h-9 w-fit items-center justify-center rounded-lg p-[3px]',
      control:
        'inline-flex w-full items-center justify-start gap-0 border-b-2 border-border p-0 text-muted-foreground',
    },
  },
  defaultVariants: {
    variant: 'default',
  },
});

const tabsTriggerVariants = cva('', {
  variants: {
    variant: {
      default:
        "data-[state=active]:bg-background dark:data-[state=active]:text-foreground focus-visible:border-ring focus-visible:ring-ring/50 focus-visible:outline-ring dark:data-[state=active]:border-input dark:data-[state=active]:bg-input/30 text-foreground dark:text-muted-foreground inline-flex h-[calc(100%-1px)] flex-1 items-center justify-center gap-1.5 rounded-md border border-transparent px-2 py-1 text-sm font-medium whitespace-nowrap transition-[color,box-shadow] focus-visible:ring-[3px] focus-visible:outline-1 disabled:pointer-events-none disabled:opacity-50 data-[state=active]:shadow-sm [&_svg:not([class*='size-'])]:size-4",
      control:
        'w-full mx-2 -mb-[2px] inline-flex items-center justify-center whitespace-nowrap border-b-2 px-2 py-2 text-base font-medium transition-all first-of-type:ml-0 disabled:pointer-events-none disabled:text-muted-foreground data-[state=active]:border-primary data-[state=inactive]:border-transparent data-[state=active]:font-semibold data-[state=active]:text-foreground',
    },
  },
  defaultVariants: {
    variant: 'default',
  },
});

const tabsContentVariants = cva('', {
  variants: {
    variant: {
      default: 'flex-1 outline-none',
      control:
        'mt-2 ring-offset-background focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2',
    },
  },
  defaultVariants: {
    variant: 'default',
  },
});

function Tabs({ className, ...props }: React.ComponentProps<typeof TabsPrimitive.Root>) {
  return (
    <TabsPrimitive.Root
      data-slot='tabs'
      className={cn('flex flex-col gap-2', className)}
      {...props}
    />
  );
}

function TabsList({ className, variant = 'default', ...props }: TabsListProps) {
  return (
    <TabsPrimitive.List
      data-slot='tabs-list'
      className={cn(tabsListVariants({ variant }), className)}
      {...props}
    />
  );
}

function TabsTrigger({ className, variant = 'default', ...props }: TabsTriggerProps) {
  return (
    <TabsPrimitive.Trigger
      data-slot='tabs-trigger'
      className={cn(tabsTriggerVariants({ variant }), className)}
      {...props}
    />
  );
}

function TabsContent({ className, variant = 'default', ...props }: TabsContentProps) {
  return (
    <TabsPrimitive.Content
      data-slot='tabs-content'
      className={cn(tabsContentVariants({ variant }), className)}
      {...props}
    />
  );
}

export { Tabs, TabsList, TabsTrigger, TabsContent };
