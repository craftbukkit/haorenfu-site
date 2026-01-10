/*
 * ═══════════════════════════════════════════════════════════════════════════
 *                        ADMIN DASHBOARD VIEW
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Administrative interface for server management and analytics.
 * Requires ADMIN or OWNER role to access.
 */
package world.haorenfu.ui.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;
import world.haorenfu.domain.user.UserService;
import world.haorenfu.ui.layout.MainLayout;

/**
 * Admin dashboard with analytics and management tools.
 */
@Route(value = "admin", layout = MainLayout.class)
@PageTitle("管理后台 | 好人服")
@RolesAllowed({"ADMIN", "OWNER"})
public class AdminDashboardView extends VerticalLayout {

    private final UserService userService;

    public AdminDashboardView(UserService userService) {
        this.userService = userService;

        addClassName("admin-dashboard");
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        add(createHeader());
        add(createStatsCards());
        add(createQuickActions());
        add(createRecentActivity());
    }

    private Component createHeader() {
        H2 title = new H2("管理后台");
        title.addClassNames(LumoUtility.Margin.Bottom.NONE);

        Paragraph description = new Paragraph("服务器状态概览和管理工具");
        description.addClassNames(LumoUtility.TextColor.SECONDARY);

        VerticalLayout header = new VerticalLayout(title, description);
        header.setSpacing(false);
        header.setPadding(false);
        return header;
    }

    private Component createStatsCards() {
        HorizontalLayout cards = new HorizontalLayout();
        cards.setWidthFull();
        cards.setSpacing(true);

        UserService.UserStatistics stats = userService.getStatistics();

        cards.add(createStatCard("总用户数", String.valueOf(stats.totalUsers()), VaadinIcon.USERS, "#4CAF50", "+12%"));
        cards.add(createStatCard("今日活跃", String.valueOf(stats.activeUsersToday()), VaadinIcon.FLASH, "#2196F3", "+5%"));
        cards.add(createStatCard("今日新增", String.valueOf(stats.newUsersToday()), VaadinIcon.PLUS_CIRCLE, "#FF9800", "+8%"));
        cards.add(createStatCard("白名单用户", String.valueOf(stats.whitelistedUsers()), VaadinIcon.CHECK_CIRCLE, "#9C27B0", "+3%"));

        return cards;
    }

    private Component createStatCard(String label, String value, VaadinIcon icon, String color, String change) {
        VerticalLayout card = new VerticalLayout();
        card.setSpacing(false);
        card.setPadding(true);
        card.setWidth("25%");
        card.getElement().getStyle()
            .set("background", "var(--lumo-contrast-5pct)")
            .set("border-radius", "var(--lumo-border-radius-l)")
            .set("border-left", "4px solid " + color);

        HorizontalLayout top = new HorizontalLayout();
        top.setWidthFull();
        top.setAlignItems(FlexComponent.Alignment.CENTER);

        Span labelSpan = new Span(label);
        labelSpan.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.SMALL);

        Icon cardIcon = icon.create();
        cardIcon.setSize("20px");
        cardIcon.getElement().getStyle().set("color", color);

        top.add(labelSpan);
        top.addAndExpand(new Span());
        top.add(cardIcon);

        Span valueSpan = new Span(value);
        valueSpan.addClassNames(LumoUtility.FontSize.XXXLARGE, LumoUtility.FontWeight.BOLD);

        Span changeSpan = new Span(change);
        changeSpan.getElement().getStyle()
            .set("color", "#4CAF50")
            .set("font-size", "var(--lumo-font-size-s)");

