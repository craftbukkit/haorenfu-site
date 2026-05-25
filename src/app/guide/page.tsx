import { NewPlayerTimeline, SiteHeader } from '@/components/sections';
import { GlassCard, SectionHeading } from '@/components/ui';

export default function GuidePage() {
  return (
    <main>
      <SiteHeader />
      <section className='mx-auto max-w-6xl space-y-6 px-4 py-10'>
        <SectionHeading title='新玩家完整指南' subtitle='Create 入门、任务路线、多人协作、性能友好建议。' />
        <NewPlayerTimeline />
        <div className='grid gap-4 md:grid-cols-2'>
          <GlassCard><h3 className='font-semibold'>Create 入门</h3><p className='mt-2 text-sm text-mech'>先掌握动力源、轴系与齿轮比，避免过早追求超大型工厂。</p></GlassCard>
          <GlassCard><h3 className='font-semibold'>任务书路线</h3><p className='mt-2 text-sm text-mech'>主线优先，支线按需推进，能稳定获得资源与目标感。</p></GlassCard>
          <GlassCard><h3 className='font-semibold'>多人协作建议</h3><p className='mt-2 text-sm text-mech'>公共仓储统一命名，公共工厂留维护文档与输入输出标识。</p></GlassCard>
          <GlassCard><h3 className='font-semibold'>性能友好玩法</h3><p className='mt-2 text-sm text-mech'>减少实体堆积，优先传送带与管道，集中处理掉落物回收。</p></GlassCard>
        </div>
      </section>
    </main>
  );
}
