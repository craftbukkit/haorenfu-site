/*
 * ═══════════════════════════════════════════════════════════════════════════
 *                         MESSAGES VIEW
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Private messaging interface with real-time updates.
 */
package world.haorenfu.ui.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.*;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;
import world.haorenfu.core.security.AuthenticatedUserProvider;
import world.haorenfu.domain.social.PrivateMessage;
import world.haorenfu.domain.social.SocialService;
import world.haorenfu.domain.user.User;
import world.haorenfu.domain.user.UserService;
import world.haorenfu.ui.layout.MainLayout;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Private messaging interface.
 */
@Route(value = "messages", layout = MainLayout.class)
@PageTitle("私信 | 好人服")
@PermitAll
public class MessagesView extends VerticalLayout implements HasUrlParameter<String> {

    private final SocialService socialService;
    private final UserService userService;
    private final AuthenticatedUserProvider authenticatedUser;

    private VerticalLayout conversationList;
    private VerticalLayout messageArea;
    private TextField messageInput;
    private User selectedUser;

    public MessagesView(SocialService socialService, UserService userService,
                        AuthenticatedUserProvider authenticatedUser) {
        this.socialService = socialService;
        this.userService = userService;
        this.authenticatedUser = authenticatedUser;

        addClassName("messages-view");
        setSizeFull();
        setPadding(false);
        setSpacing(false);

        add(createMainLayout());
    }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
        if (parameter != null && !parameter.isEmpty()) {
            try {
                UUID userId = UUID.fromString(parameter);
                userService.findById(userId).ifPresent(user -> {
                    this.selectedUser = user;
                    loadConversation(user);
                });
            } catch (IllegalArgumentException ignored) {}
        }
    }

    private Component createMainLayout() {
        HorizontalLayout main = new HorizontalLayout();
        main.setSizeFull();
        main.setSpacing(false);

        // Left panel - conversation list
        VerticalLayout leftPanel = new VerticalLayout();
        leftPanel.setWidth("300px");
        leftPanel.setHeightFull();
        leftPanel.setSpacing(false);
        leftPanel.setPadding(false);
        leftPanel.getElement().getStyle()
            .set("border-right", "1px solid var(--lumo-contrast-10pct)");

        // Header
        HorizontalLayout leftHeader = new HorizontalLayout();
        leftHeader.setWidthFull();
        leftHeader.setAlignItems(FlexComponent.Alignment.CENTER);
        leftHeader.setPadding(true);
        leftHeader.getElement().getStyle()
            .set("border-bottom", "1px solid var(--lumo-contrast-10pct)");

        H3 title = new H3("私信");
        title.addClassNames(LumoUtility.Margin.NONE);

        Button newMessageButton = new Button(VaadinIcon.PLUS.create());
        newMessageButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        leftHeader.add(title);
        leftHeader.addAndExpand(new Span());
        leftHeader.add(newMessageButton);

        // Search
        TextField searchField = new TextField();
        searchField.setPlaceholder("搜索对话...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setWidthFull();

        VerticalLayout searchContainer = new VerticalLayout(searchField);
        searchContainer.setPadding(true);
        searchContainer.setSpacing(false);

        // Conversation list
        conversationList = new VerticalLayout();
        conversationList.setSpacing(false);
        conversationList.setPadding(false);
        conversationList.setWidthFull();

        Scroller conversationScroller = new Scroller(conversationList);
        conversationScroller.setHeightFull();

        leftPanel.add(leftHeader, searchContainer, conversationScroller);

        // Right panel - message area
        VerticalLayout rightPanel = new VerticalLayout();
        rightPanel.setSizeFull();
        rightPanel.setSpacing(false);
        rightPanel.setPadding(false);

        // Chat header (will be populated when conversation selected)
        HorizontalLayout chatHeader = new HorizontalLayout();
        chatHeader.setId("chat-header");
        chatHeader.setWidthFull();
        chatHeader.setAlignItems(FlexComponent.Alignment.CENTER);
        chatHeader.setPadding(true);
        chatHeader.getElement().getStyle()
            .set("border-bottom", "1px solid var(--lumo-contrast-10pct)")
            .set("min-height", "60px");

        // Message area
        messageArea = new VerticalLayout();
        messageArea.setSizeFull();
        messageArea.setSpacing(true);
        messageArea.setPadding(true);

        Scroller messageScroller = new Scroller(messageArea);
        messageScroller.setHeightFull();
        messageScroller.setScrollDirection(Scroller.ScrollDirection.VERTICAL);

        // Input area
        HorizontalLayout inputArea = createInputArea();

        rightPanel.add(chatHeader, messageScroller, inputArea);

        main.add(leftPanel, rightPanel);
        main.setFlexGrow(1, rightPanel);

        // Load conversations
        loadConversations();

        return main;
    }

    private HorizontalLayout createInputArea() {
        HorizontalLayout inputArea = new HorizontalLayout();
        inputArea.setWidthFull();
        inputArea.setAlignItems(FlexComponent.Alignment.CENTER);
        inputArea.setPadding(true);
        inputArea.getElement().getStyle()
            .set("border-top", "1px solid var(--lumo-contrast-10pct)");

        messageInput = new TextField();
        messageInput.setPlaceholder("输入消息...");
        messageInput.setWidthFull();
        messageInput.addKeyPressListener(Key.ENTER, e -> sendMessage());

        Button sendButton = new Button(VaadinIcon.PAPERPLANE.create());
        sendButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        sendButton.addClickListener(e -> sendMessage());

        inputArea.add(messageInput, sendButton);
        inputArea.setFlexGrow(1, messageInput);

        return inputArea;
    }

    private void loadConversations() {
        conversationList.removeAll();

        Optional<User> currentUser = authenticatedUser.get();
        if (currentUser.isEmpty()) {
            return;
        }

        List<SocialService.ConversationSummary> conversations =
            socialService.getConversations(currentUser.get());

        if (conversations.isEmpty()) {
            Paragraph empty = new Paragraph("暂无对话");
            empty.addClassNames(LumoUtility.TextColor.SECONDARY);
            empty.getElement().getStyle().set("padding", "16px");
            conversationList.add(empty);
            return;
        }

        for (SocialService.ConversationSummary conv : conversations) {
            conversationList.add(createConversationItem(conv));
        }
    }

    private Component createConversationItem(SocialService.ConversationSummary conv) {
        HorizontalLayout item = new HorizontalLayout();
        item.setWidthFull();
        item.setAlignItems(FlexComponent.Alignment.CENTER);
        item.setPadding(true);
        item.getElement().getStyle()
            .set("cursor", "pointer")
            .set("border-bottom", "1px solid var(--lumo-contrast-5pct)");

        if (selectedUser != null && selectedUser.getId().equals(conv.otherUser().getId())) {
            item.getElement().getStyle().set("background", "var(--lumo-primary-color-10pct)");
        }

        // Avatar
        Div avatar = new Div();
        avatar.setText(conv.otherUser().getUsername().substring(0, 1).toUpperCase());
        avatar.getElement().getStyle()
            .set("width", "40px")
            .set("height", "40px")
            .set("border-radius", "50%")
            .set("background", "var(--lumo-primary-color)")
            .set("display", "flex")
            .set("align-items", "center")
            .set("justify-content", "center")
            .set("color", "white")
            .set("font-weight", "bold")
            .set("flex-shrink", "0");

        // Info
        VerticalLayout info = new VerticalLayout();
        info.setSpacing(false);
        info.setPadding(false);
        info.setWidthFull();

        HorizontalLayout topRow = new HorizontalLayout();
        topRow.setWidthFull();

        Span name = new Span(conv.otherUser().getUsername());
        name.addClassNames(LumoUtility.FontWeight.SEMIBOLD);

        Span time = new Span(formatTime(conv.lastMessageTime()));
        time.addClassNames(LumoUtility.FontSize.XSMALL, LumoUtility.TextColor.SECONDARY);
        time.getElement().getStyle().set("margin-left", "auto");

        topRow.add(name, time);

        Span preview = new Span(truncate(conv.lastMessage(), 30));
        preview.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY);

        info.add(topRow, preview);

        item.add(avatar, info);

        // Unread badge
        if (conv.unreadCount() > 0) {
            Span badge = new Span(String.valueOf(conv.unreadCount()));
            badge.getElement().getStyle()
                .set("background", "var(--lumo-primary-color)")
                .set("color", "white")
                .set("padding", "2px 8px")
                .set("border-radius", "10px")
                .set("font-size", "var(--lumo-font-size-xs)")
                .set("flex-shrink", "0");
            item.add(badge);
        }

        item.addClickListener(e -> {
            selectedUser = conv.otherUser();
            loadConversation(conv.otherUser());
            loadConversations(); // Refresh to update selection
        });

        return item;
    }

    private void loadConversation(User otherUser) {
        messageArea.removeAll();

        Optional<User> currentUser = authenticatedUser.get();
        if (currentUser.isEmpty()) return;

        // Update header
        updateChatHeader(otherUser);

        // Mark as read
        // In real implementation, get conversation ID and mark as read

        // Load messages
        List<PrivateMessage> messages = socialService.getConversation(
            currentUser.get(), otherUser,
            org.springframework.data.domain.PageRequest.of(0, 50)
        );

        // Reverse to show oldest first
        java.util.Collections.reverse(messages);

        for (PrivateMessage message : messages) {
            boolean isMine = message.getSender().getId().equals(currentUser.get().getId());
            messageArea.add(createMessageBubble(message, isMine));
        }

        // Scroll to bottom
        messageArea.getElement().executeJs(
            "this.scrollTop = this.scrollHeight"
        );
    }

    private void updateChatHeader(User user) {
        getElement().executeJs(
            "const header = document.getElementById('chat-header');" +
            "if (header) header.innerHTML = '';"
        );

        // This would be better implemented with component reference
        // For simplicity, showing the concept
    }

    private Component createMessageBubble(PrivateMessage message, boolean isMine) {
        VerticalLayout bubble = new VerticalLayout();
        bubble.setSpacing(false);
        bubble.setPadding(true);
        bubble.setWidth("auto");
        bubble.setMaxWidth("70%");
        bubble.getElement().getStyle()
            .set("background", isMine ? "var(--lumo-primary-color)" : "var(--lumo-contrast-10pct)")
            .set("color", isMine ? "white" : "var(--lumo-body-text-color)")
            .set("border-radius", "12px")
            .set("margin-left", isMine ? "auto" : "0")
            .set("margin-right", isMine ? "0" : "auto");

        Span content = new Span(message.getContent());

        Span time = new Span(formatMessageTime(message.getSentAt()));
        time.addClassNames(LumoUtility.FontSize.XXSMALL);
        time.getElement().getStyle()
            .set("opacity", "0.7")
            .set("margin-top", "4px");

        bubble.add(content, time);
        return bubble;
    }

    private void sendMessage() {
        if (selectedUser == null) return;

        String content = messageInput.getValue().trim();
        if (content.isEmpty()) return;

        Optional<User> currentUser = authenticatedUser.get();
        if (currentUser.isEmpty()) return;

        try {
            socialService.sendMessage(currentUser.get(), selectedUser, content);
            messageInput.clear();
            loadConversation(selectedUser);
            loadConversations();
        } catch (Exception e) {
            com.vaadin.flow.component.notification.Notification
                .show("发送失败: " + e.getMessage(), 3000,
                      com.vaadin.flow.component.notification.Notification.Position.BOTTOM_CENTER);
        }
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength) + "...";
    }

    private String formatTime(java.time.Instant instant) {
        java.time.Duration duration = java.time.Duration.between(instant, java.time.Instant.now());
        if (duration.toMinutes() < 60) return duration.toMinutes() + "分钟前";
        if (duration.toHours() < 24) return duration.toHours() + "小时前";
        if (duration.toDays() < 7) return duration.toDays() + "天前";

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd")
            .withZone(ZoneId.systemDefault());
        return formatter.format(instant);
    }

    private String formatMessageTime(java.time.Instant instant) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm")
            .withZone(ZoneId.systemDefault());
        return formatter.format(instant);
    }
}
