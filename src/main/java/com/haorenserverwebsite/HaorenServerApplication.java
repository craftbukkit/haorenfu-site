package com.haorenserverwebsite;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.Theme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 好人服务器官网启动类
 * 
 * 技术栈: Vaadin 24 + Spring Boot 3 + Java 21
 * 
 * @since 2024-01
 */
@SpringBootApplication
@Theme("mctheme")
@PWA(
    name = "好人服务器", 
    shortName = "好人服", 
    description = "Minecraft好人服务器官网"
)
@Push
public class HaorenServerApplication implements AppShellConfigurator {

    public static void main(String[] args) {
        // 优化随机数生成器性能
        System.setProperty("java.security.egd", "file:/dev/./urandom");
        SpringApplication.run(HaorenServerApplication.class, args);
    }
}
