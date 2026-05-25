# Minecraft好人服 1.21.1 官网（最终版实现）

## 项目介绍
这是一个面向 Minecraft 工程科技玩家的现代化门户站，主题围绕 Create 机械工程、多人协作工厂的服务器。

## 技术栈
- Next.js App Router + TypeScript
- Tailwind CSS
- Motion for React（framer-motion）
- lucide-react
- 静态导出部署（Nginx/Vercel/Cloudflare Pages）

## 本地运行
```bash
npm install
npm run dev
```

## 构建
```bash
npm run build
```

## 部署
- Nginx 静态托管：`npm run build` 后部署 `out/`
- Vercel：直接连接仓库，自动构建
- Cloudflare Pages：构建命令 `npm run build`，输出目录 `out`

## 修改服务器信息的位置
集中在：`src/config/server.ts`

## 如何替换下载链接
编辑：
- `clientZipUrl`
- `resourcepackUrl`

## 如何接入真实 Minecraft 状态 API
当前使用 `/api/status` mock 数据。后续可在 `src/app/api/status/route.ts` 替换为：
1. 服务端 ping/代理接口（加超时）；
2. 缓存最近一次成功结果；
3. 失败时回退静态兜底值。

## Three.js / 重视觉性能开销建议
- 低端设备降级到静态背景；
- 减少粒子数量与复杂几何；
- 限制 dpr；
- 遵循 `prefers-reduced-motion`；
- 对首屏效果做 progressive enhancement。

## 上线前检查清单
- 核验 `src/config/server.ts` 中服务器地址、QQ群、下载链接是否为最新。
- 验证 `/download`、`/guide`、`/rules`、`/tech` 路由在移动端/桌面端展示正常。
- 检查低配模式开关与 reduced-motion 降级是否生效。
- 若接入真实状态 API，验证接口超时回退与兜底文案是否可用。
- 执行 `npm run build` 并检查静态导出产物。


## 壁纸放置位置
如果后续需要启用自定义壁纸，请将文件放到：`public/images/hero-wallpaper.png`（桌面）和 `public/images/hero-wallpaper-mobile.jpg`（移动端），再在 `src/app/globals.css` 的 `body` 背景中引用。
