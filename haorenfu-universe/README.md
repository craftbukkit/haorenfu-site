# 🌌 Haorenfu Universe

<div align="center">

**一个全栈 Java 实现的 Minecraft 社区平台**

*Built with passion, powered by mathematics*

[![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2-6DB33F?style=for-the-badge&logo=spring&logoColor=white)](https://spring.io/projects/spring-boot)
[![Vaadin](https://img.shields.io/badge/Vaadin-24-00B4F0?style=for-the-badge&logo=vaadin&logoColor=white)](https://vaadin.com/)

</div>

---

## ✨ 特性

### 🎯 100% 纯 Java 实现
- **前端**: Vaadin Flow - 无需编写 JavaScript/CSS
- **后端**: Spring Boot 3.2 - 企业级框架
- **数据**: Spring Data JPA - 优雅的数据访问

### 🔬 前沿数学理论应用

| 算法 | 应用场景 | 数学基础 |
|------|----------|----------|
| **布隆过滤器** | 用户名快速查重 | 概率论、哈希函数 |
| **香农熵** | 密码强度分析 | 信息论 |
| **PageRank 变体** | 内容热度排名 | 图论、马尔可夫链 |
| **Wilson 置信区间** | 公平评分系统 | 贝叶斯推断 |
| **指数衰减** | 时间加权排序 | 微分方程 |

### 🎮 完整的 MC 服务器功能

```
📦 功能模块
├── 👤 用户系统
│   ├── 注册/登录
│   ├── 个人资料
│   ├── 角色权限
│   └── 声望等级
├── 💬 社区论坛
│   ├── 帖子发布
│   ├── 评论回复
│   ├── 投票系统
│   └── 热度排行
├── 🏆 成就系统
│   ├── 多种类别
│   ├── 稀有度分级
│   └── 声望奖励
├── 📊 排行榜
│   ├── 声望榜
│   ├── 发帖榜
│   └── 游戏时长
├── 🗳️ 投票系统
│   ├── 社区决策
│   └── 实时统计
├── 🗺️ 服务器地图
│   ├── 地标位置
│   └── 坐标搜索
├── 📖 百科系统
│   ├── 分类浏览
│   └── 版本历史
├── 🎉 活动系统
│   ├── 进行中活动
│   └── 进度追踪
└── ⚙️ 服务器状态
    ├── 实时监控
    └── 玩家列表
```

---

## 🚀 快速开始

### 环境要求

- Java 21+
- Maven 3.8+
- (可选) PostgreSQL 15+

### 开发模式

```bash
# 克隆项目
git clone https://github.com/your-org/haorenfu-universe.git
cd haorenfu-universe

# 运行 (使用 H2 内存数据库)
./mvnw spring-boot:run

# 访问
open http://localhost:8080
```

### 生产部署

```bash
# 构建生产包
./mvnw package -Pproduction

# 设置环境变量
export DATABASE_URL=jdbc:postgresql://localhost:5432/haorenfu
export DATABASE_USER=your_user
export DATABASE_PASSWORD=your_password
export MC_SERVER_HOST=your-mc-server.com

# 运行
java -jar target/haorenfu-universe-2.0.0-HORIZON.jar
```

---

## 📁 项目结构

```
haorenfu-universe/
├── src/main/java/world/haorenfu/
│   ├── HaorenfuUniverseApplication.java    # 应用入口
│   │
│   ├── core/                               # 核心模块
│   │   ├── algorithm/                      # 数学算法
│   │   │   ├── BloomFilter.java           # 布隆过滤器
│   │   │   ├── EntropyAnalyzer.java       # 熵分析器
│   │   │   └── PopularityEngine.java      # 热度引擎
│   │   ├── config/                         # 配置类
│   │   ├── security/                       # 安全模块
│   │   └── util/                           # 工具类
│   │
│   ├── domain/                             # 领域模型
│   │   ├── user/                           # 用户域
│   │   ├── forum/                          # 论坛域
│   │   ├── achievement/                    # 成就域
│   │   ├── server/                         # 服务器域
│   │   ├── vote/                           # 投票域
│   │   └── wiki/                           # 百科域
│   │
│   └── ui/                                 # 用户界面 (Vaadin)
│       ├── layout/                         # 布局组件
│       ├── view/                           # 页面视图
│       └── component/                      # 可复用组件
│
└── src/main/resources/
    └── application.properties              # 应用配置
```

---

## 🔬 核心算法详解

### 布隆过滤器 (BloomFilter)

用于高效的成员检测，在用户注册时快速检查用户名是否已存在。

```java
// 创建过滤器: 期望10000元素，1%误报率
BloomFilter<String> filter = new BloomFilter<>(10000, 0.01);

// 添加元素
filter.add("username");

// 查询 (O(1) 时间复杂度)
if (filter.mightContain("username")) {
    // 可能存在，需要进一步确认
}
```

**数学原理**:
- 误报概率: `p ≈ (1 - e^(-kn/m))^k`
- 最优哈希函数数: `k = (m/n) * ln(2)`

### 熵分析器 (EntropyAnalyzer)

基于香农信息论评估密码强度：

```java
PasswordStrength strength = EntropyAnalyzer.analyzePassword("MyP@ssw0rd!");
// 返回: 熵值、强度等级、改进建议
```

### 热度引擎 (PopularityEngine)

结合多种算法计算内容排名：

```java
double hotScore = PopularityEngine.calculateHotScore(
    upvotes, downvotes, createdAt, views
);

double wilsonScore = PopularityEngine.calculateWilsonScore(
    positiveVotes, totalVotes
);
```

---

## 🛠️ 配置说明

### 数据库配置

开发环境使用 H2，生产环境建议 PostgreSQL：

```properties
# PostgreSQL 配置
spring.datasource.url=jdbc:postgresql://localhost:5432/haorenfu
spring.datasource.username=your_user
spring.datasource.password=your_password
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

### Minecraft 服务器配置

```properties
app.minecraft.server.host=your-server.com
app.minecraft.server.port=25565
app.minecraft.status.check-interval=60000
```

---

## 🤝 贡献指南

我们欢迎各种形式的贡献！

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/amazing-feature`)
3. 提交更改 (`git commit -m 'Add amazing feature'`)
4. 推送分支 (`git push origin feature/amazing-feature`)
5. 发起 Pull Request

---

## 📜 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

---

## 🙏 致谢

- [Spring Boot](https://spring.io/projects/spring-boot) - 后端框架
- [Vaadin](https://vaadin.com/) - Java Web 框架
- [Minecraft](https://minecraft.net/) - 游戏本体

---

<div align="center">

**Made with ❤️ by the Haorenfu Community**

*"一个和谐的 Minecraft 基友服"*

</div>
