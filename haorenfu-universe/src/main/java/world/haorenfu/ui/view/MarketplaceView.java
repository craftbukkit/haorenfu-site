/*
 * ═══════════════════════════════════════════════════════════════════════════
 *                        MARKETPLACE VIEW
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * In-game trading marketplace for buying, selling, and auctioning items.
 * Features real-time bidding, price history, and trust ratings.
 */
package world.haorenfu.ui.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;
import world.haorenfu.ui.layout.MainLayout;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Trading marketplace interface.
 */
@Route(value = "marketplace", layout = MainLayout.class)
@PageTitle("交易市场 | 好人服")
@PermitAll
public class MarketplaceView extends VerticalLayout {

    private FlexLayout listingGrid;

    public MarketplaceView() {
        addClassName("marketplace-view");
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        add(createHeader());
        add(createStats());
        add(createFilterBar());
        add(createTabs());

        listingGrid = new FlexLayout();
        listingGrid.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        listingGrid.getElement().getStyle().set("gap", "16px");
        listingGrid.setWidthFull();

        add(listingGrid);

        loadListings("all");
    }

    private Component createHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);

        VerticalLayout titleSection = new VerticalLayout();
        titleSection.setSpacing(false);
        titleSection.setPadding(false);

        H2 title = new H2("交易市场");
        title.addClassNames(LumoUtility.Margin.NONE);

        Paragraph description = new Paragraph("安全便捷的游戏内物品交易平台，支持一口价和拍卖模式");
        description.addClassNames(LumoUtility.TextColor.SECONDARY);

        titleSection.add(title, description);

        // Action buttons
        HorizontalLayout actions = new HorizontalLayout();

        Button createListing = new Button("发布商品", VaadinIcon.PLUS.create());
        createListing.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button myListings = new Button("我的商品", VaadinIcon.PACKAGE.create());

        Button myOrders = new Button("我的订单", VaadinIcon.CART.create());

        actions.add(createListing, myListings, myOrders);

        header.add(titleSection, actions);
        header.setFlexGrow(1, titleSection);

        return header;
    }

    private Component createStats() {
        HorizontalLayout stats = new HorizontalLayout();
        stats.setWidthFull();
        stats.setSpacing(true);
        stats.getElement().getStyle()
            .set("background", "var(--lumo-contrast-5pct)")
            .set("border-radius", "var(--lumo-border-radius-l)")
            .set("padding", "16px");

        stats.add(createStatCard("📦", "在售商品", "1,234"));
        stats.add(createStatCard("🔨", "进行中拍卖", "56"));
        stats.add(createStatCard("💎", "今日交易额", "2,450"));
        stats.add(createStatCard("✅", "完成交易", "8,901"));

        return stats;
    }

    private Component createStatCard(String emoji, String label, String value) {
        VerticalLayout card = new VerticalLayout();
        card.setSpacing(false);
        card.setAlignItems(FlexComponent.Alignment.CENTER);
        card.setWidth("150px");

        Span emojiSpan = new Span(emoji);
        emojiSpan.getElement().getStyle().set("font-size", "24px");

        Span valueSpan = new Span(value);
        valueSpan.addClassNames(LumoUtility.FontSize.XLARGE, LumoUtility.FontWeight.BOLD);

        Span labelSpan = new Span(label);
        labelSpan.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY);

        card.add(emojiSpan, valueSpan, labelSpan);
        return card;
    }

    private Component createFilterBar() {
        HorizontalLayout filterBar = new HorizontalLayout();
        filterBar.setWidthFull();
        filterBar.setAlignItems(FlexComponent.Alignment.END);
        filterBar.setSpacing(true);

        TextField searchField = new TextField();
        searchField.setPlaceholder("搜索商品...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setWidth("250px");

        Select<String> categorySelect = new Select<>();
        categorySelect.setLabel("分类");
        categorySelect.setItems("全部", "工具", "武器", "护甲", "方块", "红石", "药水", "食物", "材料", "附魔物品", "稀有物品", "服务");
        categorySelect.setValue("全部");

        Select<String> typeSelect = new Select<>();
        typeSelect.setLabel("类型");
        typeSelect.setItems("全部", "一口价", "拍卖", "以物换物", "服务");
        typeSelect.setValue("全部");

        Select<String> currencySelect = new Select<>();
        currencySelect.setLabel("货币");
        currencySelect.setItems("全部", "💎 钻石", "💚 绿宝石", "🥇 金锭", "🔩 铁锭");
        currencySelect.setValue("全部");

        Select<String> sortSelect = new Select<>();
        sortSelect.setLabel("排序");
        sortSelect.setItems("最新", "价格从低到高", "价格从高到低", "最多浏览", "即将结束");
        sortSelect.setValue("最新");

        filterBar.add(searchField, categorySelect, typeSelect, currencySelect, sortSelect);

        return filterBar;
    }

    private Component createTabs() {
        Tab allTab = new Tab("全部商品");
        Tab auctionTab = new Tab("🔨 拍卖");
        Tab fixedTab = new Tab("💰 一口价");
        Tab serviceTab = new Tab("🛠️ 服务");
        Tab endingSoonTab = new Tab("⏰ 即将结束");

        Tabs tabs = new Tabs(allTab, auctionTab, fixedTab, serviceTab, endingSoonTab);
        tabs.addSelectedChangeListener(event -> {
            Tab selected = event.getSelectedTab();
            if (selected == allTab) loadListings("all");
            else if (selected == auctionTab) loadListings("auction");
            else if (selected == fixedTab) loadListings("fixed");
            else if (selected == serviceTab) loadListings("service");
            else if (selected == endingSoonTab) loadListings("ending");
        });

        return tabs;
    }

    private void loadListings(String filter) {
        listingGrid.removeAll();

        List<ListingData> listings = List.of(
            new ListingData("钻石镐 效率5 耐久3", "tools", "FIXED", BigDecimal.valueOf(15), "DIAMOND",
                           "BuilderPro", 4.8, 234, null, null),
            new ListingData("附魔金苹果 x5", "food", "AUCTION", BigDecimal.valueOf(25), "DIAMOND",
                           "FarmKing", 4.9, 567, BigDecimal.valueOf(32), Instant.now().plus(Duration.ofHours(2))),
            new ListingData("鞘翅", "rare", "AUCTION", BigDecimal.valueOf(50), "DIAMOND",
                           "ExplorerX", 4.7, 891, BigDecimal.valueOf(78), Instant.now().plus(Duration.ofHours(5))),
            new ListingData("海晶灯 x64", "blocks", "FIXED", BigDecimal.valueOf(8), "DIAMOND",
                           "OceanBuilder", 4.5, 123, null, null),
            new ListingData("红石建筑服务", "service", "SERVICE", BigDecimal.valueOf(100), "DIAMOND",
                           "RedstoneMaster", 5.0, 45, null, null),
            new ListingData("下界合金套装", "armor", "AUCTION", BigDecimal.valueOf(200), "DIAMOND",
                           "NetherKnight", 4.6, 1234, BigDecimal.valueOf(256), Instant.now().plus(Duration.ofMinutes(30)))
        );

        for (ListingData listing : listings) {
            listingGrid.add(createListingCard(listing));
        }
    }

    private Component createListingCard(ListingData listing) {
        VerticalLayout card = new VerticalLayout();
        card.setSpacing(false);
        card.setPadding(false);
        card.setWidth("280px");
        card.getElement().getStyle()
            .set("background", "var(--lumo-contrast-5pct)")
            .set("border-radius", "var(--lumo-border-radius-l)")
            .set("overflow", "hidden");

        // Item preview
        Div preview = new Div();
        preview.setWidthFull();
        preview.setHeight("120px");
        preview.getElement().getStyle()
            .set("background", getItemBackground(listing.category))
            .set("display", "flex")
            .set("align-items", "center")
            .set("justify-content", "center")
            .set("position", "relative");

        Span itemIcon = new Span(getItemIcon(listing.category));
        itemIcon.getElement().getStyle().set("font-size", "48px");
        preview.add(itemIcon);

        // Type badge
        Span typeBadge = new Span(getTypeLabel(listing.type));
        typeBadge.getElement().getStyle()
            .set("position", "absolute")
            .set("top", "8px")
            .set("left", "8px")
            .set("background", getTypeColor(listing.type))
            .set("color", "white")
            .set("padding", "2px 8px")
            .set("border-radius", "12px")
            .set("font-size", "var(--lumo-font-size-xs)");
        preview.add(typeBadge);

        // Ending soon indicator
        if (listing.endTime != null) {
            Duration remaining = Duration.between(Instant.now(), listing.endTime);
            if (remaining.toHours() < 1) {
                Span urgentBadge = new Span("⏰ " + remaining.toMinutes() + "分钟");
                urgentBadge.getElement().getStyle()
                    .set("position", "absolute")
                    .set("top", "8px")
                    .set("right", "8px")
                    .set("background", "#F44336")
                    .set("color", "white")
                    .set("padding", "2px 8px")
                    .set("border-radius", "12px")
                    .set("font-size", "var(--lumo-font-size-xs)")
                    .set("animation", "pulse 1s infinite");
                preview.add(urgentBadge);
            }
        }

        // Info section
        VerticalLayout info = new VerticalLayout();
        info.setSpacing(false);
        info.setPadding(true);

        H4 title = new H4(listing.title);
        title.addClassNames(LumoUtility.Margin.NONE, LumoUtility.FontSize.MEDIUM);
        title.getElement().getStyle()
            .set("overflow", "hidden")
            .set("text-overflow", "ellipsis")
            .set("white-space", "nowrap");

        // Seller info
        HorizontalLayout sellerRow = new HorizontalLayout();
        sellerRow.setAlignItems(FlexComponent.Alignment.CENTER);
        sellerRow.setSpacing(false);

        Span sellerName = new Span(listing.seller);
        sellerName.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY);

        Span rating = new Span(" ⭐ " + listing.rating);
        rating.addClassNames(LumoUtility.FontSize.SMALL);
        rating.getElement().getStyle().set("color", "#FFD700");

        sellerRow.add(sellerName, rating);

        // Price section
        HorizontalLayout priceRow = new HorizontalLayout();
        priceRow.setWidthFull();
        priceRow.setAlignItems(FlexComponent.Alignment.BASELINE);
        priceRow.getElement().getStyle().set("margin-top", "8px");

        if (listing.type.equals("AUCTION") && listing.currentBid != null) {
            VerticalLayout bidInfo = new VerticalLayout();
            bidInfo.setSpacing(false);
            bidInfo.setPadding(false);

            Span bidLabel = new Span("当前出价");
            bidLabel.addClassNames(LumoUtility.FontSize.XSMALL, LumoUtility.TextColor.SECONDARY);

            Span bidValue = new Span(getCurrencyEmoji(listing.currency) + " " + listing.currentBid);
            bidValue.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.FontWeight.BOLD);
            bidValue.getElement().getStyle().set("color", "var(--lumo-primary-color)");

            bidInfo.add(bidLabel, bidValue);
            priceRow.add(bidInfo);
        } else {
            Span price = new Span(getCurrencyEmoji(listing.currency) + " " + listing.price);
            price.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.FontWeight.BOLD);
            price.getElement().getStyle().set("color", "var(--lumo-primary-color)");
            priceRow.add(price);
        }

        // Views
        Span views = new Span("👁 " + listing.views);
        views.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY);
        views.getElement().getStyle().set("margin-left", "auto");
        priceRow.add(views);

        info.add(title, sellerRow, priceRow);

        // Action button
        Button actionBtn;
        if (listing.type.equals("AUCTION")) {
            actionBtn = new Button("出价", VaadinIcon.GAVEL.create());
            actionBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        } else if (listing.type.equals("SERVICE")) {
            actionBtn = new Button("咨询", VaadinIcon.COMMENT.create());
            actionBtn.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        } else {
            actionBtn = new Button("购买", VaadinIcon.CART.create());
            actionBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        }
        actionBtn.setWidthFull();

        VerticalLayout actionSection = new VerticalLayout(actionBtn);
        actionSection.setPadding(true);
        actionSection.setSpacing(false);

        card.add(preview, info, actionSection);
        return card;
    }

    private String getItemIcon(String category) {
        return switch (category) {
            case "tools" -> "⛏️";
            case "weapons" -> "🗡️";
            case "armor" -> "🛡️";
            case "blocks" -> "🧱";
            case "redstone" -> "⚡";
            case "potions" -> "🧪";
            case "food" -> "🍎";
            case "materials" -> "💎";
            case "rare" -> "✨";
            case "service" -> "🛠️";
            default -> "📦";
        };
    }

    private String getItemBackground(String category) {
        return switch (category) {
            case "rare" -> "linear-gradient(135deg, #FFD700 0%, #FFA500 100%)";
            case "tools" -> "linear-gradient(135deg, #607D8B 0%, #455A64 100%)";
            case "weapons" -> "linear-gradient(135deg, #F44336 0%, #C62828 100%)";
            case "armor" -> "linear-gradient(135deg, #3F51B5 0%, #1A237E 100%)";
            case "service" -> "linear-gradient(135deg, #4CAF50 0%, #2E7D32 100%)";
            default -> "linear-gradient(135deg, #9E9E9E 0%, #616161 100%)";
        };
    }

    private String getTypeLabel(String type) {
        return switch (type) {
            case "AUCTION" -> "🔨 拍卖";
            case "FIXED" -> "💰 一口价";
            case "TRADE" -> "🔄 换物";
            case "SERVICE" -> "🛠️ 服务";
            default -> type;
        };
    }

    private String getTypeColor(String type) {
        return switch (type) {
            case "AUCTION" -> "#FF9800";
            case "FIXED" -> "#4CAF50";
            case "TRADE" -> "#2196F3";
            case "SERVICE" -> "#9C27B0";
            default -> "#757575";
        };
    }

    private String getCurrencyEmoji(String currency) {
        return switch (currency) {
            case "DIAMOND" -> "💎";
            case "EMERALD" -> "💚";
            case "GOLD" -> "🥇";
            case "IRON" -> "🔩";
            default -> "💰";
        };
    }

    // Data record
    private record ListingData(String title, String category, String type,
                               BigDecimal price, String currency, String seller,
                               double rating, int views, BigDecimal currentBid,
                               Instant endTime) {}
}
