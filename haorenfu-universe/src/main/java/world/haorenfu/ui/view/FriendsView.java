/*
 * ═══════════════════════════════════════════════════════════════════════════
 *                          FRIENDS VIEW
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Social connections management interface.
 * Displays friends, pending requests, and recommendations.
 */
package world.haorenfu.ui.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;
import world.haorenfu.core.security.AuthenticatedUserProvider;
import world.haorenfu.domain.social.Friendship;
import world.haorenfu.domain.social.SocialService;
import world.haorenfu.domain.user.User;
import world.haorenfu.ui.layout.MainLayout;

import java.util.List;
import java.util.Optional;

/**
 * Friends management view.
 */
@Route(value = "friends", layout = MainLayout.class)
@PageTitle("好友 | 好人服")
@PermitAll
public class FriendsView extends VerticalLayout {

    private final SocialService socialService;
    private final AuthenticatedUserProvider authenticatedUser;
    private VerticalLayout contentArea;

    public FriendsView(SocialService socialService, AuthenticatedUserProvider authenticatedUser) {
        this.socialService = socialService;
        this.authenticatedUser = authenticatedUser;

        addClassName("friends-view");
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        add(createHeader());
        add(createTabs());

        contentArea = new VerticalLayout();
        contentArea.setSizeFull();
        contentArea.setPadding(false);
        add(contentArea);

        showFriendsList();
    }

