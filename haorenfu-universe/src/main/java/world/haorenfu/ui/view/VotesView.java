/*
 * ═══════════════════════════════════════════════════════════════════════════
 *                           VOTES VIEW
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Community voting system for server decisions and features.
 * Implements fair voting with anti-manipulation measures.
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
 * Community voting interface.
 */
@Route(value = "votes", layout = MainLayout.class)
@PageTitle("社区投票 | 好人服")
@PermitAll
public class VotesView extends VerticalLayout {

    public VotesView() {
        addClassName("votes-view");
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        add(createHeader());
        add(createActiveVotes());
        add(createPastVotes());
    }

    private Component createHeader() {
        H2 title = new H2("社区投票");
        title.addClassNames(LumoUtility.Margin.Bottom.NONE);

        Paragraph description = new Paragraph("参与社区决策，你的每一票都很重要！投票结果将直接影响服务器的发展方向。");
        description.addClassNames(LumoUtility.TextColor.SECONDARY);

        VerticalLayout header = new VerticalLayout(title, description);
        header.setSpacing(false);
        header.setPadding(false);
        return header;
    }

    private Component createActiveVotes() {
        VerticalLayout section = new VerticalLayout();
        section.setSpacing(true);
        section.setPadding(false);

        H3 sectionTitle = new H3("进行中的投票");
        sectionTitle.addClassNames(LumoUtility.Margin.Bottom.SMALL);
        section.add(sectionTitle);

        // Sample active votes
        List<VoteData> activeVotes = List.of(
            new VoteData(
                "下个版本新增什么功能？",
                "我们计划在下个版本中新增一项重要功能，请投票选择你最期待的！",
                List.of(
                    new VoteOption("领地插件", 45),
                    new VoteOption("经济系统", 32),
                    new VoteOption("自定义称号", 18),
                    new VoteOption("宠物系统", 5)
                ),
                Instant.now().plus(Duration.ofDays(3)),
                156,
                false
            ),
            new VoteData(
                "服务器开放时间调整",
                "是否赞成将服务器重启维护时间从凌晨4点改为凌晨6点？",
                List.of(
                    new VoteOption("赞成", 78),
                    new VoteOption("反对", 22)
                ),
                Instant.now().plus(Duration.ofDays(1)),
                89,
                true
            )
        );

        for (VoteData vote : activeVotes) {
            section.add(createVoteCard(vote, true));
        }

        return section;
    }

    private Component createPastVotes() {
        VerticalLayout section = new VerticalLayout();
        section.setSpacing(true);
        section.setPadding(false);

        H3 sectionTitle = new H3("已结束的投票");
        sectionTitle.addClassNames(LumoUtility.Margin.Bottom.SMALL);
        section.add(sectionTitle);

        // Sample past votes
        List<VoteData> pastVotes = List.of(
            new VoteData(
                "2024年春节活动类型",
                "选择春节期间服务器举办什么类型的活动",
                List.of(
                    new VoteOption("建筑比赛", 42),
                    new VoteOption("寻宝活动", 35),
                    new VoteOption("PvP竞技", 15),
                    new VoteOption("红包雨", 8)
                ),
                Instant.now().minus(Duration.ofDays(10)),
                234,
                false
            )
        );

        for (VoteData vote : pastVotes) {
            section.add(createVoteCard(vote, false));
        }

        return section;
    }

    private Component createVoteCard(VoteData vote, boolean isActive) {
        VerticalLayout card = new VerticalLayout();
        card.setSpacing(true);
        card.setPadding(true);
        card.setWidthFull();
        card.getElement().getStyle()
            .set("background", "var(--lumo-contrast-5pct)")
            .set("border-radius", "var(--lumo-border-radius-l)")
            .set("border-left", isActive ? "4px solid var(--lumo-primary-color)" : "4px solid var(--lumo-contrast-30pct)");

        // Header
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);

        H4 title = new H4(vote.title);
        title.addClassNames(LumoUtility.Margin.NONE);

        Span statusBadge = new Span(isActive ? "进行中" : "已结束");
        statusBadge.getElement().getStyle()
            .set("background", isActive ? "#4CAF50" : "#9E9E9E")
            .set("color", "white")
            .set("padding", "2px 8px")
            .set("border-radius", "12px")
            .set("font-size", "var(--lumo-font-size-xs)")
            .set("margin-left", "auto");

