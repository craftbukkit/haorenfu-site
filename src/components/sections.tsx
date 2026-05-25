'use client';

import Link from 'next/link';
import { useMemo, useState } from 'react';
import { useServerStatus } from './status-client';
import { useLowFxMode } from './performance-mode';
import { AnimatePresence, motion, useReducedMotion } from 'framer-motion';
import { ChevronDown, Copy, Download, ExternalLink, Server } from 'lucide-react';
import { serverConfig } from '@/config/server';
import { GlassCard, HudBadge, Reveal, SectionHeading } from './ui';

export function SiteHeader() {
  return (
    <header className='sticky top-0 z-30 border-b border-white/10 bg-bg0/80 backdrop-blur'>
      <div className='mx-auto flex max-w-6xl flex-col gap-2 px-4 py-3 sm:flex-row sm:items-center sm:justify-between'>
        <b>{serverConfig.serverName}</b>
        <nav className='flex w-full flex-wrap gap-x-4 gap-y-1 text-sm text-mech sm:w-auto'>
          <Link href='/download'>下载</Link><Link href='/guide'>指南</Link><Link href='/tech'>技术</Link><Link href='/rules'>规则</Link>
        </nav>
      </div>
    </header>
  );
}

export function ParticleField() {
  return <div className='particle-field' aria-hidden />;
}

export function GearScene3D() {
  return <div className='absolute inset-0 -z-10 opacity-60 bg-[radial-gradient(circle_at_20%_20%,rgba(71,214,180,.15),transparent_45%),radial-gradient(circle_at_80%_30%,rgba(74,168,255,.2),transparent_35%)]'><ParticleField /><div className='gear g1'/><div className='gear g2'/></div>;
}

export function HeroSection() {
  const [copied, setCopied] = useState(false);
  const reduce = useReducedMotion();
  const { lowFx, setLowFx } = useLowFxMode();
  return (
    <section className='relative overflow-hidden px-4 py-20 md:py-28'>
      {!lowFx ? <GearScene3D /> : null}
      <div className='mx-auto max-w-6xl'>
        <HudBadge label='CREATE × NeoForge × seL4 route' />
        <h1 className='mt-4 text-3xl font-bold leading-tight sm:text-4xl md:text-6xl'>{serverConfig.serverName}</h1>
        <p className='mt-4 max-w-3xl text-mech'>以 Create 工程、空天载具、自动化工厂与 seL4 微内核技术路线为特色的现代科技整合包服务器。</p>
        <div className='mt-5 flex flex-wrap gap-2 text-sm'><HudBadge label={`服务器 ${serverConfig.domain}`} /><HudBadge label={`QQ群 ${serverConfig.qqGroup}`} /><HudBadge label='白名单 开启' /><HudBadge label='中文客户端 v2' /></div>
        <div className='mt-8 grid w-full gap-3 sm:flex sm:flex-wrap'>
          <motion.button whileTap={reduce ? {} : { scale: 0.96 }} onClick={async()=>{await navigator.clipboard.writeText(serverConfig.domain);setCopied(true);setTimeout(()=>setCopied(false),1200);}} className='rounded-lg bg-arc px-4 py-2 text-left font-semibold text-bg0'>
            <Copy className='mr-1 inline h-4 w-4'/>复制服务器地址
          </motion.button>
          <a href={`https://qm.qq.com/q/${serverConfig.qqGroup}`} className='rounded-lg border border-white/20 px-4 py-2 text-left'>加入 QQ 群 <ExternalLink className='inline h-4 w-4'/></a>
          <Link href='/download' className='rounded-lg border border-white/20 px-4 py-2 text-left'>下载完整中文客户端 <Download className='inline h-4 w-4'/></Link>
          <Link href='/guide' className='rounded-lg border border-white/20 px-4 py-2 text-left'>查看新手指南</Link>
          <button onClick={()=>setLowFx(!lowFx)} className='rounded-lg border border-white/20 px-4 py-2 text-left text-sm'>{lowFx?'开启动效':'低配模式'}</button>
        </div>
        <AnimatePresence>{copied ? <motion.p initial={{opacity:0,y:6}} animate={{opacity:1,y:0}} exit={{opacity:0}} className='mt-3 text-sm text-verdigris'>HUD: 地址已复制，可直接在启动器中使用。</motion.p>:null}</AnimatePresence>
      </div>
    </section>
  );
}

export function ServerStatusCard(){const {status,loading}=useServerStatus();const rows=[['状态',loading?'检测中':status?.online?'在线':'离线 / 未知'],['地址',status?.address??serverConfig.domain],['备用地址',serverConfig.fallbackAddress],['版本',status?.version??`${serverConfig.minecraftVersion} / ${serverConfig.loader}`],['加载器',serverConfig.loader],['白名单',status?.whitelist?'开启':'开启'],['在线人数',status?.playersOnline!=null&&status?.playersMax!=null?`${status.playersOnline}/${status.playersMax}`:'-- / --'],['中文客户端','v2 已完成'],['seL4','CAmkES VM 构建验证完成']];return <Reveal><GlassCard><SectionHeading title='在线状态卡片' subtitle='静态兜底 + API 预留，不因接口波动影响可读性。' /><div className='grid gap-2 text-sm md:grid-cols-2'>{rows.map(([k,v])=><p key={k}><span className='text-mech'>{k}：</span>{v}</p>)}</div></GlassCard></Reveal>}

