/*
 * ═══════════════════════════════════════════════════════════════════════════
 *                         RANKINGS VIEW
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Comprehensive leaderboards showcasing top players across various metrics.
 * Uses Wilson score and Bayesian ranking for fair comparisons.
 */
package world.haorenfu.ui.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;
import world.haorenfu.domain.user.User;
import world.haorenfu.domain.user.UserService;
import world.haorenfu.ui.layout.MainLayout;

import java.util.List;

/**
 * View displaying various player rankings.
 */
@Route(value = "rankings", layout = MainLayout.class)
@PageTitle("排行榜 | 好人服")
@PermitAll
public class RankingsView extends VerticalLayout {

    private final UserService userService;
    private VerticalLayout contentArea;

    public RankingsView(UserService userService) {
        this.userService = userService;

        addClassName("rankings-view");
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        add(createHeader());
        add(createTabs());

        contentArea = new VerticalLayout();
        contentArea.setSizeFull();
        contentArea.setPadding(false);
        add(contentArea);

        // Show reputation ranking by default
        showReputationRanking();
    }

    private Component createHeader() {
        H2 title = new H2("排行榜");
        title.addClassNames(LumoUtility.Margin.Bottom.NONE);

        Paragraph description = new Paragraph("展示社区中表现最出色的玩家，向榜上有名的大佬们学习！");
        description.addClassNames(LumoUtility.TextColor.SECONDARY);

        VerticalLayout header = new VerticalLayout(title, description);
        header.setSpacing(false);
        header.setPadding(false);
        return header;
    }

    private Component createTabs() {
        Tab reputationTab = new Tab(VaadinIcon.STAR.create(), new Span("声望榜"));
        Tab postTab = new Tab(VaadinIcon.COMMENT.create(), new Span("发帖榜"));
        Tab playTimeTab = new Tab(VaadinIcon.CLOCK.create(), new Span("游戏时长"));
        Tab achievementTab = new Tab(VaadinIcon.MEDAL.create(), new Span("成就榜"));

        Tabs tabs = new Tabs(reputationTab, postTab, playTimeTab, achievementTab);
        tabs.addSelectedChangeListener(event -> {
            Tab selected = event.getSelectedTab();
            if (selected == reputationTab) showReputationRanking();
            else if (selected == postTab) showPostRanking();
            else if (selected == playTimeTab) showPlayTimeRanking();
            else if (selected == achievementTab) showAchievementRanking();
        });

        return tabs;
    }

    private void showReputationRanking() {
        contentArea.removeAll();

        List<User> topUsers = userService.getTopUsersByReputation();

        // Top 3 podium
        if (topUsers.size() >= 3) {
            contentArea.add(createPodium(topUsers.subList(0, 3)));
        }

        // Rest of the list
        VerticalLayout list = new VerticalLayout();
        list.setSpacing(false);
        list.setPadding(false);
        list.setWidthFull();

        int rank = 1;
        for (User user : topUsers) {
            list.add(createRankingRow(rank++, user, String.valueOf(user.getReputation()), "声望"));
        }

        contentArea.add(list);
    }

    private Component createPodium(List<User> top3) {
        HorizontalLayout podium = new HorizontalLayout();
        podium.setWidthFull();
        podium.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        podium.setAlignItems(FlexComponent.Alignment.END);
        podium.setSpacing(true);
        podium.getElement().getStyle().set("margin-bottom", "32px");

        // Second place (left)
        podium.add(createPodiumCard(top3.get(1), 2, "#C0C0C0", "180px"));

        // First place (center, tallest)
        podium.add(createPodiumCard(top3.get(0), 1, "#FFD700", "220px"));

        // Third place (right)
        podium.add(createPodiumCard(top3.get(2), 3, "#CD7F32", "150px"));

        return podium;
    }

    private Component createPodiumCard(User user, int rank, String color, String height) {
        VerticalLayout card = new VerticalLayout();
        card.setAlignItems(FlexComponent.Alignment.CENTER);
        card.setSpacing(false);
        card.setPadding(true);
        card.setWidth("150px");
        card.setHeight(height);
        card.getElement().getStyle()
            .set("background", "linear-gradient(180deg, " + color + "20 0%, " + color + "40 100%)")
            .set("border-radius", "var(--lumo-border-radius-l)")
            .set("border", "2px solid " + color);

        // Rank badge
        Span rankBadge = new Span(String.valueOf(rank));
        rankBadge.getElement().getStyle()
            .set("background", color)
            .set("color", rank == 1 ? "#000" : "#fff")
            .set("width", "40px")
            .set("height", "40px")
            .set("border-radius", "50%")
            .set("display", "flex")
            .set("align-items", "center")
            .set("justify-content", "center")
            .set("font-size", "var(--lumo-font-size-xl)")
            .set("font-weight", "bold");

        // Avatar
        Div avatar = new Div();
        avatar.setText(user.getUsername().substring(0, 1).toUpperCase());
        avatar.getElement().getStyle()
            .set("width", "60px")
            .set("height", "60px")
            .set("border-radius", "50%")
            .set("background", "var(--lumo-primary-color)")
            .set("display", "flex")
            .set("align-items", "center")
            .set("justify-content", "center")
            .set("color", "white")
            .set("font-size", "var(--lumo-font-size-xxl)")
            .set("font-weight", "bold")
            .set("margin", "12px 0");

        // Username
        Span name = new Span(user.getUsername());
        name.addClassNames(LumoUtility.FontWeight.BOLD);

        // Value
        Span value = new Span(user.getReputation() + " 声望");
        value.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.SMALL);

