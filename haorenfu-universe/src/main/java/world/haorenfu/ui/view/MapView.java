/*
 * ═══════════════════════════════════════════════════════════════════════════
 *                            MAP VIEW
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Interactive server map viewer with points of interest and teleport locations.
 * Integrates with Dynmap/Bluemap for real-time world visualization.
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
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;
import world.haorenfu.ui.layout.MainLayout;

import java.util.List;

/**
 * Interactive map viewer for the Minecraft server.
 */
@Route(value = "map", layout = MainLayout.class)
@PageTitle("服务器地图 | 好人服")
@PermitAll
public class MapView extends VerticalLayout {

    public MapView() {
        addClassName("map-view");
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        add(createHeader());
        add(createMapSection());
        add(createPointsOfInterest());
    }

    private Component createHeader() {
        H2 title = new H2("服务器地图");
        title.addClassNames(LumoUtility.Margin.Bottom.NONE);

        Paragraph description = new Paragraph("探索我们的世界，发现隐藏的宝藏和玩家建造的奇迹！");
        description.addClassNames(LumoUtility.TextColor.SECONDARY);

        // Coordinates search
        TextField coordSearch = new TextField();
        coordSearch.setPlaceholder("输入坐标 (如: 100, 64, -200)");
        coordSearch.setPrefixComponent(VaadinIcon.SEARCH.create());
        coordSearch.setWidth("300px");

        HorizontalLayout headerRow = new HorizontalLayout();
        headerRow.setWidthFull();
        headerRow.setAlignItems(FlexComponent.Alignment.CENTER);

        VerticalLayout titleSection = new VerticalLayout(title, description);
        titleSection.setSpacing(false);
        titleSection.setPadding(false);

        headerRow.add(titleSection, coordSearch);
        headerRow.setFlexGrow(1, titleSection);

        return headerRow;
    }

    private Component createMapSection() {
        // Map container (would embed Dynmap/Bluemap iframe in production)
        Div mapContainer = new Div();
        mapContainer.setWidthFull();
        mapContainer.setHeight("500px");
        mapContainer.getElement().getStyle()
            .set("background", "linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%)")
            .set("border-radius", "var(--lumo-border-radius-l)")
            .set("display", "flex")
            .set("align-items", "center")
            .set("justify-content", "center")
            .set("position", "relative")
            .set("overflow", "hidden");

        // Map placeholder with grid pattern
        Div gridOverlay = new Div();
        gridOverlay.getElement().getStyle()
            .set("position", "absolute")
            .set("top", "0")
            .set("left", "0")
            .set("right", "0")
            .set("bottom", "0")
            .set("background-image", "linear-gradient(rgba(255,255,255,0.1) 1px, transparent 1px), linear-gradient(90deg, rgba(255,255,255,0.1) 1px, transparent 1px)")
            .set("background-size", "50px 50px");

        // Center content
        VerticalLayout centerContent = new VerticalLayout();
        centerContent.setAlignItems(FlexComponent.Alignment.CENTER);
        centerContent.setSpacing(false);

        Icon globeIcon = VaadinIcon.GLOBE.create();
        globeIcon.setSize("64px");
        globeIcon.getElement().getStyle().set("color", "rgba(255,255,255,0.5)");

        Span mapText = new Span("交互式地图");
        mapText.getElement().getStyle()
            .set("color", "rgba(255,255,255,0.7)")
            .set("font-size", "var(--lumo-font-size-xl)")
            .set("margin-top", "16px");

        Span mapHint = new Span("连接 Dynmap: map.haorenfu.cn:8123");
        mapHint.getElement().getStyle()
            .set("color", "rgba(255,255,255,0.5)")
            .set("font-size", "var(--lumo-font-size-s)")
            .set("margin-top", "8px");

        centerContent.add(globeIcon, mapText, mapHint);

        // Compass
        Div compass = createCompass();
        compass.getElement().getStyle()
            .set("position", "absolute")
            .set("top", "16px")
            .set("right", "16px");

        // World selector
        HorizontalLayout worldSelector = new HorizontalLayout();
        worldSelector.getElement().getStyle()
            .set("position", "absolute")
            .set("bottom", "16px")
            .set("left", "16px")
            .set("background", "rgba(0,0,0,0.5)")
            .set("padding", "8px 16px")
            .set("border-radius", "var(--lumo-border-radius-m)");

        worldSelector.add(
            createWorldButton("🌍", "主世界", true),
            createWorldButton("🔥", "下界", false),
            createWorldButton("🌌", "末地", false)
        );

        mapContainer.add(gridOverlay, centerContent, compass, worldSelector);
        return mapContainer;
    }

