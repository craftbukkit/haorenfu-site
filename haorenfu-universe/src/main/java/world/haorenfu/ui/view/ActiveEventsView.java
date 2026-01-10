/*
 * ═══════════════════════════════════════════════════════════════════════════
 *                        ACTIVE EVENTS VIEW
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Displays ongoing server events, activities, and seasonal celebrations.
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
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;
import world.haorenfu.ui.layout.MainLayout;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Active server events and activities.
 */
@Route(value = "events", layout = MainLayout.class)
@PageTitle("服务器活动 | 好人服")
@PermitAll
public class ActiveEventsView extends VerticalLayout {

    public ActiveEventsView() {
        addClassName("events-view");
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        add(createHeader());
        add(createFeaturedEvent());
        add(createEventList());
        add(createUpcomingEvents());
    }

    private Component createHeader() {
        H2 title = new H2("服务器活动");
        title.addClassNames(LumoUtility.Margin.Bottom.NONE);

        Paragraph description = new Paragraph("参与精彩活动，赢取丰厚奖励！");
        description.addClassNames(LumoUtility.TextColor.SECONDARY);

        VerticalLayout header = new VerticalLayout(title, description);
        header.setSpacing(false);
        header.setPadding(false);
        return header;
    }

    private Component createFeaturedEvent() {
        VerticalLayout featured = new VerticalLayout();
        featured.setPadding(true);
        featured.setWidthFull();
        featured.getElement().getStyle()
            .set("background", "linear-gradient(135deg, #FF6B6B 0%, #FF8E53 100%)")
            .set("border-radius", "var(--lumo-border-radius-l)")
            .set("color", "white");

        Span badge = new Span("🔥 热门活动");
        badge.getElement().getStyle()
            .set("background", "rgba(255,255,255,0.2)")
            .set("padding", "4px 12px")
            .set("border-radius", "12px");

        H3 eventTitle = new H3("春节建筑大赛 2024");
        eventTitle.getElement().getStyle().set("color", "white");
        eventTitle.addClassNames(LumoUtility.Margin.Vertical.SMALL);

        Paragraph eventDesc = new Paragraph(
            "以「龙年腾飞」为主题，发挥创意建造令人惊叹的作品！优胜者将获得限定称号、声望奖励和实物周边！"
        );
        eventDesc.getElement().getStyle().set("color", "rgba(255,255,255,0.9)");

        HorizontalLayout stats = new HorizontalLayout();
        stats.setSpacing(true);
        stats.add(createEventStat("👥", "128 参与者"));
        stats.add(createEventStat("🏆", "10,000 声望池"));
        stats.add(createEventStat("⏰", "剩余 5 天"));

        Button joinButton = new Button("立即参加", VaadinIcon.ARROW_RIGHT.create());
        joinButton.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        joinButton.setIconAfterText(true);
        joinButton.getElement().getStyle()
            .set("background", "white")
            .set("color", "#FF6B6B");

        featured.add(badge, eventTitle, eventDesc, stats, joinButton);
        return featured;
    }

    private Component createEventStat(String icon, String text) {
        Span stat = new Span(icon + " " + text);
        stat.getElement().getStyle()
            .set("background", "rgba(255,255,255,0.2)")
            .set("padding", "4px 12px")
            .set("border-radius", "8px")
            .set("font-size", "var(--lumo-font-size-s)");
        return stat;
    }

    private Component createEventList() {
        VerticalLayout section = new VerticalLayout();
        section.setSpacing(true);
        section.setPadding(false);

        H3 title = new H3("进行中的活动");
        section.add(title);

        FlexLayout grid = new FlexLayout();
        grid.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        grid.getElement().getStyle().set("gap", "16px");

        List<EventData> events = List.of(
            new EventData("🎯", "每日签到", "连续签到获取递增奖励", "永久", "#4CAF50", 0.75),
            new EventData("⛏️", "挖矿马拉松", "本周挖掘最多矿石的玩家", "3天后结束", "#2196F3", 0.4),
            new EventData("🎣", "钓鱼大师", "钓到稀有物品获得额外奖励", "5天后结束", "#9C27B0", 0.6),
            new EventData("👹", "怪物猎人", "击败1000只怪物", "限时挑战", "#F44336", 0.25)
        );

        for (EventData event : events) {
            grid.add(createEventCard(event));
        }

        section.add(grid);
        return section;
    }