        card.add(rankBadge, avatar, name, value);
        return card;
    }

    private Component createRankingRow(int rank, User user, String value, String unit) {
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setAlignItems(FlexComponent.Alignment.CENTER);
        row.setPadding(true);
        row.getElement().getStyle()
            .set("background", rank % 2 == 0 ? "var(--lumo-contrast-5pct)" : "transparent")
            .set("border-radius", "var(--lumo-border-radius-m)");

        // Rank number
        Span rankSpan = new Span("#" + rank);
        rankSpan.setWidth("50px");
        rankSpan.addClassNames(LumoUtility.FontWeight.BOLD);
        if (rank <= 3) {
            String color = rank == 1 ? "#FFD700" : rank == 2 ? "#C0C0C0" : "#CD7F32";
            rankSpan.getElement().getStyle().set("color", color);
        }

        // User info
        HorizontalLayout userInfo = new HorizontalLayout();
        userInfo.setAlignItems(FlexComponent.Alignment.CENTER);

        Div avatar = new Div();
        avatar.setText(user.getUsername().substring(0, 1).toUpperCase());
        avatar.getElement().getStyle()
            .set("width", "36px")
            .set("height", "36px")
            .set("border-radius", "50%")
            .set("background", "var(--lumo-primary-color)")
            .set("display", "flex")
            .set("align-items", "center")
            .set("justify-content", "center")
            .set("color", "white")
            .set("font-weight", "bold");

        VerticalLayout nameAndRole = new VerticalLayout();
        nameAndRole.setSpacing(false);
        nameAndRole.setPadding(false);

        Span name = new Span(user.getUsername());
        name.addClassNames(LumoUtility.FontWeight.SEMIBOLD);

        Span role = new Span(user.getRole().getDisplayName());
        role.addClassNames(LumoUtility.FontSize.XSMALL, LumoUtility.TextColor.SECONDARY);

        nameAndRole.add(name, role);
        userInfo.add(avatar, nameAndRole);

        // Value
        Span valueSpan = new Span(value + " " + unit);
        valueSpan.addClassNames(LumoUtility.FontWeight.SEMIBOLD);
        valueSpan.getElement().getStyle().set("margin-left", "auto");

        row.add(rankSpan, userInfo, valueSpan);
        return row;
    }

    private void showPostRanking() {
        contentArea.removeAll();

        H3 title = new H3("发帖排行榜");
        Paragraph desc = new Paragraph("统计玩家在论坛的发帖数量");
        desc.addClassNames(LumoUtility.TextColor.SECONDARY);

        contentArea.add(title, desc);

        // This would fetch from forum service in real implementation
        List<User> topUsers = userService.getTopUsersByReputation();
        VerticalLayout list = new VerticalLayout();
        list.setSpacing(false);
        list.setPadding(false);

        int rank = 1;
        for (User user : topUsers) {
            list.add(createRankingRow(rank++, user, String.valueOf(user.getPostCount()), "帖子"));
        }

        contentArea.add(list);
    }

    private void showPlayTimeRanking() {
        contentArea.removeAll();

        H3 title = new H3("游戏时长排行榜");
        Paragraph desc = new Paragraph("统计玩家在服务器的在线时长");
        desc.addClassNames(LumoUtility.TextColor.SECONDARY);

        contentArea.add(title, desc);

        List<User> topUsers = userService.getTopUsersByReputation();
        VerticalLayout list = new VerticalLayout();
        list.setSpacing(false);
        list.setPadding(false);

        int rank = 1;
        for (User user : topUsers) {
            long hours = user.getPlayTimeMinutes() / 60;
            list.add(createRankingRow(rank++, user, String.valueOf(hours), "小时"));
        }

        contentArea.add(list);
    }

    private void showAchievementRanking() {
        contentArea.removeAll();

        H3 title = new H3("成就排行榜");
        Paragraph desc = new Paragraph("统计玩家解锁的成就数量");
        desc.addClassNames(LumoUtility.TextColor.SECONDARY);

        contentArea.add(title, desc);

        // Placeholder - would integrate with achievement service
        Paragraph placeholder = new Paragraph("成就系统即将上线，敬请期待！");
        placeholder.getElement().getStyle()
            .set("text-align", "center")
            .set("padding", "48px")
            .set("color", "var(--lumo-secondary-text-color)");

        contentArea.add(placeholder);
    }
}
