import { Sel4TechPanel, SiteHeader } from '@/components/sections';
import { GlassCard, SectionHeading } from '@/components/ui';

const roadmap = [
  { stage: '阶段 A（已完成）', detail: 'seL4 kernel 与 CAmkES VM 构建验证完成，镜像产出流程已打通。' },
  { stage: '阶段 B（进行中）', detail: 'Linux guest 运行时治理与可观测性增强（日志、资源约束、故障恢复）。' },
  { stage: '阶段 C（路线）', detail: 'JRE + Minecraft 服务进程迁移验证，分阶段压测与回滚策略设计。' }
];

export default function TechPage() {
  return (
    <main>
      <SiteHeader />
      <section className='mx-auto max-w-6xl space-y-6 px-4 py-10'>
        <Sel4TechPanel />
        <SectionHeading title='迁移路线图与边界说明' subtitle='强调工程实验属性，不夸大当前生产部署形态。' />
        <div className='grid gap-4 md:grid-cols-3'>
          {roadmap.map((item) => (
            <GlassCard key={item.stage}>
              <h3 className='font-semibold'>{item.stage}</h3>
              <p className='mt-2 text-sm text-mech'>{item.detail}</p>
            </GlassCard>
          ))}
        </div>
        <GlassCard className='text-sm text-mech'>
          生产 Minecraft 服务端当前仍以稳定运行优先。seL4/CAmkES 内容用于底层安全基础设施实验与迁移验证，不代表当前已在裸 seL4 上直接运行 Java Minecraft。
        </GlassCard>
      </section>
    </main>
  );
}