    private Component createHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);

        H2 title = new H2("好友");
        title.addClassNames(LumoUtility.Margin.NONE);

        // Search field
        TextField searchField = new TextField();
        searchField.setPlaceholder("搜索好友或用户...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setWidth("300px");

        header.add(title);
        header.addAndExpand(new Span());
        header.add(searchField);

        return header;
    }

    private Component createTabs() {
        Tab friendsTab = new Tab(VaadinIcon.USERS.create(), new Span("我的好友"));
        Tab requestsTab = new Tab(VaadinIcon.ENVELOPE.create(), new Span("好友请求"));
        Tab suggestionsTab = new Tab(VaadinIcon.LIGHTBULB.create(), new Span("推荐好友"));

        Tabs tabs = new Tabs(friendsTab, requestsTab, suggestionsTab);
        tabs.addSelectedChangeListener(event -> {
            Tab selected = event.getSelectedTab();
            if (selected == friendsTab) showFriendsList();
            else if (selected == requestsTab) showRequests();
            else if (selected == suggestionsTab) showSuggestions();
        });

        return tabs;
    }

    private void showFriendsList() {
        contentArea.removeAll();

        Optional<User> currentUser = authenticatedUser.get();
        if (currentUser.isEmpty()) {
            showLoginPrompt();
            return;
        }

        List<User> friends = socialService.getFriends(currentUser.get());

        if (friends.isEmpty()) {
            showEmptyState("还没有好友", "去探索社区，添加志同道合的小伙伴吧！");
            return;
        }

        // Stats
        HorizontalLayout stats = new HorizontalLayout();
        stats.setSpacing(true);
        stats.add(createStatBadge(friends.size() + " 位好友", VaadinIcon.USERS));
        // Add online count in real implementation
        stats.add(createStatBadge("3 位在线", VaadinIcon.CIRCLE));
        contentArea.add(stats);

        // Friends grid
        FlexLayout grid = new FlexLayout();
        grid.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        grid.getElement().getStyle().set("gap", "16px");

        for (User friend : friends) {
            grid.add(createFriendCard(friend));
        }

        contentArea.add(grid);
    }

    private Component createFriendCard(User friend) {
        VerticalLayout card = new VerticalLayout();
        card.setSpacing(false);
        card.setPadding(true);
        card.setWidth("200px");
        card.setAlignItems(FlexComponent.Alignment.CENTER);
        card.getElement().getStyle()
            .set("background", "var(--lumo-contrast-5pct)")
            .set("border-radius", "var(--lumo-border-radius-l)")
            .set("cursor", "pointer")
            .set("transition", "transform 0.2s");

        // Avatar
        Div avatar = new Div();
        avatar.setText(friend.getUsername().substring(0, 1).toUpperCase());
        avatar.getElement().getStyle()
            .set("width", "64px")
            .set("height", "64px")
            .set("border-radius", "50%")
            .set("background", friend.getRole().getColor())
            .set("display", "flex")
            .set("align-items", "center")
            .set("justify-content", "center")
            .set("color", "white")
            .set("font-size", "var(--lumo-font-size-xxl)")
            .set("font-weight", "bold");

        // Online indicator
        Div onlineIndicator = new Div();
        onlineIndicator.getElement().getStyle()
            .set("width", "12px")
            .set("height", "12px")
            .set("border-radius", "50%")
            .set("background", "#4CAF50")
            .set("border", "2px solid var(--lumo-base-color)")
            .set("position", "absolute")
            .set("bottom", "0")
            .set("right", "0");

        Div avatarContainer = new Div(avatar, onlineIndicator);
        avatarContainer.getElement().getStyle().set("position", "relative");

        // Username
        Span name = new Span(friend.getUsername());
        name.addClassNames(LumoUtility.FontWeight.SEMIBOLD);

        // Role badge
        Span role = new Span(friend.getRole().getDisplayName());
        role.addClassNames(LumoUtility.FontSize.XSMALL);
        role.getElement().getStyle()
            .set("color", friend.getRole().getColor());

        // Action buttons
        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(false);

        Button messageButton = new Button(VaadinIcon.COMMENT.create());
        messageButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        messageButton.addClickListener(e ->
            getUI().ifPresent(ui -> ui.navigate("messages/" + friend.getId()))
        );

        Button profileButton = new Button(VaadinIcon.USER.create());
        profileButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        profileButton.addClickListener(e ->
            getUI().ifPresent(ui -> ui.navigate("profile/" + friend.getId()))
        );

        actions.add(messageButton, profileButton);

        card.add(avatarContainer, name, role, actions);

        card.addClickListener(e ->
            getUI().ifPresent(ui -> ui.navigate("profile/" + friend.getId()))
        );

        return card;
    }

    private void showRequests() {
        contentArea.removeAll();

        Optional<User> currentUser = authenticatedUser.get();
        if (currentUser.isEmpty()) {
            showLoginPrompt();
            return;
        }

        // Received requests
        H4 receivedTitle = new H4("收到的请求");
        contentArea.add(receivedTitle);

        List<Friendship> received = socialService.getPendingRequests(currentUser.get());

        if (received.isEmpty()) {
            Paragraph empty = new Paragraph("没有待处理的好友请求");
            empty.addClassNames(LumoUtility.TextColor.SECONDARY);
            contentArea.add(empty);
        } else {
            for (Friendship request : received) {
                contentArea.add(createRequestCard(request, true));
            }
        }

        // Sent requests
        H4 sentTitle = new H4("发出的请求");
        sentTitle.addClassNames(LumoUtility.Margin.Top.LARGE);
        contentArea.add(sentTitle);

        List<Friendship> sent = socialService.getSentRequests(currentUser.get());

        if (sent.isEmpty()) {
            Paragraph empty = new Paragraph("没有待确认的请求");
            empty.addClassNames(LumoUtility.TextColor.SECONDARY);
            contentArea.add(empty);
        } else {
            for (Friendship request : sent) {
                contentArea.add(createRequestCard(request, false));
            }
        }
    }

    private Component createRequestCard(Friendship request, boolean isReceived) {
        HorizontalLayout card = new HorizontalLayout();
        card.setWidthFull();
        card.setAlignItems(FlexComponent.Alignment.CENTER);
        card.setPadding(true);
        card.getElement().getStyle()
            .set("background", "var(--lumo-contrast-5pct)")
            .set("border-radius", "var(--lumo-border-radius-m)");

        User otherUser = isReceived ? request.getUser() : request.getFriend();

        // Avatar
        Div avatar = new Div();
        avatar.setText(otherUser.getUsername().substring(0, 1).toUpperCase());
        avatar.getElement().getStyle()
            .set("width", "48px")
            .set("height", "48px")
            .set("border-radius", "50%")
            .set("background", "var(--lumo-primary-color)")
            .set("display", "flex")
            .set("align-items", "center")
            .set("justify-content", "center")
            .set("color", "white")
            .set("font-weight", "bold");

        // Info
        VerticalLayout info = new VerticalLayout();
        info.setSpacing(false);
        info.setPadding(false);

        Span name = new Span(otherUser.getUsername());
        name.addClassNames(LumoUtility.FontWeight.SEMIBOLD);

        Span time = new Span(formatTime(request.getCreatedAt()));
        time.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY);

        info.add(name, time);

        card.add(avatar, info);
        card.addAndExpand(new Span());

        if (isReceived) {
            Button acceptButton = new Button("接受");
            acceptButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
            acceptButton.addClickListener(e -> {
                try {
                    socialService.acceptFriendRequest(request.getId(), authenticatedUser.get().orElseThrow());
                    Notification.show("已添加好友！", 3000, Notification.Position.BOTTOM_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    showRequests();
                } catch (Exception ex) {
                    Notification.show("操作失败", 3000, Notification.Position.BOTTOM_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
            });

            Button rejectButton = new Button("拒绝");
            rejectButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
            rejectButton.addClickListener(e -> {
                socialService.rejectFriendRequest(request.getId(), authenticatedUser.get().orElseThrow());
                showRequests();
            });

            card.add(acceptButton, rejectButton);
        } else {
            Span pending = new Span("等待确认");
            pending.addClassNames(LumoUtility.TextColor.SECONDARY);
            card.add(pending);
        }

        return card;
    }

    private void showSuggestions() {
        contentArea.removeAll();

        Optional<User> currentUser = authenticatedUser.get();
        if (currentUser.isEmpty()) {
            showLoginPrompt();
            return;
        }

        H4 title = new H4("推荐好友");
        Paragraph desc = new Paragraph("基于你的社交网络和共同好友推荐");
        desc.addClassNames(LumoUtility.TextColor.SECONDARY);
        contentArea.add(title, desc);

        List<User> suggestions = socialService.recommendFriends(currentUser.get(), 10);

        if (suggestions.isEmpty()) {
            Paragraph empty = new Paragraph("暂无推荐，去论坛认识更多朋友吧！");
            empty.addClassNames(LumoUtility.TextColor.SECONDARY);
            contentArea.add(empty);
            return;
        }

        FlexLayout grid = new FlexLayout();
        grid.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        grid.getElement().getStyle().set("gap", "16px");

        for (User suggestion : suggestions) {
            grid.add(createSuggestionCard(suggestion, currentUser.get()));
        }

        contentArea.add(grid);
    }

    private Component createSuggestionCard(User suggestion, User currentUser) {
        VerticalLayout card = new VerticalLayout();
        card.setSpacing(false);
        card.setPadding(true);
        card.setWidth("200px");
        card.setAlignItems(FlexComponent.Alignment.CENTER);
        card.getElement().getStyle()
            .set("background", "var(--lumo-contrast-5pct)")
            .set("border-radius", "var(--lumo-border-radius-l)");

        // Avatar
        Div avatar = new Div();
        avatar.setText(suggestion.getUsername().substring(0, 1).toUpperCase());
        avatar.getElement().getStyle()
            .set("width", "64px")
            .set("height", "64px")
            .set("border-radius", "50%")
            .set("background", suggestion.getRole().getColor())
            .set("display", "flex")
            .set("align-items", "center")
            .set("justify-content", "center")
            .set("color", "white")
            .set("font-size", "var(--lumo-font-size-xxl)")
            .set("font-weight", "bold");

        Span name = new Span(suggestion.getUsername());
        name.addClassNames(LumoUtility.FontWeight.SEMIBOLD);

        // Mutual friends count
        List<User> mutual = socialService.getMutualFriends(currentUser, suggestion);
        Span mutualSpan = new Span(mutual.size() + " 位共同好友");
        mutualSpan.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY);

        Button addButton = new Button("添加好友", VaadinIcon.PLUS.create());
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        addButton.addClickListener(e -> {
            try {
                socialService.sendFriendRequest(currentUser, suggestion);
                addButton.setText("已发送");
                addButton.setEnabled(false);
                Notification.show("好友请求已发送", 3000, Notification.Position.BOTTOM_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } catch (Exception ex) {
                Notification.show(ex.getMessage(), 3000, Notification.Position.BOTTOM_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        card.add(avatar, name, mutualSpan, addButton);
        return card;
    }

    private Component createStatBadge(String text, VaadinIcon icon) {
        HorizontalLayout badge = new HorizontalLayout();
        badge.setAlignItems(FlexComponent.Alignment.CENTER);
        badge.setSpacing(false);
        badge.getElement().getStyle()
            .set("background", "var(--lumo-contrast-5pct)")
            .set("padding", "4px 12px")
            .set("border-radius", "16px");

        badge.add(icon.create(), new Span(" " + text));
        return badge;
    }

    private void showEmptyState(String title, String message) {
        VerticalLayout empty = new VerticalLayout();
        empty.setAlignItems(FlexComponent.Alignment.CENTER);
        empty.setPadding(true);

        Span emoji = new Span("👥");
        emoji.getElement().getStyle().set("font-size", "64px");

        H3 emptyTitle = new H3(title);
        Paragraph emptyMessage = new Paragraph(message);
        emptyMessage.addClassNames(LumoUtility.TextColor.SECONDARY);

        empty.add(emoji, emptyTitle, emptyMessage);
        contentArea.add(empty);
    }

    private void showLoginPrompt() {
        VerticalLayout prompt = new VerticalLayout();
        prompt.setAlignItems(FlexComponent.Alignment.CENTER);
        prompt.setPadding(true);

        H3 title = new H3("请先登录");
        Button loginButton = new Button("登录", e ->
            getUI().ifPresent(ui -> ui.navigate(LoginView.class))
        );
        loginButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        prompt.add(title, loginButton);
        contentArea.add(prompt);
    }

    private String formatTime(java.time.Instant instant) {
        java.time.Duration duration = java.time.Duration.between(instant, java.time.Instant.now());
        if (duration.toMinutes() < 60) return duration.toMinutes() + " 分钟前";
        if (duration.toHours() < 24) return duration.toHours() + " 小时前";
        return duration.toDays() + " 天前";
    }
}
