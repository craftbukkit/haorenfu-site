/*
 * ═══════════════════════════════════════════════════════════════════════════
 *                        ACHIEVEMENTS VIEW
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Gamification interface displaying available and earned achievements.
 * Inspired by gaming achievement systems with rarity-based styling.
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
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;
import world.haorenfu.ui.layout.MainLayout;

import java.util.List;

/**
 * Achievement browser and progress tracker.
 */
@Route(value = "achievements", layout = MainLayout.class)
@PageTitle("成就系统 | 好人服")
@PermitAll
public class AchievementsView extends VerticalLayout {

    private FlexLayout achievementGrid;

    public AchievementsView() {
        addClassName("achievements-view");
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        add(createHeader());
        add(createProgressSection());
        add(createCategoryTabs());

        achievementGrid = new FlexLayout();
        achievementGrid.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        achievementGrid.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
        achievementGrid.getElement().getStyle().set("gap", "16px");
        achievementGrid.setWidthFull();

        add(achievementGrid);

        loadAchievements("all");
    }

    private Component createHeader() {
        H2 title = new H2("成就系统");
        title.addClassNames(LumoUtility.Margin.Bottom.NONE);

        Paragraph description = new Paragraph("完成挑战，收集成就，展示你的实力！");
        description.addClassNames(LumoUtility.TextColor.SECONDARY);

        VerticalLayout header = new VerticalLayout(title, description);
        header.setSpacing(false);
        header.setPadding(false);
        return header;
    }

    private Component createProgressSection() {
        HorizontalLayout section = new HorizontalLayout();
        section.setWidthFull();
        section.setSpacing(true);
        section.getElement().getStyle()
            .set("background", "var(--lumo-contrast-5pct)")
            .set("border-radius", "var(--lumo-border-radius-l)")
            .set("padding", "24px");

        // Overall progress
        VerticalLayout progressCard = new VerticalLayout();
        progressCard.setSpacing(false);
        progressCard.setWidth("300px");

        Span progressTitle = new Span("总体进度");
        progressTitle.addClassNames(LumoUtility.FontWeight.SEMIBOLD);

        ProgressBar progressBar = new ProgressBar(0, 100, 35);
        progressBar.setWidthFull();

        Span progressText = new Span("35 / 100 成就已解锁");
        progressText.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY);

        progressCard.add(progressTitle, progressBar, progressText);

        // Points earned
        VerticalLayout pointsCard = createStatBox("总声望奖励", "2,450", VaadinIcon.STAR);

        // Rarest achievement
        VerticalLayout rarestCard = createStatBox("最稀有成就", "龙之主宰", VaadinIcon.DIAMOND);

        // Recent achievement
        VerticalLayout recentCard = createStatBox("最新解锁", "建筑大师", VaadinIcon.CLOCK);

