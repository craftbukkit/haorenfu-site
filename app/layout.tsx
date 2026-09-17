import type { Metadata } from "next";
import "./globals.css";
import { SiteFooter } from "@/components/site/site-footer";
import { SiteHeader } from "@/components/site/site-header";

export const metadata: Metadata = {
  metadataBase: new URL("https://haorenfu.cn"),
  title: { default: "Minecraft 好人服务器｜Haorenfu Universe", template: "%s｜Minecraft 好人服务器" },
  description: "始于 2013 年的 Minecraft 长期多人世界。机械、铁路、航空、工业、群星、东方幻想、建筑与影像在这里共同生长。",
  applicationName: "好人服宇宙",
  keywords: ["Minecraft", "好人服", "Haorenfu Universe", "NeoForge", "Minecraft 服务器", "整合包"],
  icons: { icon: "/favicon.svg", shortcut: "/favicon.svg" },
  manifest: "/manifest.webmanifest",
};

export default function RootLayout({ children }: Readonly<{ children: React.ReactNode }>) {
  return <html lang="zh-CN"><body><a className="skip-link" href="#main-content">跳到主要内容</a><SiteHeader />{children}<SiteFooter /></body></html>;
}
