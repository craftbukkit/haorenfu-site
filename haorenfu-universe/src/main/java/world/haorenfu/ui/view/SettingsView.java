/*
 * ═══════════════════════════════════════════════════════════════════════════
 *                         SETTINGS VIEW
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * User preferences and account settings management.
 */
package world.haorenfu.ui.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;
import world.haorenfu.ui.layout.MainLayout;

/**
 * User settings and preferences.
 */
@Route(value = "settings", layout = MainLayout.class)
@PageTitle("设置 | 好人服")
@PermitAll
public class SettingsView extends VerticalLayout {

    private VerticalLayout contentArea;

    public SettingsView() {
        addClassName("settings-view");
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        add(createHeader());

        HorizontalLayout mainContent = new HorizontalLayout();
        mainContent.setSizeFull();
        mainContent.setSpacing(true);

        mainContent.add(createTabs());

        contentArea = new VerticalLayout();
        contentArea.setSizeFull();
        contentArea.setPadding(false);
        mainContent.add(contentArea);
        mainContent.setFlexGrow(1, contentArea);

        add(mainContent);

        showProfileSettings();
    }

    private Component createHeader() {
        H2 title = new H2("账号设置");
        title.addClassNames(LumoUtility.Margin.Bottom.SMALL);

        Paragraph description = new Paragraph("管理你的账号信息、隐私设置和偏好选项");
        description.addClassNames(LumoUtility.TextColor.SECONDARY);

        VerticalLayout header = new VerticalLayout(title, description);
        header.setSpacing(false);
        header.setPadding(false);
        return header;
    }

    private Component createTabs() {
        VerticalLayout tabsContainer = new VerticalLayout();
        tabsContainer.setWidth("200px");
        tabsContainer.setSpacing(false);
        tabsContainer.setPadding(false);

        Button profileTab = createTabButton("👤", "个人资料", true);
        Button securityTab = createTabButton("🔒", "账号安全", false);
        Button notificationTab = createTabButton("🔔", "通知设置", false);
        Button privacyTab = createTabButton("👁️", "隐私设置", false);
        Button appearanceTab = createTabButton("🎨", "外观主题", false);
        Button minecraftTab = createTabButton("🎮", "MC绑定", false);

        profileTab.addClickListener(e -> {
            resetTabs(profileTab, securityTab, notificationTab, privacyTab, appearanceTab, minecraftTab);
            setActiveTab(profileTab);
            showProfileSettings();
        });

        securityTab.addClickListener(e -> {
            resetTabs(profileTab, securityTab, notificationTab, privacyTab, appearanceTab, minecraftTab);
            setActiveTab(securityTab);
            showSecuritySettings();
        });

        notificationTab.addClickListener(e -> {
            resetTabs(profileTab, securityTab, notificationTab, privacyTab, appearanceTab, minecraftTab);
            setActiveTab(notificationTab);
            showNotificationSettings();
        });

        privacyTab.addClickListener(e -> {
            resetTabs(profileTab, securityTab, notificationTab, privacyTab, appearanceTab, minecraftTab);
            setActiveTab(privacyTab);
            showPrivacySettings();
        });

        appearanceTab.addClickListener(e -> {
            resetTabs(profileTab, securityTab, notificationTab, privacyTab, appearanceTab, minecraftTab);
            setActiveTab(appearanceTab);
            showAppearanceSettings();
        });

        minecraftTab.addClickListener(e -> {
            resetTabs(profileTab, securityTab, notificationTab, privacyTab, appearanceTab, minecraftTab);
            setActiveTab(minecraftTab);
            showMinecraftSettings();
        });

        tabsContainer.add(profileTab, securityTab, notificationTab, privacyTab, appearanceTab, minecraftTab);
        return tabsContainer;
    }

    private Button createTabButton(String icon, String text, boolean active) {
        Button button = new Button(icon + "  " + text);
        button.setWidthFull();
        button.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        button.getElement().getStyle()
            .set("justify-content", "flex-start")
            .set("padding", "12px 16px");

        if (active) {
            setActiveTab(button);
        }

        return button;
    }

