import { RulesSection, SiteHeader } from '@/components/sections';
import { GlassCard, SectionHeading } from '@/components/ui';
import { serverConfig } from '@/config/server';

export default function RulesPage() {
  return (
    <main>
      <SiteHeader />
      <section className='mx-auto max-w-6xl space-y-6 px-4 py-10'>
        <RulesSection />
        <SectionHeading title='白名单说明' subtitle='加入流程透明，便于新玩家快速上手。' />
        <GlassCard className='text-sm text-mech'>
          先加入 QQ 群 <span className='font-mono text-arc'>{serverConfig.qqGroup}</span>，按群公告模板提交申请信息。审核通过后可使用 <span className='font-mono text-arc'>{serverConfig.domain}</span> 进入服务器。
        </GlassCard>
        <SectionHeading title='玩家协作准则' subtitle='围绕公共工程可持续运行设计。' />
        <div className='grid gap-4 md:grid-cols-2'>
          <GlassCard><h3 className='font-semibold'>公共项目优先沟通</h3><p className='mt-2 text-sm text-mech'>涉及主干交通、公共仓储、空港等项目，先沟通规划再施工。</p></GlassCard>
          <GlassCard><h3 className='font-semibold'>大型机器分级上线</h3><p className='mt-2 text-sm text-mech'>先在测试区验证，再逐步迁移到主区，避免 TPS 波动影响全服。</p></GlassCard>
        </div>
      </section>
    </main>
  );
}
