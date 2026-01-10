/*
 * ═══════════════════════════════════════════════════════════════════════════
 *                          PROFILE VIEW
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Comprehensive user profile page displaying achievements, statistics,
 * activity history, and social connections.
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
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.*;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;
import world.haorenfu.domain.user.User;
import world.haorenfu.domain.user.UserService;
import world.haorenfu.ui.layout.MainLayout;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

/**
 * User profile page with detailed statistics and activity.
 */
@Route(value = "profile", layout = MainLayout.class)
@PageTitle("个人资料 | 好人服")
@PermitAll
public class ProfileView extends VerticalLayout implements HasUrlParameter<String> {

    private final UserService userService;
    private User profileUser;
    private VerticalLayout contentArea;

    public ProfileView(UserService userService) {
        this.userService = userService;

        addClassName("profile-view");
        setSizeFull();
        setPadding(true);
        setSpacing(true);
    }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
        if (parameter != null && !parameter.isEmpty()) {
            try {
                UUID userId = UUID.fromString(parameter);
                Optional<User> user = userService.findById(userId);
                user.ifPresent(this::displayProfile);
            } catch (IllegalArgumentException e) {
                // Try to find by username
                Optional<User> user = userService.findByUsername(parameter);
                user.ifPresentOrElse(
                    this::displayProfile,
                    () -> showNotFound()
                );
            }
        } else {
            showNotFound();
        }
    }

    private void displayProfile(User user) {
        this.profileUser = user;
        removeAll();

        add(createProfileHeader());
        add(createStatsSection());
        add(createTabs());

        contentArea = new VerticalLayout();
        contentArea.setSizeFull();
        contentArea.setPadding(false);
        add(contentArea);

        showActivityTab();
    }

    private Component createProfileHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setSpacing(true);
        header.getElement().getStyle()
            .set("background", "linear-gradient(135deg, var(--lumo-primary-color-10pct), var(--lumo-contrast-5pct))")
            .set("border-radius", "var(--lumo-border-radius-l)")
            .set("padding", "32px");

        // Avatar
        Div avatar = new Div();
        avatar.setText(profileUser.getUsername().substring(0, 1).toUpperCase());
        avatar.getElement().getStyle()
            .set("width", "100px")
            .set("height", "100px")
            .set("border-radius", "50%")
            .set("background", profileUser.getRole().getColor())
            .set("display", "flex")
            .set("align-items", "center")
            .set("justify-content", "center")
            .set("color", "white")
            .set("font-size", "48px")
            .set("font-weight", "bold")
            .set("box-shadow", "0 4px 12px rgba(0,0,0,0.15)");

        // User info
        VerticalLayout userInfo = new VerticalLayout();
        userInfo.setSpacing(false);
        userInfo.setPadding(false);

        HorizontalLayout nameRow = new HorizontalLayout();
        nameRow.setAlignItems(FlexComponent.Alignment.CENTER);

        H2 username = new H2(profileUser.getUsername());
        username.addClassNames(LumoUtility.Margin.NONE);

        Span roleBadge = new Span(profileUser.getRole().getDisplayName());
        roleBadge.getElement().getStyle()
            .set("background", profileUser.getRole().getColor())
            .set("color", "white")
            .set("padding", "4px 12px")
            .set("border-radius", "12px")
            .set("font-size", "var(--lumo-font-size-s)")
            .set("margin-left", "12px");

        nameRow.add(username, roleBadge);

        // Signature
        Paragraph signature = new Paragraph(
            profileUser.getSignature() != null ? profileUser.getSignature() : "这个人很懒，什么都没写~"
        );
        signature.addClassNames(LumoUtility.TextColor.SECONDARY);

        // MC ID and dates
        HorizontalLayout metaInfo = new HorizontalLayout();
        metaInfo.setSpacing(true);

        if (profileUser.getMinecraftId() != null) {
            Span mcId = createMetaSpan(VaadinIcon.GAMEPAD, profileUser.getMinecraftId());
            metaInfo.add(mcId);
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日")
            .withZone(ZoneId.systemDefault());
        Span joinDate = createMetaSpan(VaadinIcon.CALENDAR, "加入于 " + formatter.format(profileUser.getCreatedAt()));
        metaInfo.add(joinDate);

        if (profileUser.isWhitelisted()) {
            Span whitelisted = createMetaSpan(VaadinIcon.CHECK, "已获白名单");
            whitelisted.getElement().getStyle().set("color", "#4CAF50");
            metaInfo.add(whitelisted);
        }

        userInfo.add(nameRow, signature, metaInfo);

        header.add(avatar, userInfo);
        return header;
    }

    private Span createMetaSpan(VaadinIcon icon, String text) {
        Icon i = icon.create();
        i.setSize("14px");
        i.getElement().getStyle().set("margin-right", "4px");

        Span span = new Span();
        span.getElement().appendChild(i.getElement());
        span.add(text);
        span.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY);
        return span;
    }

    private Component createStatsSection() {
        HorizontalLayout stats = new HorizontalLayout();
        stats.setWidthFull();
        stats.setJustifyContentMode(FlexComponent.JustifyContentMode.EVENLY);
        stats.getElement().getStyle()
            .set("padding", "24px 0")
            .set("border-bottom", "1px solid var(--lumo-contrast-10pct)");

        stats.add(createStatCard("声望", String.valueOf(profileUser.getReputation()), "⭐"));
        stats.add(createStatCard("等级", profileUser.getReputationRank(), "🏆"));
        stats.add(createStatCard("帖子", String.valueOf(profileUser.getPostCount()), "📝"));
        stats.add(createStatCard("评论", String.valueOf(profileUser.getCommentCount()), "💬"));
        stats.add(createStatCard("游戏时长", (profileUser.getPlayTimeMinutes() / 60) + "h", "🎮"));

        return stats;
    }

    private Component createStatCard(String label, String value, String emoji) {
        VerticalLayout card = new VerticalLayout();
        card.setSpacing(false);
        card.setAlignItems(FlexComponent.Alignment.CENTER);

        Span emojiSpan = new Span(emoji);
        emojiSpan.getElement().getStyle().set("font-size", "24px");

        Span valueSpan = new Span(value);
        valueSpan.addClassNames(LumoUtility.FontSize.XLARGE, LumoUtility.FontWeight.BOLD);

        Span labelSpan = new Span(label);
        labelSpan.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY);

        card.add(emojiSpan, valueSpan, labelSpan);
        return card;
    }

    private Component createTabs() {
        Tab activityTab = new Tab("动态");
        Tab postsTab = new Tab("帖子");
        Tab achievementsTab = new Tab("成就");
        Tab friendsTab = new Tab("好友");

        Tabs tabs = new Tabs(activityTab, postsTab, achievementsTab, friendsTab);
        tabs.addSelectedChangeListener(event -> {
            Tab selected = event.getSelectedTab();
            if (selected == activityTab) showActivityTab();
            else if (selected == postsTab) showPostsTab();
            else if (selected == achievementsTab) showAchievementsTab();
            else if (selected == friendsTab) showFriendsTab();
        });

        return tabs;
    }

    private void showActivityTab() {
        contentArea.removeAll();

        // Activity feed
        VerticalLayout feed = new VerticalLayout();
        feed.setSpacing(true);
        feed.setPadding(false);

        // Sample activities
        feed.add(createActivityItem("🎮", "登录服务器", "2小时前"));
        feed.add(createActivityItem("📝", "发布了帖子「新手建筑分享」", "1天前"));
        feed.add(createActivityItem("💬", "评论了帖子「红石教程」", "2天前"));
        feed.add(createActivityItem("🏆", "获得成就「建筑大师」", "3天前"));
        feed.add(createActivityItem("⭐", "获得了 50 声望", "1周前"));

        contentArea.add(feed);
    }

    private Component createActivityItem(String emoji, String text, String time) {
        HorizontalLayout item = new HorizontalLayout();
        item.setWidthFull();
        item.setAlignItems(FlexComponent.Alignment.CENTER);
        item.getElement().getStyle()
            .set("padding", "12px")
            .set("background", "var(--lumo-contrast-5pct)")
            .set("border-radius", "var(--lumo-border-radius-m)");

        Span emojiSpan = new Span(emoji);
        emojiSpan.getElement().getStyle().set("font-size", "20px");

        Span textSpan = new Span(text);
        textSpan.addClassNames(LumoUtility.FontWeight.MEDIUM);

        Span timeSpan = new Span(time);
        timeSpan.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY);
        timeSpan.getElement().getStyle().set("margin-left", "auto");

        item.add(emojiSpan, textSpan, timeSpan);
        return item;
    }

    private void showPostsTab() {
        contentArea.removeAll();
        Paragraph placeholder = new Paragraph("该用户还没有发布任何帖子");
        placeholder.addClassNames(LumoUtility.TextColor.SECONDARY);
        placeholder.getElement().getStyle().set("text-align", "center").set("padding", "48px");
        contentArea.add(placeholder);
    }

    private void showAchievementsTab() {
        contentArea.removeAll();

        FlexLayout grid = new FlexLayout();
        grid.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        grid.getElement().getStyle().set("gap", "12px");

        // Sample achievements
        grid.add(createAchievementBadge("🎮", "初来乍到", true));
        grid.add(createAchievementBadge("📝", "畅所欲言", true));
        grid.add(createAchievementBadge("🏠", "小小建筑师", true));
        grid.add(createAchievementBadge("🏛️", "建筑大师", true));
        grid.add(createAchievementBadge("⛏️", "矿工入门", false));
        grid.add(createAchievementBadge("🐉", "龙之主宰", false));

        contentArea.add(grid);
    }

    private Component createAchievementBadge(String emoji, String name, boolean unlocked) {
        VerticalLayout badge = new VerticalLayout();
        badge.setAlignItems(FlexComponent.Alignment.CENTER);
        badge.setSpacing(false);
        badge.setPadding(true);
        badge.setWidth("100px");
        badge.getElement().getStyle()
            .set("background", unlocked ? "var(--lumo-primary-color-10pct)" : "var(--lumo-contrast-5pct)")
            .set("border-radius", "var(--lumo-border-radius-m)")
            .set("opacity", unlocked ? "1" : "0.5");

        Span emojiSpan = new Span(emoji);
        emojiSpan.getElement().getStyle()
            .set("font-size", "32px")
            .set("filter", unlocked ? "none" : "grayscale(100%)");

        Span nameSpan = new Span(name);
        nameSpan.addClassNames(LumoUtility.FontSize.XSMALL);
        nameSpan.getElement().getStyle().set("text-align", "center");

        badge.add(emojiSpan, nameSpan);
        return badge;
    }

    private void showFriendsTab() {
        contentArea.removeAll();
        Paragraph placeholder = new Paragraph("好友系统即将上线");
        placeholder.addClassNames(LumoUtility.TextColor.SECONDARY);
        placeholder.getElement().getStyle().set("text-align", "center").set("padding", "48px");
        contentArea.add(placeholder);
    }

    private void showNotFound() {
        removeAll();
        H2 title = new H2("用户未找到");
        Paragraph message = new Paragraph("抱歉，找不到该用户的资料。");
        message.addClassNames(LumoUtility.TextColor.SECONDARY);

        VerticalLayout notFound = new VerticalLayout(title, message);
        notFound.setAlignItems(FlexComponent.Alignment.CENTER);
        notFound.setPadding(true);
        add(notFound);
    }
}
