'use client';

import { ControlPanel } from '@/features/control-panel';
import { VisualizationPanel } from '@/features/visualization';
import { SOCKET_URL } from '@/shared/constants/api';
import { useAmrSocketStore } from '@/shared/store/amrSocket';
import { Client } from '@stomp/stompjs';
import { useEffect, Suspense } from 'react';
import SockJS from 'sockjs-client';

const StompClientConfig = {
  connectHeaders: {},
  // debug: (str: string) => console.log(str),
  reconnectDelay: 5000,
  heartbeatIncoming: 4000,
  heartbeatOutgoing: 4000,
};

export default function ControlPage() {
  const { setAmrSocket, setIsConnected, reset } = useAmrSocketStore();

  useEffect(() => {
    const socket = new SockJS(SOCKET_URL + '/status');

    const client = new Client({
      ...StompClientConfig,
      webSocketFactory: () => socket,
      onConnect: () => {
        console.log('connected');
        setIsConnected(true);
      },
      onDisconnect: () => {
        console.log('disconnected');
        reset();
      },
      onStompError: (error) => {
        console.log('stomp error', error);
      },
    });

    client.activate();
    setAmrSocket(client);

    return () => {
      reset();
      client.deactivate();
      socket.close();
    };
  }, [setAmrSocket, setIsConnected, reset]);

  return (
    <Suspense fallback={<div>Loading...</div>}>
      <div className='flex h-screen'>
        <ControlPanel />
        <VisualizationPanel />
      </div>
    </Suspense>
  );
}
