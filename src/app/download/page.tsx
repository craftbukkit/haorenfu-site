import { DownloadCards, SiteHeader } from '@/components/sections';
import { GlassCard, SectionHeading } from '@/components/ui';

export default function DownloadPage() {
  return (
    <main>
      <SiteHeader />
      <section className='mx-auto max-w-6xl space-y-6 px-4 py-10'>
        <SectionHeading title='下载与安装说明' subtitle='完整中文客户端优先，减少模组不一致与缺失语言 key。' />
        <DownloadCards />
        <div className='grid gap-4 md:grid-cols-3'>
          <GlassCard><h3 className='font-semibold'>PCL 导入</h3><p className='mt-2 text-sm text-mech'>下载 ZIP 后选择“导入整合包”，等待索引完成再启动。</p></GlassCard>
          <GlassCard><h3 className='font-semibold'>HMCL 导入</h3><p className='mt-2 text-sm text-mech'>新建实例 → 导入整合包 → 指定 Java 17+。</p></GlassCard>
          <GlassCard><h3 className='font-semibold'>Prism 导入</h3><p className='mt-2 text-sm text-mech'>Add Instance → Import from zip，分配 6~8GB 内存。</p></GlassCard>
        </div>
      </section>
    </main>
  );
}