        section.add(progressCard, pointsCard, rarestCard, recentCard);
        return section;
    }

    private VerticalLayout createStatBox(String label, String value, VaadinIcon icon) {
        VerticalLayout box = new VerticalLayout();
        box.setSpacing(false);
        box.setAlignItems(FlexComponent.Alignment.CENTER);
        box.setWidth("150px");

        Icon boxIcon = icon.create();
        boxIcon.setSize("32px");
        boxIcon.getElement().getStyle().set("color", "var(--lumo-primary-color)");

        Span valueSpan = new Span(value);
        valueSpan.addClassNames(LumoUtility.FontWeight.BOLD, LumoUtility.FontSize.LARGE);

        Span labelSpan = new Span(label);
        labelSpan.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY);

        box.add(boxIcon, valueSpan, labelSpan);
        return box;
    }

    private Component createCategoryTabs() {
        Tab allTab = new Tab("全部");
        Tab communityTab = new Tab("💬 社区");
        Tab explorationTab = new Tab("🗺️ 探索");
        Tab buildingTab = new Tab("🏗️ 建造");
        Tab combatTab = new Tab("⚔️ 战斗");
        Tab survivalTab = new Tab("🏕️ 生存");
        Tab redstoneTab = new Tab("⚡ 红石");
        Tab collectionTab = new Tab("📦 收集");
        Tab specialTab = new Tab("⭐ 特殊");

        Tabs tabs = new Tabs(allTab, communityTab, explorationTab, buildingTab,
                            combatTab, survivalTab, redstoneTab, collectionTab, specialTab);

        tabs.addSelectedChangeListener(event -> {
            String category = event.getSelectedTab().getLabel();
            loadAchievements(category);
        });

        return tabs;
    }

    private void loadAchievements(String category) {
        achievementGrid.removeAll();

        // Sample achievements
        List<AchievementData> achievements = List.of(
            new AchievementData("first_login", "初来乍到", "首次登录服务器", "🎮", "COMMON", true, 10),
            new AchievementData("first_post", "畅所欲言", "发布第一篇论坛帖子", "💬", "COMMON", true, 15),
            new AchievementData("builder_1", "小小建筑师", "完成第一个建筑作品", "🏠", "COMMON", true, 20),
            new AchievementData("miner_1", "矿工入门", "挖掘100个矿石", "⛏️", "COMMON", true, 15),
            new AchievementData("farmer_1", "田园牧歌", "种植1000株作物", "🌾", "UNCOMMON", true, 30),
            new AchievementData("redstone_1", "红石萌新", "制作第一个红石装置", "⚡", "UNCOMMON", true, 25),
            new AchievementData("explorer_1", "冒险起航", "探索10个不同的群系", "🗺️", "UNCOMMON", false, 40),
            new AchievementData("collector_1", "收藏家", "收集所有种类的羊毛", "📦", "RARE", false, 50),
            new AchievementData("pvp_1", "初战告捷", "在PvP中获得首胜", "⚔️", "RARE", false, 60),
            new AchievementData("builder_master", "建筑大师", "建造被评为精品的作品", "🏛️", "EPIC", true, 100),
            new AchievementData("dragon_slayer", "龙之主宰", "击败末影龙", "🐉", "EPIC", false, 150),
            new AchievementData("legendary_player", "传奇玩家", "达到传奇声望等级", "👑", "LEGENDARY", false, 500)
        );

        for (AchievementData achievement : achievements) {
            achievementGrid.add(createAchievementCard(achievement));
        }
    }

    private Component createAchievementCard(AchievementData data) {
        VerticalLayout card = new VerticalLayout();
        card.setSpacing(false);
        card.setPadding(true);
        card.setWidth("220px");
        card.setAlignItems(FlexComponent.Alignment.CENTER);

        // Rarity-based styling
        String rarityColor = switch (data.rarity) {
            case "COMMON" -> "#AAAAAA";
            case "UNCOMMON" -> "#1EFF00";
            case "RARE" -> "#0070DD";
            case "EPIC" -> "#A335EE";
            case "LEGENDARY" -> "#FF8000";
            default -> "#AAAAAA";
        };

        card.getElement().getStyle()
            .set("background", data.unlocked
                ? "linear-gradient(135deg, " + rarityColor + "20, " + rarityColor + "10)"
                : "var(--lumo-contrast-5pct)")
            .set("border-radius", "var(--lumo-border-radius-l)")
            .set("border", "2px solid " + (data.unlocked ? rarityColor : "transparent"))
            .set("opacity", data.unlocked ? "1" : "0.6")
            .set("transition", "transform 0.2s, box-shadow 0.2s")
            .set("cursor", "pointer");

        // Icon
        Span icon = new Span(data.icon);
        icon.getElement().getStyle()
            .set("font-size", "48px")
            .set("filter", data.unlocked ? "none" : "grayscale(100%)");

        // Name
        Span name = new Span(data.name);
        name.addClassNames(LumoUtility.FontWeight.BOLD);
        name.getElement().getStyle().set("color", data.unlocked ? rarityColor : "var(--lumo-secondary-text-color)");

        // Description
        Span desc = new Span(data.description);
        desc.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY);
        desc.getElement().getStyle().set("text-align", "center");

        // Rarity badge
        Span rarityBadge = new Span(getRarityName(data.rarity));
        rarityBadge.getElement().getStyle()
            .set("background", rarityColor)
            .set("color", data.rarity.equals("LEGENDARY") || data.rarity.equals("EPIC") ? "white" : "#000")
            .set("padding", "2px 8px")
            .set("border-radius", "12px")
            .set("font-size", "var(--lumo-font-size-xs)")
            .set("margin-top", "8px");

        // Reward
        HorizontalLayout reward = new HorizontalLayout();
        reward.setAlignItems(FlexComponent.Alignment.CENTER);
        reward.setSpacing(false);

        Icon starIcon = VaadinIcon.STAR.create();
        starIcon.setSize("14px");
        starIcon.getElement().getStyle().set("color", "#FFD700");

        Span rewardText = new Span("+" + data.reputationReward);
        rewardText.addClassNames(LumoUtility.FontSize.SMALL);

        reward.add(starIcon, rewardText);

        // Status indicator
        if (data.unlocked) {
            Icon checkIcon = VaadinIcon.CHECK_CIRCLE.create();
            checkIcon.setSize("20px");
            checkIcon.getElement().getStyle().set("color", "#4CAF50");
            card.add(checkIcon);
        }

        card.add(icon, name, desc, rarityBadge, reward);
        return card;
    }

    private String getRarityName(String rarity) {
        return switch (rarity) {
            case "COMMON" -> "普通";
            case "UNCOMMON" -> "稀有";
            case "RARE" -> "精良";
            case "EPIC" -> "史诗";
            case "LEGENDARY" -> "传说";
            default -> "普通";
        };
    }

    // Data record for achievements
    private record AchievementData(
        String code,
        String name,
        String description,
        String icon,
        String rarity,
        boolean unlocked,
        int reputationReward
    ) {}
}
