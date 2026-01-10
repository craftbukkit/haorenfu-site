/*
 * ═══════════════════════════════════════════════════════════════════════════
 *                            WIKI VIEW
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Community-editable knowledge base for server information, guides, and lore.
 * Supports versioned editing with Markdown rendering.
 */
package world.haorenfu.ui.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;
import world.haorenfu.ui.layout.MainLayout;

import java.util.List;

/**
 * Server wiki and knowledge base.
 */
@Route(value = "wiki", layout = MainLayout.class)
@PageTitle("百科全书 | 好人服")
@PermitAll
public class WikiView extends VerticalLayout {

    private VerticalLayout contentArea;

    public WikiView() {
        addClassName("wiki-view");
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        add(createHeader());

        HorizontalLayout mainContent = new HorizontalLayout();
        mainContent.setSizeFull();
        mainContent.setSpacing(true);

        mainContent.add(createSidebar());

        contentArea = new VerticalLayout();
        contentArea.setSizeFull();
        contentArea.setPadding(false);
        mainContent.add(contentArea);
        mainContent.setFlexGrow(1, contentArea);

        add(mainContent);

        // Show home page by default
        showWikiHome();
    }

    private Component createHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);

        H2 title = new H2("百科全书");
        title.addClassNames(LumoUtility.Margin.NONE);

        // Search
        TextField searchField = new TextField();
        searchField.setPlaceholder("搜索百科...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setWidth("300px");

        // Create button
        Button createButton = new Button("创建页面", VaadinIcon.PLUS.create());
        createButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout rightSection = new HorizontalLayout(searchField, createButton);
        rightSection.setAlignItems(FlexComponent.Alignment.CENTER);

        header.add(title);
        header.addAndExpand(new Span());
        header.add(rightSection);

        return header;
    }

    private Component createSidebar() {
        VerticalLayout sidebar = new VerticalLayout();
        sidebar.setWidth("250px");
        sidebar.setSpacing(false);
        sidebar.setPadding(false);
        sidebar.getElement().getStyle()
            .set("background", "var(--lumo-contrast-5pct)")
            .set("border-radius", "var(--lumo-border-radius-l)")
            .set("padding", "16px");

        // Categories
        H4 categoriesTitle = new H4("分类");
        categoriesTitle.addClassNames(LumoUtility.Margin.Bottom.SMALL);
        sidebar.add(categoriesTitle);

        List<CategoryItem> categories = List.of(
            new CategoryItem("📖", "新手指南", 12),
            new CategoryItem("🎮", "游戏玩法", 25),
            new CategoryItem("⚔️", "职业系统", 8),
            new CategoryItem("🏗️", "建筑教程", 15),
            new CategoryItem("⚡", "红石科技", 10),
            new CategoryItem("💰", "经济系统", 6),
            new CategoryItem("🗺️", "世界介绍", 9),
            new CategoryItem("📜", "服务器历史", 4),
            new CategoryItem("❓", "常见问题", 18)
        );

        for (CategoryItem category : categories) {
            sidebar.add(createCategoryItem(category));
        }

        // Recent edits
        H4 recentTitle = new H4("最近更新");
        recentTitle.addClassNames(LumoUtility.Margin.Top.LARGE, LumoUtility.Margin.Bottom.SMALL);
        sidebar.add(recentTitle);

        List<String> recentPages = List.of(
            "新手入门指南",
            "钻石矿寻找技巧",
            "红石活塞门教程",
            "服务器规则",
            "经济系统介绍"
        );

        for (String page : recentPages) {
            Anchor link = new Anchor("#", page);
            link.addClassNames(
                LumoUtility.FontSize.SMALL,
                LumoUtility.Display.BLOCK,
                LumoUtility.Padding.Vertical.XSMALL
            );
            sidebar.add(link);
        }

        return sidebar;
    }

    private Component createCategoryItem(CategoryItem category) {
        HorizontalLayout item = new HorizontalLayout();
        item.setWidthFull();
        item.setAlignItems(FlexComponent.Alignment.CENTER);
        item.getElement().getStyle()
            .set("padding", "8px")
            .set("border-radius", "var(--lumo-border-radius-m)")
            .set("cursor", "pointer");

        Span icon = new Span(category.icon);
        Span name = new Span(category.name);
        name.addClassNames(LumoUtility.FontSize.SMALL);

        Span count = new Span(String.valueOf(category.pageCount));
        count.addClassNames(LumoUtility.FontSize.XSMALL, LumoUtility.TextColor.SECONDARY);
        count.getElement().getStyle()
            .set("margin-left", "auto")
            .set("background", "var(--lumo-contrast-10pct)")
            .set("padding", "2px 6px")
            .set("border-radius", "10px");

        item.add(icon, name, count);
        return item;
    }

    private void showWikiHome() {
        contentArea.removeAll();

        // Featured article
        VerticalLayout featured = new VerticalLayout();
        featured.setPadding(true);
        featured.getElement().getStyle()
            .set("background", "linear-gradient(135deg, var(--lumo-primary-color-10pct), var(--lumo-contrast-5pct))")
            .set("border-radius", "var(--lumo-border-radius-l)");

        Span featuredBadge = new Span("⭐ 精选文章");
        featuredBadge.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY);

        H3 featuredTitle = new H3("新手入门完全指南");
        featuredTitle.addClassNames(LumoUtility.Margin.Vertical.SMALL);

        Paragraph featuredDesc = new Paragraph(
            "欢迎来到好人服！本指南将帮助你快速了解服务器的基本玩法、规则和社区文化。" +
            "无论你是 Minecraft 新手还是老玩家，这里都有你需要知道的一切。"
        );
        featuredDesc.addClassNames(LumoUtility.TextColor.SECONDARY);

        Button readButton = new Button("阅读全文", VaadinIcon.ARROW_RIGHT.create());
        readButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        readButton.setIconAfterText(true);

        featured.add(featuredBadge, featuredTitle, featuredDesc, readButton);
        contentArea.add(featured);

        // Popular pages grid
        H3 popularTitle = new H3("热门页面");
        contentArea.add(popularTitle);

        HorizontalLayout popularGrid = new HorizontalLayout();
        popularGrid.setWidthFull();
        popularGrid.setSpacing(true);
        popularGrid.getElement().getStyle().set("flex-wrap", "wrap");

        List<WikiPagePreview> popularPages = List.of(
            new WikiPagePreview("🎮", "游戏基础操作", "了解 Minecraft 的基本控制和界面", 1250),
            new WikiPagePreview("⛏️", "采矿效率指南", "如何快速有效地获取矿石资源", 980),
            new WikiPagePreview("🏠", "第一个庇护所", "建造你的第一个生存基地", 856),
            new WikiPagePreview("🗡️", "战斗技巧", "PvE 和 PvP 战斗的进阶技巧", 742),
            new WikiPagePreview("🌾", "农业自动化", "建造高效的自动农场", 698),
            new WikiPagePreview("⚡", "红石入门", "从零开始学习红石电路", 654)
        );

        for (WikiPagePreview page : popularPages) {
            popularGrid.add(createPageCard(page));
        }

        contentArea.add(popularGrid);

        // Quick links
        H3 quickLinksTitle = new H3("快速入口");
        contentArea.add(quickLinksTitle);

        HorizontalLayout quickLinks = new HorizontalLayout();
        quickLinks.setSpacing(true);

        quickLinks.add(createQuickLink("📜", "服务器规则", "必读"));
        quickLinks.add(createQuickLink("❓", "常见问题", "FAQ"));
        quickLinks.add(createQuickLink("🎁", "新手礼包", "福利"));
        quickLinks.add(createQuickLink("📞", "联系管理", "求助"));

        contentArea.add(quickLinks);
    }

    private Component createPageCard(WikiPagePreview page) {
        VerticalLayout card = new VerticalLayout();
        card.setSpacing(false);
        card.setPadding(true);
        card.setWidth("280px");
        card.getElement().getStyle()
            .set("background", "var(--lumo-contrast-5pct)")
            .set("border-radius", "var(--lumo-border-radius-m)")
            .set("cursor", "pointer")
            .set("transition", "transform 0.2s");

        Span icon = new Span(page.icon);
        icon.getElement().getStyle().set("font-size", "32px");

        H4 title = new H4(page.title);
        title.addClassNames(LumoUtility.Margin.Vertical.SMALL);

        Paragraph desc = new Paragraph(page.description);
        desc.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY);

        HorizontalLayout footer = new HorizontalLayout();
        footer.setWidthFull();
        footer.setAlignItems(FlexComponent.Alignment.CENTER);

        Icon viewIcon = VaadinIcon.EYE.create();
        viewIcon.setSize("14px");
        Span views = new Span(page.views + " 次浏览");
        views.addClassNames(LumoUtility.FontSize.XSMALL, LumoUtility.TextColor.SECONDARY);

        footer.add(viewIcon, views);

        card.add(icon, title, desc, footer);
        return card;
    }

    private Component createQuickLink(String icon, String title, String badge) {
        HorizontalLayout link = new HorizontalLayout();
        link.setAlignItems(FlexComponent.Alignment.CENTER);
        link.setPadding(true);
        link.getElement().getStyle()
            .set("background", "var(--lumo-contrast-5pct)")
            .set("border-radius", "var(--lumo-border-radius-m)")
            .set("cursor", "pointer");

        Span iconSpan = new Span(icon);
        iconSpan.getElement().getStyle().set("font-size", "24px");

        Span titleSpan = new Span(title);
        titleSpan.addClassNames(LumoUtility.FontWeight.SEMIBOLD);

        Span badgeSpan = new Span(badge);
        badgeSpan.getElement().getStyle()
            .set("background", "var(--lumo-primary-color)")
            .set("color", "white")
            .set("padding", "2px 8px")
            .set("border-radius", "10px")
            .set("font-size", "var(--lumo-font-size-xs)");

        link.add(iconSpan, titleSpan, badgeSpan);
        return link;
    }

    // Data records
    private record CategoryItem(String icon, String name, int pageCount) {}
    private record WikiPagePreview(String icon, String title, String description, int views) {}
}
