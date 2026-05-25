'use client';

import { useEffect, useState } from 'react';
import { serverConfig } from '@/config/server';

type StatusResponse = {
  online: boolean;
  playersOnline: number | null;
  playersMax: number | null;
  version: string;
  address: string;
  whitelist: boolean;
  message: string;
};

export function useServerStatus() {
  const [status, setStatus] = useState<StatusResponse | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let mounted = true;
    const load = async () => {
      try {
        const res = await fetch(serverConfig.statusApiUrl, { cache: 'no-store' });
        if (!res.ok) throw new Error('status fetch failed');
        const data = (await res.json()) as StatusResponse;
        if (mounted) setStatus(data);
      } catch {
        if (mounted) setStatus(null);
      } finally {
        if (mounted) setLoading(false);
      }
    };
    void load();
    return () => {
      mounted = false;
    };
  }, []);

  return { status, loading };
}
