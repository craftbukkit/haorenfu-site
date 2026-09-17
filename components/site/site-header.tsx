import Link from "next/link";
import { Sparkles } from "lucide-react";
import { navigation } from "@/lib/site-data";
export function SiteHeader(){return <header className="site-header"><div className="site-shell header-inner"><Link className="brand" href="/"><span className="brand-mark"><Sparkles/></span><span><strong>好人服宇宙</strong><small>HAORENFU UNIVERSE</small></span></Link><nav className="desktop-nav">{navigation.map(i=><Link className="nav-link" href={i.href} key={i.href}>{i.label}</Link>)}</nav><Link className="primary-button header-join" href="/download">立即加入</Link></div></header>}
