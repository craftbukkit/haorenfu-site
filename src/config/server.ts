export type FaqItem = { q: string; a: string };

export const serverConfig = {
  serverName: 'Minecraft好人服 1.21.1',
  minecraftVersion: 'Minecraft 1.21.1',
  loader: 'NeoForge',
  domain: 'haorenfu.cn',
  fallbackAddress: '1.94.195.52:25565',
  qqGroup: '302805107',
  modpackName: 'Create Ultimate Selection 2 风格现代工程科技整合包',
  clientZipUrl: '/downloads/minecraft好人服202605-1.21.1-full-client-zh-v2.zip',
  resourcepackUrl: '/downloads/minecraft好人服202605-1.21.1-zh-resourcepack-v2.zip',
  statusApiUrl: '/api/status',
  sel4Status: 'CAmkES VM 构建验证完成，后续迁移路线进行中',
  features: [
    {
      title: 'Create 机械动力',
      detail: '齿轮、轴、传送带、机械臂、压机、搅拌器与风扇构建完整自动化产线。'
    },
    { title: 'Create Aeronautics / 载具', detail: '飞艇、飞机、移动工厂与空中平台形成工程机动能力。' },
    { title: 'AE2 数字仓储', detail: 'ME 网络、自动合成与服务器级物资管理体系。' },
    { title: 'Mekanism 科技链', detail: '能源、矿物处理与多级机器链协同推进。' },
    { title: 'FTB Quests 中文任务线', detail: '新手可按任务线循序推进，不迷路。' },
    { title: '多人协作工程', detail: '公共仓库、交通网络、空港与长期公共项目。' }
  ],
  timeline: [
    '第 0 步：下载完整中文客户端',
    '第 1 步：加入 QQ 群 302805107，申请白名单',
    '第 2 步：使用 haorenfu.cn 进入服务器',
    '第 3 步：阅读任务书并完成基础生存',
    '第 4 步：学习 Create 旋转动力与基础自动化',
    '第 5 步：进入 AE2 / Mekanism / 飞行器 / 大型工厂'
  ],
  rules: [
    '尊重公共设施，不乱拆、不偷盗。',
    '大型机器注意 TPS，不制造无限掉落物与实体堆积。',
    '飞行器和移动平台先在测试区调试。',
    '遵守白名单和 QQ 群管理规范。',
    '遇到未汉化文本或客户端问题可及时反馈。',
    '禁止外挂、恶意破坏、恶意卡服。'
  ],
  faq: [
    { q: '怎么进入服务器？', a: '下载完整中文客户端，进群申请白名单后，使用 haorenfu.cn 进入。' },
    { q: '为什么建议完整中文客户端？', a: '已完成大规模二次汉化与依赖校验，能明显减少缺字串与模组不一致问题。' },
    { q: '服务器地址是什么？', a: '主地址 haorenfu.cn，备用地址 1.94.195.52:25565。' },
    { q: 'QQ群是多少？', a: '302805107。' },
    { q: '白名单怎么申请？', a: '进入 QQ 群后按管理指引申请，审核后可进入服务器。' },
    { q: 'seL4 是什么？是不是已裸跑？', a: '当前是 seL4/CAmkES 构建验证与基础设施实验，不是生产服裸跑。' },
    { q: '模组不一致怎么办？', a: '优先使用完整中文客户端，检查 Java 版本、整合包版本与资源包版本。' },
    { q: '低配电脑能玩吗？', a: '可以。建议中低画质、降低视距，先进行中小规模自动化。' },
    { q: '如何反馈 BUG？', a: '在 QQ 群按模板反馈日志、复现步骤与截图。' }
  ] satisfies FaqItem[]
};