export function EngineeringShowcase(){return <Reveal><SectionHeading title='工程玩法展示' subtitle='多模块协同推进，从机械动力到空天载具，再到服务器级自动仓储。' /><div className='grid gap-4 md:grid-cols-2'>{serverConfig.features.map((f,i)=><GlassCard key={f.title} className='relative overflow-hidden'><span className='text-xs text-arc/80'>0{i+1}</span><h3 className='mt-1 font-semibold'>{f.title}</h3><p className='mt-2 text-sm text-mech'>{f.detail}</p></GlassCard>)}</div></Reveal>}

export function NewPlayerTimeline(){return <Reveal><SectionHeading title='从第一根传动轴开始' subtitle='新玩家推荐路线，避免前期信息过载。' /><ol className='space-y-3'>{serverConfig.timeline.map((item,index)=><li key={item} className='relative rounded-xl border border-white/15 p-4 pl-10 sm:pl-12'><span className='absolute left-4 top-4 font-mono text-verdigris'>{index}</span>{item}</li>)}</ol></Reveal>}

export function DownloadCards(){return <Reveal><SectionHeading title='下载区' subtitle='链接集中配置，后续替换只需改 src/config/server.ts。' /><div className='grid gap-4 md:grid-cols-2'><GlassCard><h3 className='font-semibold'>完整中文客户端 v2</h3><p className='mt-2 text-sm text-mech'>适合新玩家，支持 PCL / HMCL / Prism 导入。</p><a href={serverConfig.clientZipUrl} className='mt-3 inline-flex text-arc underline'>下载 ZIP</a><p className='mt-3 text-xs text-mech'>校验建议：下载后比对 SHA256，避免损坏包导致启动失败。</p></GlassCard><GlassCard><h3 className='font-semibold'>单独中文资源包 v2</h3><p className='mt-2 text-sm text-mech'>适合已有客户端玩家。</p><a href={serverConfig.resourcepackUrl} className='mt-3 inline-flex text-arc underline'>下载资源包</a><p className='mt-3 text-xs text-mech'>常见问题：版本不一致、资源冲突、语言未切换为简体中文。</p></GlassCard></div></Reveal>}

export function TerminalBlock(){const lines=['$ repo init sel4-camkes-vm','[ok] seL4 kernel image generated','[ok] CAmkES VM image generated','[todo] Linux guest runtime hardening','[todo] Minecraft service migration validation'];return <div className='mt-4 rounded-lg border border-arc/25 bg-black/40 p-3 font-mono text-xs text-verdigris'>{lines.map((line)=><p key={line} className='leading-6'>{line}</p>)}</div>}

export function Sel4TechPanel(){const layers=useMemo(()=>['seL4 Microkernel','CAmkES VMM','Linux Guest','Java Runtime','Minecraft Server'],[]);return <Reveal><SectionHeading title='不仅是模组服，也是底层系统工程实验' subtitle='当前生产服以稳定运行为优先；seL4 路线用于安全基础设施实验与迁移验证。' /><GlassCard><div className='mb-3 flex flex-wrap gap-2'><HudBadge label='seL4 Build: DONE'/><HudBadge label='CAmkES VM: DONE'/><HudBadge label='Linux Guest Runtime: Roadmap'/><HudBadge label='Minecraft Migration: Roadmap'/></div><p className='text-sm text-mech'>技术路线：seL4 → CAmkES VMM → Linux guest → JRE → Minecraft。已完成 seL4/CAmkES VM 构建验证与镜像产出，不夸大为当前裸跑。</p><div className='mt-4 grid gap-2 md:grid-cols-5'>{layers.map((l)=><div key={l} className='rounded border border-verdigris/30 p-2 text-center text-xs'>{l}</div>)}</div><TerminalBlock /></GlassCard></Reveal>}

export function RulesSection(){return <Reveal><SectionHeading title='服务器规则' /><ul className='list-disc space-y-2 pl-6 text-sm'>{serverConfig.rules.map((x)=><li key={x}>{x}</li>)}</ul></Reveal>}

export function FaqAccordion(){const [open,setOpen]=useState(0);return <Reveal><SectionHeading title='FAQ' /><div className='space-y-3'>{serverConfig.faq.map((item,idx)=>{const active=open===idx;return <GlassCard key={item.q}><button aria-expanded={active} aria-controls={`faq-panel-${idx}`} className='flex w-full items-center justify-between text-left font-semibold focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-arc/70' onClick={()=>setOpen(active?-1:idx)}>{item.q}<ChevronDown className={`h-4 w-4 transition ${active?'rotate-180':''}`}/></button><AnimatePresence>{active?<motion.p id={`faq-panel-${idx}`} initial={{height:0,opacity:0}} animate={{height:'auto',opacity:1}} exit={{height:0,opacity:0}} className='mt-2 overflow-hidden text-sm text-mech'>{item.a}</motion.p>:null}</AnimatePresence></GlassCard>})}</div></Reveal>}

export function SiteFooter(){return <footer className='border-t border-white/10 px-4 py-8 text-center text-sm text-mech'><Server className='mr-2 inline h-4 w-4'/>Minecraft好人服 1.21.1 · haorenfu.cn · QQ群：302805107 · 非 Mojang / Microsoft 官方项目</footer>}