    private void resetTabs(Button... tabs) {
        for (Button tab : tabs) {
            tab.getElement().getStyle()
                .set("background", "transparent")
                .set("color", "var(--lumo-body-text-color)");
        }
    }

    private void setActiveTab(Button tab) {
        tab.getElement().getStyle()
            .set("background", "var(--lumo-primary-color-10pct)")
            .set("color", "var(--lumo-primary-text-color)");
    }

    private void showProfileSettings() {
        contentArea.removeAll();

        VerticalLayout form = createSettingsSection("个人资料");

        TextField usernameField = new TextField("用户名");
        usernameField.setValue("Player123");
        usernameField.setWidthFull();
        usernameField.setHelperText("3-20个字符，支持中英文和数字");

        TextField emailField = new TextField("邮箱");
        emailField.setValue("player@example.com");
        emailField.setWidthFull();

        TextArea signatureField = new TextArea("个性签名");
        signatureField.setValue("这个人很懒，什么都没写~");
        signatureField.setWidthFull();
        signatureField.setMaxLength(200);

        TextArea bioField = new TextArea("个人简介");
        bioField.setWidthFull();
        bioField.setMaxLength(2000);
        bioField.setPlaceholder("介绍一下你自己...");

        Button saveButton = new Button("保存更改");
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(e ->
            Notification.show("设置已保存", 3000, Notification.Position.BOTTOM_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS)
        );

        form.add(usernameField, emailField, signatureField, bioField, saveButton);
        contentArea.add(form);
    }

    private void showSecuritySettings() {
        contentArea.removeAll();

        VerticalLayout form = createSettingsSection("账号安全");

        // Change password
        H4 passwordTitle = new H4("修改密码");
        PasswordField currentPassword = new PasswordField("当前密码");
        currentPassword.setWidthFull();

        PasswordField newPassword = new PasswordField("新密码");
        newPassword.setWidthFull();
        newPassword.setHelperText("至少8位，包含字母和数字");

        PasswordField confirmPassword = new PasswordField("确认新密码");
        confirmPassword.setWidthFull();

        Button changePasswordButton = new Button("修改密码");
        changePasswordButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        // Two-factor auth
        Hr divider = new Hr();

        H4 twoFactorTitle = new H4("两步验证");
        Paragraph twoFactorDesc = new Paragraph("启用两步验证以增强账号安全性");
        twoFactorDesc.addClassNames(LumoUtility.TextColor.SECONDARY);

        Button enableTwoFactor = new Button("启用两步验证");
        enableTwoFactor.addThemeVariants(ButtonVariant.LUMO_CONTRAST);

        // Sessions
        Hr divider2 = new Hr();

        H4 sessionsTitle = new H4("登录会话");
        Paragraph sessionsDesc = new Paragraph("管理你的登录设备和会话");
        sessionsDesc.addClassNames(LumoUtility.TextColor.SECONDARY);

        Button logoutAllButton = new Button("登出所有设备");
        logoutAllButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        form.add(passwordTitle, currentPassword, newPassword, confirmPassword, changePasswordButton,
                 divider, twoFactorTitle, twoFactorDesc, enableTwoFactor,
                 divider2, sessionsTitle, sessionsDesc, logoutAllButton);
        contentArea.add(form);
    }

    private void showNotificationSettings() {
        contentArea.removeAll();

        VerticalLayout form = createSettingsSection("通知设置");

        Checkbox emailNotifications = new Checkbox("接收邮件通知");
        emailNotifications.setValue(true);

        Checkbox forumReplies = new Checkbox("帖子回复通知");
        forumReplies.setValue(true);

        Checkbox mentionNotifications = new Checkbox("@提及通知");
        mentionNotifications.setValue(true);

        Checkbox activityNotifications = new Checkbox("活动和公告通知");
        activityNotifications.setValue(true);

        Checkbox weeklyDigest = new Checkbox("每周社区摘要");
        weeklyDigest.setValue(false);

        Button saveButton = new Button("保存设置");
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        form.add(emailNotifications, forumReplies, mentionNotifications,
                 activityNotifications, weeklyDigest, saveButton);
        contentArea.add(form);
    }

