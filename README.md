# Minecraft 好人服务器官网 / Haorenfu Universe

Minecraft 好人服务器（Haorenfu Universe）公开官网源码。服务器始于 2013 年 5 月；本仓库整理自 2026-09-04 的官网开发基线，并在公开前移除了生产环境部署拓扑、内部托管标识和凭据相关内容。

## 内容

- 首页与八条玩法路线
- 56 项可审计模组/组件百科
- 客户端加入与版本边界说明
- 社区、历史、规则、隐私、安全与状态页面
- Minecraft Java 1.21.1 / NeoForge 21.1.248 基线资料

## 本地运行

需要 Node.js 22 或更新版本。

```bash
npm install
npm run dev
```

生产静态导出：

```bash
npm run build
```

输出目录为 `out/`。

## 开源边界

本仓库只公开网站应用本身。真实生产 `.env`、Token、密码、SSH 私钥、Tailscale/Zero-Trust 配置、云平台凭据、备份凭据、内部管理地址与生产部署拓扑均不应进入公开仓库。

页面中的 Minecraft、模组和第三方项目名称及商标归各自权利人所有。此项目不是 Mojang Studios 官方产品。

## 历史版本

2026-09-17 公开前的旧 GitHub 版本保存在 `archive/pre-2026-09-17` 分支。

## License

网站源代码以 MIT License 发布。第三方资产、商标和各 Minecraft 模组仍遵循各自许可证。
