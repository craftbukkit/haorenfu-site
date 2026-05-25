'use client';

import { useEffect, useState } from 'react';

const KEY = 'haorenfu-lowfx';

export function useLowFxMode() {
  const [lowFx, setLowFx] = useState(false);

  useEffect(() => {
    const saved = localStorage.getItem(KEY);
    if (saved === '1') setLowFx(true);
  }, []);

  useEffect(() => {
    localStorage.setItem(KEY, lowFx ? '1' : '0');
    document.documentElement.dataset.lowfx = lowFx ? '1' : '0';
  }, [lowFx]);

  return { lowFx, setLowFx };
}
