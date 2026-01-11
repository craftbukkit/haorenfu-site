# 好人服务器官网完整部署教程

本教程将手把手教你如何在Linux服务器上部署好人服务器官网，包含背景图片配置。

---

## 目录

1. [服务器准备](#1-服务器准备)
2. [系统初始化](#2-系统初始化)
3. [安装Java 21](#3-安装java-21)
4. [安装Maven](#4-安装maven)
5. [上传并构建项目](#5-上传并构建项目)
6. [配置背景图片](#6-配置背景图片)（重要！）
7. [创建系统服务](#7-创建系统服务)
8. [安装Nginx](#8-安装nginx)
9. [配置Nginx反向代理](#9-配置nginx反向代理)
10. [申请SSL证书](#10-申请ssl证书)
11. [配置防火墙](#11-配置防火墙)
12. [域名解析](#12-域名解析)
13. [运维管理](#13-运维管理)
14. [故障排查](#14-故障排查)

---

## 1. 服务器准备

### 1.1 服务器配置要求

| 项目 | 最低配置 | 推荐配置 |
|------|---------|---------|
| CPU | 1核 | 2核+ |
| 内存 | 2GB | 4GB+ |
| 硬盘 | 20GB SSD | 50GB SSD |
| 带宽 | 5Mbps | 10Mbps+ |
| 系统 | Ubuntu 20.04+ | Ubuntu 22.04/24.04 |

### 1.2 购买服务器

推荐云服务商：阿里云 ECS、腾讯云 CVM、华为云 ECS

购买后记录：
- 服务器公网IP
- root密码或SSH密钥
- 安全组规则（需开放80、443端口）

---

## 2. 系统初始化

### 2.1 登录服务器

```bash
ssh root@你的服务器IP
```

### 2.2 更新系统

```bash
apt update && apt upgrade -y
```

### 2.3 安装常用工具

```bash
apt install -y curl wget vim git unzip htop net-tools
```

### 2.4 创建运行用户

```bash
useradd -m -s /bin/bash mcweb
passwd mcweb
usermod -aG sudo mcweb
```

### 2.5 设置时区

```bash
timedatectl set-timezone Asia/Shanghai
```

---

## 3. 安装Java 21

```bash
apt install -y openjdk-21-jdk
java -version
```

配置JAVA_HOME：
```bash
echo 'JAVA_HOME="/usr/lib/jvm/java-21-openjdk-amd64"' >> /etc/environment
source /etc/environment
```

---

## 4. 安装Maven

```bash
apt install -y maven
mvn -version
```

配置阿里云镜像加速：
```bash
mkdir -p /home/mcweb/.m2

cat > /home/mcweb/.m2/settings.xml << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<settings>
    <mirrors>
        <mirror>
            <id>aliyun</id>
            <mirrorOf>*</mirrorOf>
            <url>https://maven.aliyun.com/repository/public</url>
        </mirror>
    </mirrors>
</settings>
EOF

chown -R mcweb:mcweb /home/mcweb/.m2
```

---

## 5. 上传并构建项目

### 5.1 创建项目目录

```bash
mkdir -p /opt/mcweb
chown mcweb:mcweb /opt/mcweb
```

### 5.2 上传项目文件

使用SCP上传：
```bash
scp haoren-mc-website-v1.0.0.zip root@服务器IP:/opt/mcweb/
```

### 5.3 解压项目

```bash
cd /opt/mcweb
unzip haoren-mc-website-v1.0.0.zip
cd minecraft-server-website
```

### 5.4 构建项目

```bash
su - mcweb
cd /opt/mcweb/minecraft-server-website
./mvnw clean package -Pproduction -DskipTests
```

---

## 6. 配置背景图片

**这是非常重要的一步！** 网站需要配置背景图片才能正常显示。

### 6.1 背景图片文件说明

项目需要以下背景图片：

| 文件名 | 用途 | 建议尺寸 | 格式 |
|--------|------|---------|------|
| `bg-desktop.png` | 电脑版背景 | 1920×1080 或更大 | PNG/JPG |
| `bg-mobile.jpg` | 手机版背景 | 1080×1920 或更大 | JPG/PNG |
| `qq-qrcode.jpg` | QQ群二维码 | 300×300 | JPG/PNG/WebP |

### 6.2 图片存放位置

背景图片必须放在以下目录：
```
/opt/mcweb/minecraft-server-website/src/main/resources/META-INF/resources/images/
```

### 6.3 上传背景图片

**方法一：通过SCP上传**

```bash
# 在本地电脑执行
scp bg-desktop.png root@服务器IP:/opt/mcweb/minecraft-server-website/src/main/resources/META-INF/resources/images/
scp bg-mobile.jpg root@服务器IP:/opt/mcweb/minecraft-server-website/src/main/resources/META-INF/resources/images/
scp qq-qrcode.jpg root@服务器IP:/opt/mcweb/minecraft-server-website/src/main/resources/META-INF/resources/images/
```

**方法二：通过SFTP上传**

使用FileZilla等SFTP客户端，将图片上传到：
```
/opt/mcweb/minecraft-server-website/src/main/resources/META-INF/resources/images/
```

### 6.4 确认图片已上传

```bash
ls -la /opt/mcweb/minecraft-server-website/src/main/resources/META-INF/resources/images/
```

应该看到：
```
-rw-r--r-- 1 mcweb mcweb  xxxxx bg-desktop.png
-rw-r--r-- 1 mcweb mcweb  xxxxx bg-mobile.jpg
-rw-r--r-- 1 mcweb mcweb  xxxxx qq-qrcode.jpg
```

### 6.5 CSS中背景图片配置说明

背景图片在CSS中的配置位于 `frontend/themes/mctheme/main.css`：

**电脑版背景（第30行左右）：**
```css
.bg-img {
    position: absolute;
    inset: 0;
    background: url('../images/bg-desktop.png') center/cover no-repeat;
}
```

**手机版背景（响应式部分，第180行左右）：**
```css
@media (max-width: 768px) {
    .bg-img {
        background-image: url('../images/bg-mobile.jpg');
        background-position: center top;
    }
}
```

### 6.6 更换背景图片

如果要更换背景图片：

1. 准备新的图片文件
2. 上传到 `images/` 目录，替换原文件（保持相同文件名）
3. 或者修改CSS中的图片路径

**重要提示：**
- 图片文件名**区分大小写**
- 建议使用JPG格式以减小文件大小
- 背景图片会自动等比例缩放填充屏幕（`cover`模式），**不会变形**
- `cover`模式会保持图片比例，可能会裁剪部分边缘

### 6.7 图片上传后重新构建

**重要：** 上传图片后必须重新构建项目！

```bash
cd /opt/mcweb/minecraft-server-website
./mvnw clean package -Pproduction -DskipTests
```

---

## 7. 创建系统服务

```bash
cat > /etc/systemd/system/mcweb.service << 'EOF'
[Unit]
Description=好人服务器官网
After=network.target

[Service]
Type=simple
User=mcweb
Group=mcweb
WorkingDirectory=/opt/mcweb/minecraft-server-website
ExecStart=/usr/bin/java -Xms512m -Xmx1024m -XX:+UseG1GC -jar target/haoren-mc-website-1.0.0.jar
Restart=always
RestartSec=10

Environment=JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64

[Install]
WantedBy=multi-user.target
EOF
```

启动服务：
```bash
systemctl daemon-reload
systemctl enable mcweb
systemctl start mcweb
systemctl status mcweb
```

---

## 8. 安装Nginx

```bash
apt install -y nginx
systemctl enable nginx
systemctl start nginx
```

---

## 9. 配置Nginx反向代理

```bash
cat > /etc/nginx/sites-available/mcweb << 'EOF'
server {
    listen 80;
    server_name 你的域名.com www.你的域名.com;
    
    location /.well-known/acme-challenge/ {
        root /var/www/html;
    }
    
    location / {
        return 301 https://$server_name$request_uri;
    }
}

server {
    listen 443 ssl http2;
    server_name 你的域名.com www.你的域名.com;
    
    ssl_certificate /etc/letsencrypt/live/你的域名.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/你的域名.com/privkey.pem;
    
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_prefer_server_ciphers off;
    
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header Strict-Transport-Security "max-age=31536000" always;
    
    gzip on;
    gzip_types text/plain text/css application/json application/javascript;
    
    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_read_timeout 86400s;
    }
    
    location /VAADIN/push {
        proxy_pass http://127.0.0.1:8080;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_read_timeout 86400s;
    }
    
    location ~* \.(js|css|png|jpg|gif|ico|svg|woff|woff2)$ {
        proxy_pass http://127.0.0.1:8080;
        expires 30d;
        add_header Cache-Control "public, immutable";
    }
}
EOF
```

启用配置：
```bash
ln -s /etc/nginx/sites-available/mcweb /etc/nginx/sites-enabled/
rm -f /etc/nginx/sites-enabled/default
nginx -t
systemctl reload nginx
```

---

## 10. 申请SSL证书

```bash
apt install -y certbot python3-certbot-nginx
certbot --nginx -d 你的域名.com -d www.你的域名.com
```

设置自动续期：
```bash
crontab -e
# 添加：
0 3 * * * certbot renew --quiet --post-hook "systemctl reload nginx"
```

---

## 11. 配置防火墙

```bash
ufw enable
ufw allow ssh
ufw allow 80/tcp
ufw allow 443/tcp
ufw status
```

---

## 12. 域名解析

在域名服务商控制台添加DNS记录：

| 类型 | 主机记录 | 记录值 |
|------|---------|--------|
| A | @ | 服务器IP |
| A | www | 服务器IP |

---

## 13. 运维管理

### 常用命令

```bash
systemctl start mcweb     # 启动
systemctl stop mcweb      # 停止
systemctl restart mcweb   # 重启
systemctl status mcweb    # 状态
journalctl -u mcweb -f    # 查看日志
```

### 更新网站

```bash
systemctl stop mcweb
cp -r /opt/mcweb/minecraft-server-website /opt/mcweb/backup_$(date +%Y%m%d)
# 上传新版本并解压
cd /opt/mcweb/minecraft-server-website
./mvnw clean package -Pproduction -DskipTests
systemctl start mcweb
```

### 更换背景图片

```bash
# 1. 上传新图片到images目录（保持相同文件名）
# 2. 重新构建
cd /opt/mcweb/minecraft-server-website
./mvnw clean package -Pproduction -DskipTests
# 3. 重启服务
systemctl restart mcweb
```

---

## 14. 故障排查

### 问题1：背景图片不显示

检查步骤：
```bash
# 1. 确认图片文件存在
ls -la /opt/mcweb/minecraft-server-website/src/main/resources/META-INF/resources/images/

# 2. 确认文件名正确（区分大小写）
# bg-desktop.png
# bg-mobile.jpg

# 3. 检查文件权限
chmod 644 /opt/mcweb/minecraft-server-website/src/main/resources/META-INF/resources/images/*

# 4. 重新构建
./mvnw clean package -Pproduction -DskipTests

# 5. 重启服务
systemctl restart mcweb
```

### 问题2：网站打不开

```bash
systemctl status mcweb
netstat -tlnp | grep 8080
systemctl status nginx
nginx -t
```

### 问题3：502错误

```bash
systemctl restart mcweb
journalctl -u mcweb -n 50
```

---

## 项目文件结构

```
minecraft-server-website/
├── pom.xml
├── src/main/
│   ├── java/com/haorenserverwebsite/
│   │   ├── HaorenServerApplication.java
│   │   ├── config/WebSecurityConfig.java
│   │   └── view/IndexView.java
│   └── resources/
│       ├── application.properties
│       └── META-INF/resources/images/    ← 背景图片放这里！
│           ├── bg-desktop.png            ← 电脑版背景
│           ├── bg-mobile.jpg             ← 手机版背景
│           └── qq-qrcode.jpg             ← QQ群二维码
└── frontend/themes/mctheme/
    ├── main.css                          ← 样式表（包含背景配置）
    └── theme.json
```

---

## 常用命令速查

| 操作 | 命令 |
|------|------|
| 启动网站 | `systemctl start mcweb` |
| 停止网站 | `systemctl stop mcweb` |
| 重启网站 | `systemctl restart mcweb` |
| 查看状态 | `systemctl status mcweb` |
| 查看日志 | `journalctl -u mcweb -f` |
| 重载Nginx | `systemctl reload nginx` |
| 续期证书 | `certbot renew` |

---

文档版本：1.1  
最后更新：2026年1月
