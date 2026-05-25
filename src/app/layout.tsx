import './globals.css';import type { Metadata } from 'next';
export const metadata: Metadata = { title: 'Minecraft好人服 1.21.1', description: 'Create工程科技服务器官网' };
export default function RootLayout({children}:{children:React.ReactNode}){return <html lang='zh-CN'><body>{children}</body></html>}