        card.add(top, valueSpan, changeSpan);
        return card;
    }

    private Component createQuickActions() {
        H3 title = new H3("快捷操作");

        FlexLayout actions = new FlexLayout();
        actions.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        actions.getElement().getStyle().set("gap", "16px");

        actions.add(createActionCard("用户管理", "管理用户账号和权限", VaadinIcon.USERS, "admin/users"));
        actions.add(createActionCard("帖子管理", "审核和管理论坛帖子", VaadinIcon.COMMENT, "admin/posts"));
        actions.add(createActionCard("服务器配置", "修改服务器设置", VaadinIcon.COGS, "admin/config"));
        actions.add(createActionCard("白名单管理", "审核白名单申请", VaadinIcon.LIST, "admin/whitelist"));
        actions.add(createActionCard("封禁管理", "管理封禁用户", VaadinIcon.BAN, "admin/bans"));
        actions.add(createActionCard("数据统计", "查看详细分析数据", VaadinIcon.CHART, "admin/analytics"));

        VerticalLayout section = new VerticalLayout(title, actions);
        section.setSpacing(true);
        section.setPadding(false);
        return section;
    }

    private Component createActionCard(String title, String description, VaadinIcon icon, String route) {
        VerticalLayout card = new VerticalLayout();
        card.setSpacing(false);
        card.setPadding(true);
        card.setWidth("200px");
        card.getElement().getStyle()
            .set("background", "var(--lumo-contrast-5pct)")
            .set("border-radius", "var(--lumo-border-radius-m)")
            .set("cursor", "pointer")
            .set("transition", "transform 0.2s");

        Icon cardIcon = icon.create();
        cardIcon.setSize("32px");
        cardIcon.getElement().getStyle().set("color", "var(--lumo-primary-color)");

        Span titleSpan = new Span(title);
        titleSpan.addClassNames(LumoUtility.FontWeight.SEMIBOLD);

        Span descSpan = new Span(description);
        descSpan.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY);

        card.add(cardIcon, titleSpan, descSpan);

        card.addClickListener(e ->
            getUI().ifPresent(ui -> ui.navigate(route))
        );

        return card;
    }

    private Component createRecentActivity() {
        H3 title = new H3("最近活动");

        VerticalLayout activities = new VerticalLayout();
        activities.setSpacing(false);
        activities.setPadding(false);

        // Sample activities
        activities.add(createActivityItem("👤", "新用户注册", "Player123 加入了社区", "2分钟前"));
        activities.add(createActivityItem("📝", "新帖子", "关于红石的讨论 被发布", "5分钟前"));
        activities.add(createActivityItem("⚠️", "举报", "一条评论被举报", "10分钟前"));
        activities.add(createActivityItem("✅", "白名单", "新的白名单申请已通过", "15分钟前"));
        activities.add(createActivityItem("🔧", "系统", "服务器状态检查完成", "30分钟前"));

        VerticalLayout section = new VerticalLayout(title, activities);
        section.setSpacing(true);
        section.setPadding(false);
        return section;
    }

    private Component createActivityItem(String emoji, String type, String description, String time) {
        HorizontalLayout item = new HorizontalLayout();
        item.setWidthFull();
        item.setAlignItems(FlexComponent.Alignment.CENTER);
        item.setPadding(true);
        item.getElement().getStyle()
            .set("border-bottom", "1px solid var(--lumo-contrast-5pct)");

        Span emojiSpan = new Span(emoji);
        emojiSpan.getElement().getStyle().set("font-size", "24px");

        VerticalLayout info = new VerticalLayout();
        info.setSpacing(false);
        info.setPadding(false);

        Span typeSpan = new Span(type);
        typeSpan.addClassNames(LumoUtility.FontWeight.SEMIBOLD, LumoUtility.FontSize.SMALL);

        Span descSpan = new Span(description);
        descSpan.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.SMALL);

        info.add(typeSpan, descSpan);

        Span timeSpan = new Span(time);
        timeSpan.addClassNames(LumoUtility.FontSize.XSMALL, LumoUtility.TextColor.SECONDARY);
        timeSpan.getElement().getStyle().set("margin-left", "auto");

        item.add(emojiSpan, info, timeSpan);
        return item;
    }
}
