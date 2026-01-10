/*
 * ═══════════════════════════════════════════════════════════════════════════
 *                          PLAYERS VIEW
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Displays community members with search, filtering, and profile previews.
 * Pure Java implementation with Vaadin components.
 */
package world.haorenfu.ui.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;
import world.haorenfu.domain.user.User;
import world.haorenfu.domain.user.UserService;
import world.haorenfu.ui.layout.MainLayout;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * View displaying all community members.
 */
@Route(value = "players", layout = MainLayout.class)
@PageTitle("玩家列表 | 好人服")
@PermitAll
public class PlayersView extends VerticalLayout {

    private final UserService userService;
    private final Grid<User> grid;
    private final TextField searchField;

    public PlayersView(UserService userService) {
        this.userService = userService;

        addClassName("players-view");
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        add(createHeader());
        add(createSearchBar());

        grid = createGrid();
        add(grid);

        refreshGrid();
    }

    private Component createHeader() {
        H2 title = new H2("社区玩家");
        title.addClassNames(LumoUtility.Margin.Bottom.NONE);

        Paragraph description = new Paragraph("浏览我们社区的所有成员，了解他们的成就和贡献。");
        description.addClassNames(LumoUtility.TextColor.SECONDARY);

        // Statistics cards
        UserService.UserStatistics stats = userService.getStatistics();

        HorizontalLayout statsLayout = new HorizontalLayout();
        statsLayout.setSpacing(true);
        statsLayout.add(
            createStatCard("总玩家", String.valueOf(stats.totalUsers()), VaadinIcon.USERS),
            createStatCard("今日新增", String.valueOf(stats.newUsersToday()), VaadinIcon.PLUS),
            createStatCard("今日活跃", String.valueOf(stats.activeUsersToday()), VaadinIcon.FLASH),
            createStatCard("白名单", String.valueOf(stats.whitelistedUsers()), VaadinIcon.CHECK)
        );

        VerticalLayout header = new VerticalLayout(title, description, statsLayout);
        header.setSpacing(false);
        header.setPadding(false);
        return header;
    }

    private Component createStatCard(String label, String value, VaadinIcon icon) {
        Icon cardIcon = icon.create();
        cardIcon.setSize("24px");
        cardIcon.getElement().getStyle().set("color", "var(--lumo-primary-color)");

        Span valueSpan = new Span(value);
        valueSpan.addClassNames(LumoUtility.FontSize.XXLARGE, LumoUtility.FontWeight.BOLD);

        Span labelSpan = new Span(label);
        labelSpan.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY);

        VerticalLayout content = new VerticalLayout(cardIcon, valueSpan, labelSpan);
        content.setSpacing(false);
        content.setPadding(true);
        content.setAlignItems(FlexComponent.Alignment.CENTER);
        content.getElement().getStyle()
            .set("background", "var(--lumo-contrast-5pct)")
            .set("border-radius", "var(--lumo-border-radius-l)")
            .set("min-width", "120px");

        return content;
    }

    private Component createSearchBar() {
        searchField = new TextField();
        searchField.setPlaceholder("搜索玩家...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.addValueChangeListener(e -> refreshGrid());
        searchField.setWidthFull();
        searchField.setMaxWidth("400px");

        Button refreshButton = new Button("刷新", VaadinIcon.REFRESH.create());
        refreshButton.addClickListener(e -> refreshGrid());

        HorizontalLayout toolbar = new HorizontalLayout(searchField, refreshButton);
        toolbar.setWidthFull();
        toolbar.setAlignItems(FlexComponent.Alignment.CENTER);
        return toolbar;
    }

    private Grid<User> createGrid() {
        Grid<User> grid = new Grid<>(User.class, false);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        grid.setSizeFull();

        // Avatar and username column
        grid.addComponentColumn(user -> {
            HorizontalLayout layout = new HorizontalLayout();
            layout.setAlignItems(FlexComponent.Alignment.CENTER);

            Div avatar = new Div();
            avatar.getElement().getStyle()
                .set("width", "32px")
                .set("height", "32px")
                .set("border-radius", "50%")
                .set("background", "var(--lumo-primary-color)")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("color", "white")
                .set("font-weight", "bold");
            avatar.setText(user.getUsername().substring(0, 1).toUpperCase());

            Span name = new Span(user.getUsername());
            name.addClassNames(LumoUtility.FontWeight.SEMIBOLD);

            layout.add(avatar, name);
            return layout;
        }).setHeader("玩家").setAutoWidth(true).setFlexGrow(1);

        // Minecraft ID column
        grid.addColumn(user -> user.getMinecraftId() != null ? user.getMinecraftId() : "-")
            .setHeader("MC ID").setAutoWidth(true);

        // Role column
        grid.addComponentColumn(user -> {
            Span badge = new Span(user.getRole().getDisplayName());
            badge.getElement().getStyle()
                .set("background", user.getRole().getColor())
                .set("color", "white")
                .set("padding", "2px 8px")
                .set("border-radius", "12px")
                .set("font-size", "var(--lumo-font-size-xs)");
            return badge;
        }).setHeader("角色").setAutoWidth(true);

        // Reputation column
        grid.addComponentColumn(user -> {
            HorizontalLayout layout = new HorizontalLayout();
            layout.setAlignItems(FlexComponent.Alignment.CENTER);
            layout.setSpacing(false);

            Icon star = VaadinIcon.STAR.create();
            star.setSize("14px");
            star.getElement().getStyle().set("color", "#FFD700");

            Span rep = new Span(String.valueOf(user.getReputation()));
            layout.add(star, rep);
            return layout;
        }).setHeader("声望").setAutoWidth(true);

        // Rank column
        grid.addColumn(User::getReputationRank)
            .setHeader("等级").setAutoWidth(true);

        // Join date column
        grid.addColumn(user -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                .withZone(ZoneId.systemDefault());
            return formatter.format(user.getCreatedAt());
        }).setHeader("加入时间").setAutoWidth(true);

        // Last active column
        grid.addColumn(user -> {
            if (user.getLastActiveAt() == null) return "从未";
            Duration duration = Duration.between(user.getLastActiveAt(), Instant.now());
            if (duration.toMinutes() < 5) return "刚刚";
            if (duration.toHours() < 1) return duration.toMinutes() + " 分钟前";
            if (duration.toDays() < 1) return duration.toHours() + " 小时前";
            return duration.toDays() + " 天前";
        }).setHeader("最后活跃").setAutoWidth(true);

        // Actions column
        grid.addComponentColumn(user -> {
            Button viewButton = new Button(VaadinIcon.EYE.create());
            viewButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
            viewButton.addClickListener(e ->
                getUI().ifPresent(ui -> ui.navigate("profile/" + user.getId()))
            );
            return viewButton;
        }).setHeader("").setAutoWidth(true);

        return grid;
    }

    private void refreshGrid() {
        String filter = searchField.getValue();
        if (filter == null || filter.isEmpty()) {
            grid.setItems(userService.getTopUsersByReputation());
        } else {
            grid.setItems(
                userService.searchUsers(filter, org.springframework.data.domain.PageRequest.of(0, 50))
                    .getContent()
            );
        }
    }
}