    private Div createCompass() {
        Div compass = new Div();
        compass.getElement().getStyle()
            .set("width", "60px")
            .set("height", "60px")
            .set("background", "rgba(0,0,0,0.5)")
            .set("border-radius", "50%")
            .set("display", "flex")
            .set("align-items", "center")
            .set("justify-content", "center")
            .set("font-size", "24px");
        compass.setText("🧭");
        return compass;
    }

    private Component createWorldButton(String emoji, String name, boolean active) {
        Span button = new Span(emoji + " " + name);
        button.getElement().getStyle()
            .set("color", active ? "#4CAF50" : "rgba(255,255,255,0.7)")
            .set("cursor", "pointer")
            .set("padding", "4px 8px")
            .set("border-radius", "var(--lumo-border-radius-s)")
            .set("background", active ? "rgba(76,175,80,0.2)" : "transparent");
        return button;
    }

    private Component createPointsOfInterest() {
        VerticalLayout section = new VerticalLayout();
        section.setSpacing(true);
        section.setPadding(false);

        H3 title = new H3("地标位置");
        title.addClassNames(LumoUtility.Margin.Bottom.SMALL);
        section.add(title);

        FlexLayout grid = new FlexLayout();
        grid.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        grid.getElement().getStyle().set("gap", "16px");

        // Points of interest
        List<POIData> pois = List.of(
            new POIData("🏠", "新手出生点", "0, 64, 0", "safe"),
            new POIData("🏪", "交易中心", "150, 70, -200", "shop"),
            new POIData("🏛️", "社区大厅", "-100, 65, 50", "community"),
            new POIData("🌾", "公共农场", "300, 68, 100", "farm"),
            new POIData("⛏️", "矿区入口", "-250, 45, -150", "mine"),
            new POIData("🎮", "小游戏区", "500, 64, 500", "game"),
            new POIData("🏰", "古堡遗址", "-800, 80, 600", "landmark"),
            new POIData("🌊", "海洋神殿", "1200, 40, -900", "landmark"),
            new POIData("🏔️", "雪山基地", "-500, 120, 1000", "base"),
            new POIData("🌸", "樱花村", "800, 70, 300", "village")
        );

        for (POIData poi : pois) {
            grid.add(createPOICard(poi));
        }

        section.add(grid);
        return section;
    }

    private Component createPOICard(POIData poi) {
        HorizontalLayout card = new HorizontalLayout();
        card.setAlignItems(FlexComponent.Alignment.CENTER);
        card.setSpacing(true);
        card.setWidth("280px");
        card.getElement().getStyle()
            .set("background", "var(--lumo-contrast-5pct)")
            .set("border-radius", "var(--lumo-border-radius-m)")
            .set("padding", "12px")
            .set("cursor", "pointer")
            .set("transition", "background 0.2s");

        // Icon
        Span emoji = new Span(poi.icon);
        emoji.getElement().getStyle()
            .set("font-size", "28px")
            .set("width", "40px")
            .set("text-align", "center");

        // Info
        VerticalLayout info = new VerticalLayout();
        info.setSpacing(false);
        info.setPadding(false);

        Span name = new Span(poi.name);
        name.addClassNames(LumoUtility.FontWeight.SEMIBOLD);

        Span coords = new Span(poi.coordinates);
        coords.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY);

        info.add(name, coords);

        // Navigation icon
        Icon navIcon = VaadinIcon.MAP_MARKER.create();
        navIcon.setSize("20px");
        navIcon.getElement().getStyle()
            .set("color", "var(--lumo-primary-color)")
            .set("margin-left", "auto");

        card.add(emoji, info, navIcon);
        return card;
    }

    // Data record for points of interest
    private record POIData(String icon, String name, String coordinates, String type) {}
}
