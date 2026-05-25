'use client';

import { motion, useReducedMotion } from 'framer-motion';
import { cn } from '@/lib/utils';

export function SectionHeading({ title, subtitle }: { title: string; subtitle?: string }) {
  return (
    <div className='mb-6'>
      <h2 className='text-2xl font-bold md:text-3xl'>{title}</h2>
      {subtitle ? <p className='mt-2 max-w-3xl text-sm text-mech md:text-base'>{subtitle}</p> : null}
    </div>
  );
}

export function GlassCard({ className, children }: { className?: string; children: React.ReactNode }) {
  return <div className={cn('glass hud-border rounded-2xl p-5', className)}>{children}</div>;
}

export function HudBadge({ label }: { label: string }) {
  return <span className='rounded border border-arc/45 bg-bg0/60 px-2 py-1 font-mono text-xs text-arc'>{label}</span>;
}

export function Reveal({ children }: { children: React.ReactNode }) {
  const reduce = useReducedMotion();
  return (
    <motion.div
      initial={reduce ? false : { opacity: 0, y: 20 }}
      whileInView={reduce ? {} : { opacity: 1, y: 0 }}
      viewport={{ once: true, amount: 0.2 }}
      transition={{ duration: 0.5 }}
    >
      {children}
    </motion.div>
  );
}