    private void showPrivacySettings() {
        contentArea.removeAll();

        VerticalLayout form = createSettingsSection("隐私设置");

        Select<String> profileVisibility = new Select<>();
        profileVisibility.setLabel("资料可见性");
        profileVisibility.setItems("所有人", "仅注册用户", "仅好友", "仅自己");
        profileVisibility.setValue("所有人");
        profileVisibility.setWidthFull();

        Select<String> onlineStatus = new Select<>();
        onlineStatus.setLabel("在线状态显示");
        onlineStatus.setItems("显示", "隐藏", "仅好友可见");
        onlineStatus.setValue("显示");
        onlineStatus.setWidthFull();

        Checkbox showPlayTime = new Checkbox("显示游戏时长");
        showPlayTime.setValue(true);

        Checkbox showAchievements = new Checkbox("显示成就");
        showAchievements.setValue(true);

        Checkbox allowMessages = new Checkbox("允许陌生人发送私信");
        allowMessages.setValue(false);

        Button saveButton = new Button("保存设置");
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        form.add(profileVisibility, onlineStatus, showPlayTime, showAchievements, allowMessages, saveButton);
        contentArea.add(form);
    }

    private void showAppearanceSettings() {
        contentArea.removeAll();

        VerticalLayout form = createSettingsSection("外观主题");

        Select<String> theme = new Select<>();
        theme.setLabel("主题模式");
        theme.setItems("跟随系统", "明亮模式", "暗黑模式");
        theme.setValue("暗黑模式");
        theme.setWidthFull();

        Select<String> primaryColor = new Select<>();
        primaryColor.setLabel("主题色");
        primaryColor.setItems("默认绿色", "天空蓝", "活力橙", "优雅紫", "热情红");
        primaryColor.setValue("默认绿色");
        primaryColor.setWidthFull();

        Select<String> fontSize = new Select<>();
        fontSize.setLabel("字体大小");
        fontSize.setItems("小", "中", "大");
        fontSize.setValue("中");
        fontSize.setWidthFull();

        Checkbox animations = new Checkbox("启用动画效果");
        animations.setValue(true);

        Button saveButton = new Button("保存设置");
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        form.add(theme, primaryColor, fontSize, animations, saveButton);
        contentArea.add(form);
    }

    private void showMinecraftSettings() {
        contentArea.removeAll();

        VerticalLayout form = createSettingsSection("Minecraft 绑定");

        TextField minecraftId = new TextField("Minecraft ID");
        minecraftId.setValue("Player_123");
        minecraftId.setWidthFull();
        minecraftId.setHelperText("你的正版 Minecraft 用户名");

        // Status display
        HorizontalLayout status = new HorizontalLayout();
        status.setAlignItems(FlexComponent.Alignment.CENTER);
        status.getElement().getStyle()
            .set("background", "var(--lumo-success-color-10pct)")
            .set("padding", "12px")
            .set("border-radius", "var(--lumo-border-radius-m)");

        Span statusIcon = new Span("✓");
        statusIcon.getElement().getStyle().set("color", "var(--lumo-success-color)");

        Span statusText = new Span("已绑定并通过验证");
        statusText.getElement().getStyle().set("color", "var(--lumo-success-text-color)");

        status.add(statusIcon, statusText);

        // Whitelist status
        Paragraph whitelistInfo = new Paragraph("白名单状态: 已添加");
        whitelistInfo.addClassNames(LumoUtility.TextColor.SECONDARY);

        Button unbindButton = new Button("解除绑定");
        unbindButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);

        Button saveButton = new Button("更新绑定");
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        form.add(minecraftId, status, whitelistInfo, saveButton, unbindButton);
        contentArea.add(form);
    }

    private VerticalLayout createSettingsSection(String title) {
        VerticalLayout section = new VerticalLayout();
        section.setSpacing(true);
        section.setPadding(true);
        section.getElement().getStyle()
            .set("background", "var(--lumo-contrast-5pct)")
            .set("border-radius", "var(--lumo-border-radius-l)");

        H3 sectionTitle = new H3(title);
        sectionTitle.addClassNames(LumoUtility.Margin.Bottom.MEDIUM);
        section.add(sectionTitle);

        return section;
    }
}
