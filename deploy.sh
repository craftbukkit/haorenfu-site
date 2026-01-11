#!/bin/bash
#===============================================================================
# Minecraft好人服务器官网 - 一键部署脚本
# 适用系统: 华为欧拉系统 (openEuler)
# Java版本: Java 25
# 域名: haorenfu.com
#===============================================================================

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# 配置
DOMAIN="haorenfu.com"
APP_NAME="mcweb"
APP_USER="mcweb"
APP_DIR="/opt/mcweb"
JAR_NAME="haoren-mc-website-1.0.0.jar"
JAVA_VERSION="25"

log_info() { echo -e "${GREEN}[INFO]${NC} $1"; }
log_warn() { echo -e "${YELLOW}[WARN]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }
log_step() { echo -e "${BLUE}[STEP]${NC} $1"; }

# 检查root权限
check_root() {
    if [ "$EUID" -ne 0 ]; then
        log_error "请使用root权限运行此脚本"
        exit 1
    fi
}

# 检查系统
check_system() {
    log_step "检查系统环境..."
    if [ -f /etc/openEuler-release ]; then
        log_info "检测到华为欧拉系统: $(cat /etc/openEuler-release)"
    elif [ -f /etc/euleros-release ]; then
        log_info "检测到EulerOS: $(cat /etc/euleros-release)"
    else
        log_warn "未检测到欧拉系统，将尝试继续安装"
    fi
}

# 更新系统
update_system() {
    log_step "更新系统软件包..."
    if command -v dnf &> /dev/null; then
        dnf update -y
        dnf install -y wget curl vim tar unzip git firewalld
    elif command -v yum &> /dev/null; then
        yum update -y
        yum install -y wget curl vim tar unzip git firewalld
    fi
}

# 安装Java 25
install_java() {
    log_step "检查Java ${JAVA_VERSION}..."
    
    if java -version 2>&1 | grep -q "version \"${JAVA_VERSION}"; then
        log_info "Java ${JAVA_VERSION} 已安装"
        return
    fi
    
    log_info "安装Java ${JAVA_VERSION}..."
    
    # 下载OpenJDK 25 (使用Adoptium/Temurin)
    JAVA_URL="https://github.com/adoptium/temurin25-binaries/releases/download/jdk-25%2B1-ea-beta/OpenJDK25-jdk_x64_linux_hotspot_25_1-ea.tar.gz"
    JAVA_DIR="/usr/lib/jvm"
    
    mkdir -p ${JAVA_DIR}
    cd /tmp
    
    # 如果无法下载最新版，使用备用方案
    log_info "尝试下载Java 25..."
    if ! wget -q --timeout=30 "${JAVA_URL}" -O openjdk25.tar.gz 2>/dev/null; then
        log_warn "无法下载预编译版本，尝试使用系统包管理器..."
        
        # 尝试使用系统包管理器安装最新可用版本
        if command -v dnf &> /dev/null; then
            dnf install -y java-latest-openjdk java-latest-openjdk-devel || \
            dnf install -y java-21-openjdk java-21-openjdk-devel
        elif command -v yum &> /dev/null; then
            yum install -y java-latest-openjdk java-latest-openjdk-devel || \
            yum install -y java-21-openjdk java-21-openjdk-devel
        fi
    else
        tar -xzf openjdk25.tar.gz -C ${JAVA_DIR}
        JAVA_HOME_DIR=$(ls -d ${JAVA_DIR}/jdk-25* 2>/dev/null | head -1)
        
        if [ -n "$JAVA_HOME_DIR" ]; then
            # 配置alternatives
            update-alternatives --install /usr/bin/java java ${JAVA_HOME_DIR}/bin/java 1
            update-alternatives --install /usr/bin/javac javac ${JAVA_HOME_DIR}/bin/javac 1
            update-alternatives --set java ${JAVA_HOME_DIR}/bin/java
            update-alternatives --set javac ${JAVA_HOME_DIR}/bin/javac
            
            # 设置JAVA_HOME
            echo "export JAVA_HOME=${JAVA_HOME_DIR}" > /etc/profile.d/java.sh
            echo 'export PATH=$JAVA_HOME/bin:$PATH' >> /etc/profile.d/java.sh
            source /etc/profile.d/java.sh
        fi
        rm -f openjdk25.tar.gz
    fi
    
    log_info "Java版本: $(java -version 2>&1 | head -1)"
}

# 安装Maven
install_maven() {
    log_step "检查Maven..."
    
    if command -v mvn &> /dev/null; then
        log_info "Maven已安装: $(mvn -version | head -1)"
        return
    fi
    
    log_info "安装Maven..."
    
    MAVEN_VERSION="3.9.6"
    cd /tmp
    wget -q "https://dlcdn.apache.org/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz"
    tar -xzf apache-maven-${MAVEN_VERSION}-bin.tar.gz -C /opt
    ln -sf /opt/apache-maven-${MAVEN_VERSION}/bin/mvn /usr/bin/mvn
    rm -f apache-maven-${MAVEN_VERSION}-bin.tar.gz
    
    # 配置阿里云镜像
    mkdir -p /root/.m2
    cat > /root/.m2/settings.xml << 'MAVEN_EOF'
<?xml version="1.0" encoding="UTF-8"?>
<settings>
    <mirrors>
        <mirror>
            <id>aliyun</id>
            <mirrorOf>*</mirrorOf>
            <name>阿里云Maven镜像</name>
            <url>https://maven.aliyun.com/repository/public</url>
        </mirror>
    </mirrors>
</settings>
MAVEN_EOF
    
    log_info "Maven安装完成"
}

# 创建用户
create_user() {
    log_step "创建应用用户..."
    
    if id "${APP_USER}" &>/dev/null; then
        log_info "用户 ${APP_USER} 已存在"
    else
        useradd -r -m -s /bin/bash ${APP_USER}
        log_info "用户 ${APP_USER} 创建成功"
    fi
}

# 部署应用
deploy_app() {
    log_step "部署网站应用..."
    
    # 创建目录
    mkdir -p ${APP_DIR}
    
    # 检查是否有项目文件
    SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
    
    if [ -f "${SCRIPT_DIR}/pom.xml" ]; then
        log_info "从当前目录复制项目文件..."
        cp -r "${SCRIPT_DIR}"/* ${APP_DIR}/
    elif [ -f "./pom.xml" ]; then
        log_info "从当前目录复制项目文件..."
        cp -r ./* ${APP_DIR}/
    else
        log_error "未找到项目文件，请确保脚本与项目在同一目录"
        exit 1
    fi
    
    cd ${APP_DIR}
    
    # 构建项目
    log_info "构建项目（生产模式）..."
    mvn clean package -Pproduction -DskipTests
    
    # 设置权限
    chown -R ${APP_USER}:${APP_USER} ${APP_DIR}
    
    log_info "项目构建完成"
}

# 创建systemd服务
create_service() {
    log_step "创建系统服务..."
    
    cat > /etc/systemd/system/${APP_NAME}.service << SERVICE_EOF
[Unit]
Description=Minecraft好人服务器官网
After=network.target

[Service]
Type=simple
User=${APP_USER}
Group=${APP_USER}
WorkingDirectory=${APP_DIR}
ExecStart=/usr/bin/java -Xms512m -Xmx1024m -XX:+UseG1GC -XX:+UseStringDeduplication -jar target/${JAR_NAME}
Restart=always
RestartSec=10
StandardOutput=journal
StandardError=journal

Environment=JAVA_OPTS=-Dfile.encoding=UTF-8
Environment=SERVER_PORT=8080

[Install]
WantedBy=multi-user.target
SERVICE_EOF

    systemctl daemon-reload
    systemctl enable ${APP_NAME}
    systemctl start ${APP_NAME}
    
    log_info "服务已启动"
}

# 安装Nginx
install_nginx() {
    log_step "安装Nginx..."
    
    if command -v nginx &> /dev/null; then
        log_info "Nginx已安装"
    else
        if command -v dnf &> /dev/null; then
            dnf install -y nginx
        elif command -v yum &> /dev/null; then
            yum install -y nginx
        fi
    fi
    
    systemctl enable nginx
}

# 配置Nginx
configure_nginx() {
    log_step "配置Nginx反向代理..."
    
    cat > /etc/nginx/conf.d/${APP_NAME}.conf << NGINX_EOF
# Minecraft好人服务器官网
upstream mcweb_backend {
    server 127.0.0.1:8080;
    keepalive 32;
}

server {
    listen 80;
    server_name ${DOMAIN} www.${DOMAIN};

    # Let's Encrypt验证
    location /.well-known/acme-challenge/ {
        root /var/www/html;
    }

    # 重定向到HTTPS
    location / {
        return 301 https://\$server_name\$request_uri;
    }
}

server {
    listen 443 ssl http2;
    server_name ${DOMAIN} www.${DOMAIN};

    # SSL证书（稍后配置）
    ssl_certificate /etc/nginx/ssl/${DOMAIN}.crt;
    ssl_certificate_key /etc/nginx/ssl/${DOMAIN}.key;

    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers ECDHE-ECDSA-AES128-GCM-SHA256:ECDHE-RSA-AES128-GCM-SHA256:ECDHE-ECDSA-AES256-GCM-SHA384:ECDHE-RSA-AES256-GCM-SHA384;
    ssl_prefer_server_ciphers off;
    ssl_session_cache shared:SSL:10m;
    ssl_session_timeout 1d;

    # 安全头
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-XSS-Protection "1; mode=block" always;
    add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;

    # Gzip压缩
    gzip on;
    gzip_types text/plain text/css application/json application/javascript text/xml application/xml;
    gzip_min_length 1000;

    # 反向代理
    location / {
        proxy_pass http://mcweb_backend;
        proxy_http_version 1.1;
        proxy_set_header Upgrade \$http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
        proxy_read_timeout 86400s;
        proxy_send_timeout 86400s;
        proxy_buffering off;
    }

    # Vaadin WebSocket
    location /VAADIN/push {
        proxy_pass http://mcweb_backend;
        proxy_http_version 1.1;
        proxy_set_header Upgrade \$http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_read_timeout 86400s;
    }

    # 静态资源缓存
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2)$ {
        proxy_pass http://mcweb_backend;
        expires 30d;
        add_header Cache-Control "public, immutable";
    }
}
NGINX_EOF

    # 创建临时的自签名证书（后续用Let's Encrypt替换）
    mkdir -p /etc/nginx/ssl
    if [ ! -f /etc/nginx/ssl/${DOMAIN}.crt ]; then
        log_info "创建临时SSL证书..."
        openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
            -keyout /etc/nginx/ssl/${DOMAIN}.key \
            -out /etc/nginx/ssl/${DOMAIN}.crt \
            -subj "/CN=${DOMAIN}"
    fi
    
    # 测试配置
    nginx -t
    systemctl restart nginx
    
    log_info "Nginx配置完成"
}

# 配置防火墙
configure_firewall() {
    log_step "配置防火墙..."
    
    systemctl enable firewalld
    systemctl start firewalld
    
    firewall-cmd --permanent --add-service=http
    firewall-cmd --permanent --add-service=https
    firewall-cmd --permanent --add-service=ssh
    firewall-cmd --reload
    
    log_info "防火墙配置完成"
}

# 安装Let's Encrypt证书
install_ssl() {
    log_step "安装SSL证书..."
    
    # 安装certbot
    if command -v dnf &> /dev/null; then
        dnf install -y certbot python3-certbot-nginx
    elif command -v yum &> /dev/null; then
        yum install -y certbot python3-certbot-nginx
    fi
    
    log_warn "SSL证书安装需要域名已解析到此服务器"
    log_info "请确保以下DNS记录已配置:"
    echo "  ${DOMAIN}     ->  $(curl -s ifconfig.me)"
    echo "  www.${DOMAIN} ->  $(curl -s ifconfig.me)"
    echo ""
    
    read -p "域名是否已解析? (y/n): " dns_ready
    if [ "$dns_ready" = "y" ] || [ "$dns_ready" = "Y" ]; then
        certbot --nginx -d ${DOMAIN} -d www.${DOMAIN} --non-interactive --agree-tos --email admin@${DOMAIN}
        
        # 设置自动续期
        echo "0 3 * * * root certbot renew --quiet --post-hook 'systemctl reload nginx'" > /etc/cron.d/certbot-renew
        
        log_info "SSL证书安装完成"
    else
        log_warn "跳过SSL证书安装，请稍后手动执行:"
        echo "  certbot --nginx -d ${DOMAIN} -d www.${DOMAIN}"
    fi
}

# 显示结果
show_result() {
    echo ""
    echo "=========================================="
    echo -e "${GREEN}部署完成!${NC}"
    echo "=========================================="
    echo ""
    echo "网站信息:"
    echo "  域名: https://${DOMAIN}"
    echo "  应用目录: ${APP_DIR}"
    echo "  运行用户: ${APP_USER}"
    echo ""
    echo "常用命令:"
    echo "  启动: systemctl start ${APP_NAME}"
    echo "  停止: systemctl stop ${APP_NAME}"
    echo "  重启: systemctl restart ${APP_NAME}"
    echo "  状态: systemctl status ${APP_NAME}"
    echo "  日志: journalctl -u ${APP_NAME} -f"
    echo ""
    echo "Nginx命令:"
    echo "  重载: systemctl reload nginx"
    echo "  测试: nginx -t"
    echo ""
    echo "SSL证书续期:"
    echo "  certbot renew"
    echo ""
    
    # 检查服务状态
    if systemctl is-active --quiet ${APP_NAME}; then
        log_info "网站服务运行中"
    else
        log_warn "网站服务未运行，请检查日志"
    fi
}

# 主函数
main() {
    echo ""
    echo "=========================================="
    echo "  Minecraft好人服务器官网 - 一键部署"
    echo "=========================================="
    echo ""
    
    check_root
    check_system
    update_system
    install_java
    install_maven
    create_user
    deploy_app
    create_service
    install_nginx
    configure_nginx
    configure_firewall
    install_ssl
    show_result
}

# 执行
main "$@"