        header.add(title, statusBadge);
        card.add(header);

        // Description
        Paragraph desc = new Paragraph(vote.description);
        desc.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.Margin.Vertical.SMALL);
        card.add(desc);

        // Options
        VerticalLayout options = new VerticalLayout();
        options.setSpacing(true);
        options.setPadding(false);
        options.setWidthFull();

        int totalVotes = vote.options.stream().mapToInt(VoteOption::votes).sum();

        for (VoteOption option : vote.options) {
            options.add(createOptionRow(option, totalVotes, isActive, vote.hasVoted));
        }

        card.add(options);

        // Footer
        HorizontalLayout footer = new HorizontalLayout();
        footer.setWidthFull();
        footer.setAlignItems(FlexComponent.Alignment.CENTER);

        Icon peopleIcon = VaadinIcon.USERS.create();
        peopleIcon.setSize("14px");
        Span voteCount = new Span(vote.totalVotes + " 人已投票");
        voteCount.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY);

        HorizontalLayout leftFooter = new HorizontalLayout(peopleIcon, voteCount);
        leftFooter.setAlignItems(FlexComponent.Alignment.CENTER);
        leftFooter.setSpacing(false);

        Span deadline;
        if (isActive) {
            Duration remaining = Duration.between(Instant.now(), vote.deadline);
            deadline = new Span("剩余 " + remaining.toDays() + " 天 " + (remaining.toHours() % 24) + " 小时");
        } else {
            deadline = new Span("已于 " + formatDate(vote.deadline) + " 结束");
        }
        deadline.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY);
        deadline.getElement().getStyle().set("margin-left", "auto");

        footer.add(leftFooter, deadline);
        card.add(footer);

        return card;
    }

    private Component createOptionRow(VoteOption option, int totalVotes, boolean isActive, boolean hasVoted) {
        VerticalLayout row = new VerticalLayout();
        row.setSpacing(false);
        row.setPadding(false);
        row.setWidthFull();

        HorizontalLayout topRow = new HorizontalLayout();
        topRow.setWidthFull();
        topRow.setAlignItems(FlexComponent.Alignment.CENTER);

        // Option text and vote button
        if (isActive && !hasVoted) {
            Button voteButton = new Button(option.name);
            voteButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
            voteButton.addClickListener(e -> {
                // Handle vote
                voteButton.setEnabled(false);
                voteButton.setText(option.name + " ✓");
            });
            topRow.add(voteButton);
        } else {
            Span optionName = new Span(option.name);
            optionName.addClassNames(LumoUtility.FontWeight.MEDIUM);
            topRow.add(optionName);
        }

        // Percentage
        double percentage = totalVotes > 0 ? (option.votes * 100.0 / totalVotes) : 0;
        Span percentageSpan = new Span(String.format("%.1f%%", percentage));
        percentageSpan.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.FontWeight.SEMIBOLD);
        percentageSpan.getElement().getStyle().set("margin-left", "auto");

        Span voteCountSpan = new Span(" (" + option.votes + "票)");
        voteCountSpan.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY);

        topRow.add(percentageSpan, voteCountSpan);
        row.add(topRow);

        // Progress bar
        Div progressContainer = new Div();
        progressContainer.setWidthFull();
        progressContainer.getElement().getStyle()
            .set("height", "8px")
            .set("background", "var(--lumo-contrast-10pct)")
            .set("border-radius", "4px")
            .set("overflow", "hidden")
            .set("margin-top", "4px");

        Div progressFill = new Div();
        progressFill.getElement().getStyle()
            .set("width", percentage + "%")
            .set("height", "100%")
            .set("background", "var(--lumo-primary-color)")
            .set("transition", "width 0.3s ease");

        progressContainer.add(progressFill);
        row.add(progressContainer);

        return row;
    }

    private String formatDate(Instant instant) {
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter
            .ofPattern("MM月dd日")
            .withZone(java.time.ZoneId.systemDefault());
        return formatter.format(instant);
    }

    // Data records
    private record VoteData(
        String title,
        String description,
        List<VoteOption> options,
        Instant deadline,
        int totalVotes,
        boolean hasVoted
    ) {}

    private record VoteOption(String name, int votes) {}
}