    private Component createEventCard(EventData event) {
        VerticalLayout card = new VerticalLayout();
        card.setSpacing(false);
        card.setPadding(true);
        card.setWidth("280px");
        card.getElement().getStyle()
            .set("background", "var(--lumo-contrast-5pct)")
            .set("border-radius", "var(--lumo-border-radius-l)")
            .set("border-top", "4px solid " + event.color);

        Span icon = new Span(event.icon);
        icon.getElement().getStyle().set("font-size", "36px");

        H4 title = new H4(event.title);
        title.addClassNames(LumoUtility.Margin.Vertical.SMALL);

        Paragraph desc = new Paragraph(event.description);
        desc.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY);

        Span deadline = new Span(event.deadline);
        deadline.addClassNames(LumoUtility.FontSize.XSMALL);
        deadline.getElement().getStyle()
            .set("color", event.color)
            .set("font-weight", "bold");

        // Progress bar
        VerticalLayout progressSection = new VerticalLayout();
        progressSection.setSpacing(false);
        progressSection.setPadding(false);
        progressSection.setWidthFull();

        Span progressLabel = new Span("你的进度");
        progressLabel.addClassNames(LumoUtility.FontSize.XSMALL, LumoUtility.TextColor.SECONDARY);

        ProgressBar progressBar = new ProgressBar(0, 1, event.progress);
        progressBar.setWidthFull();
        progressBar.getElement().getStyle().set("--lumo-primary-color", event.color);

        Span progressText = new Span(Math.round(event.progress * 100) + "%");
        progressText.addClassNames(LumoUtility.FontSize.XSMALL);

        progressSection.add(progressLabel, progressBar, progressText);

        card.add(icon, title, desc, deadline, progressSection);
        return card;
    }

    private Component createUpcomingEvents() {
        VerticalLayout section = new VerticalLayout();
        section.setSpacing(true);
        section.setPadding(false);

        H3 title = new H3("即将开始");
        section.add(title);

        List<UpcomingEvent> upcoming = List.of(
            new UpcomingEvent("🎉", "周年庆典", "2月15日", "服务器成立11周年庆祝活动"),
            new UpcomingEvent("🏆", "PvP锦标赛", "2月20日", "年度PvP冠军争夺战"),
            new UpcomingEvent("🌸", "春季建筑季", "3月1日", "春季主题建筑创作季")
        );

        for (UpcomingEvent event : upcoming) {
            section.add(createUpcomingCard(event));
        }

        return section;
    }

    private Component createUpcomingCard(UpcomingEvent event) {
        HorizontalLayout card = new HorizontalLayout();
        card.setWidthFull();
        card.setAlignItems(FlexComponent.Alignment.CENTER);
        card.setPadding(true);
        card.getElement().getStyle()
            .set("background", "var(--lumo-contrast-5pct)")
            .set("border-radius", "var(--lumo-border-radius-m)");

        Span icon = new Span(event.icon);
        icon.getElement().getStyle().set("font-size", "32px");

        VerticalLayout info = new VerticalLayout();
        info.setSpacing(false);
        info.setPadding(false);

        Span title = new Span(event.title);
        title.addClassNames(LumoUtility.FontWeight.SEMIBOLD);

        Span desc = new Span(event.description);
        desc.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY);

        info.add(title, desc);

        Span date = new Span(event.startDate);
        date.addClassNames(LumoUtility.FontWeight.BOLD);
        date.getElement().getStyle()
            .set("margin-left", "auto")
            .set("color", "var(--lumo-primary-color)");

        card.add(icon, info, date);
        return card;
    }

    // Data records
    private record EventData(String icon, String title, String description, String deadline, String color, double progress) {}
    private record UpcomingEvent(String icon, String title, String startDate, String description) {}
}
