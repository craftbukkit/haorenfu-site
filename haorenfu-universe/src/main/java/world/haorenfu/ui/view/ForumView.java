/*
 * ═══════════════════════════════════════════════════════════════════════════
 *                           FORUM VIEW
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Community discussion forum with categorized posts,
 * voting system, and real-time updates.
 */
package world.haorenfu.ui.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import world.haorenfu.core.security.AuthenticationService;
import world.haorenfu.domain.forum.ForumPost;
import world.haorenfu.domain.forum.ForumService;
import world.haorenfu.domain.forum.PostCategory;
import world.haorenfu.ui.layout.MainLayout;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Main forum view with post listing and categorization.
 */
@Route(value = "forum", layout = MainLayout.class)
@PageTitle("论坛 | 好人服")
@AnonymousAllowed
public class ForumView extends VerticalLayout implements HasUrlParameter<String> {

    private final ForumService forumService;
    private final AuthenticationService authService;

    private final VerticalLayout postListContainer = new VerticalLayout();
    private PostCategory currentCategory = null;
    private int currentPage = 0;
    private static final int PAGE_SIZE = 20;

    private final Tabs categoryTabs = new Tabs();
    private final TextField searchField = new TextField();

    public ForumView(ForumService forumService, AuthenticationService authService) {
        this.forumService = forumService;
        this.authService = authService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        add(
            createHeader(),
            createCategoryTabs(),
            createToolbar(),
            postListContainer,
            createPagination()
        );

        loadPosts();
    }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
        if (parameter != null && !parameter.isEmpty()) {
            try {
                currentCategory = PostCategory.valueOf(parameter.toUpperCase());
                updateCategoryTab();
            } catch (IllegalArgumentException e) {
                currentCategory = null;
            }
        }
        loadPosts();
    }

    private Component createHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        // Title
        HorizontalLayout titleSection = new HorizontalLayout();
        titleSection.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);

        Icon forumIcon = VaadinIcon.COMMENTS.create();
        forumIcon.setSize("32px");
        forumIcon.setColor("#4CAF50");

        H2 title = new H2("社区论坛");
        title.getStyle().set("margin", "0");

        titleSection.add(forumIcon, title);

        // New post button
        Button newPostBtn = new Button("发布帖子", VaadinIcon.PLUS.create());
        newPostBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        newPostBtn.getStyle().set("background-color", "#4CAF50");
        newPostBtn.addClickListener(e -> openNewPostDialog());

        header.add(titleSection, newPostBtn);

        return header;
    }

    private Component createCategoryTabs() {
        categoryTabs.setWidthFull();

        // All categories tab
        Tab allTab = new Tab("全部");
        allTab.setId("all");
        categoryTabs.add(allTab);

        // Category tabs
        for (PostCategory cat : PostCategory.values()) {
            if (!cat.isAdminOnly() || (authService.isAuthenticated() &&
                authService.getAuthenticatedUser().map(u -> u.getRole().getLevel() >= 3).orElse(false))) {

                Tab tab = new Tab(cat.getIcon() + " " + cat.getDisplayName());
                tab.setId(cat.name());
                categoryTabs.add(tab);
            }
        }

        categoryTabs.addSelectedChangeListener(e -> {
            Tab selected = e.getSelectedTab();
            String tabId = selected.getId().orElse("all");

            if ("all".equals(tabId)) {
                currentCategory = null;
            } else {
                try {
                    currentCategory = PostCategory.valueOf(tabId);
                } catch (IllegalArgumentException ex) {
                    currentCategory = null;
                }
            }

            currentPage = 0;
            loadPosts();
        });

        return categoryTabs;
    }

    private void updateCategoryTab() {
        categoryTabs.getChildren()
            .filter(c -> c instanceof Tab)
            .map(c -> (Tab) c)
            .forEach(tab -> {
                String tabId = tab.getId().orElse("");
                if (currentCategory == null && "all".equals(tabId)) {
                    categoryTabs.setSelectedTab(tab);
                } else if (currentCategory != null && currentCategory.name().equals(tabId)) {
                    categoryTabs.setSelectedTab(tab);
                }
            });
    }

    private Component createToolbar() {
        HorizontalLayout toolbar = new HorizontalLayout();
        toolbar.setWidthFull();
        toolbar.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);

        // Search
        searchField.setPlaceholder("搜索帖子...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setWidth("300px");
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.addValueChangeListener(e -> {
            currentPage = 0;
            loadPosts();
        });

        // Stats
        ForumService.ForumStatistics stats = forumService.getStatistics();
        Span statsSpan = new Span(
            String.format("共 %d 帖子 · 今日 %d 新帖",
                stats.totalPosts(), stats.postsToday())
        );
        statsSpan.getStyle()
            .set("color", "#888")
            .set("font-size", "14px")
            .set("margin-left", "auto");

        toolbar.add(searchField, statsSpan);

        return toolbar;
    }

    private void loadPosts() {
        postListContainer.removeAll();

        Page<ForumPost> posts;
        PageRequest pageRequest = PageRequest.of(currentPage, PAGE_SIZE);

        String searchQuery = searchField.getValue();

        if (searchQuery != null && !searchQuery.isBlank()) {
            posts = forumService.searchPosts(searchQuery, pageRequest);
        } else if (currentCategory != null) {
            posts = forumService.getPostsByCategory(currentCategory, pageRequest);
        } else {
            posts = forumService.getHotPosts(pageRequest);
        }

        if (posts.isEmpty()) {
            Paragraph empty = new Paragraph("暂无帖子");
            empty.getStyle()
                .set("color", "#888")
                .set("text-align", "center")
                .set("padding", "40px");
            postListContainer.add(empty);
        } else {
            posts.forEach(post -> postListContainer.add(createPostCard(post)));
        }
    }

    private Component createPostCard(ForumPost post) {
        HorizontalLayout card = new HorizontalLayout();
        card.setWidthFull();
        card.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.START);
        card.getStyle()
            .set("background-color", "var(--lumo-contrast-5pct)")
            .set("padding", "16px")
            .set("border-radius", "8px")
            .set("cursor", "pointer")
            .set("transition", "background-color 0.2s");

        card.getElement().addEventListener("mouseenter", e ->
            card.getStyle().set("background-color", "var(--lumo-contrast-10pct)")
        );
        card.getElement().addEventListener("mouseleave", e ->
            card.getStyle().set("background-color", "var(--lumo-contrast-5pct)")
        );

        // Vote section
        VerticalLayout voteSection = createVoteSection(post);

        // Main content
        VerticalLayout content = new VerticalLayout();
        content.setSpacing(false);
        content.setPadding(false);

        // Title with pinned/locked badges
        HorizontalLayout titleLine = new HorizontalLayout();
        titleLine.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);

        if (post.isPinned()) {
            Span pinnedBadge = new Span("📌");
            pinnedBadge.getStyle().set("margin-right", "8px");
            titleLine.add(pinnedBadge);
        }

        if (post.isLocked()) {
            Span lockedBadge = new Span("🔒");
            lockedBadge.getStyle().set("margin-right", "8px");
            titleLine.add(lockedBadge);
        }

        Span categoryBadge = new Span(post.getCategory().getIcon());
        categoryBadge.getStyle()
            .set("margin-right", "8px")
            .set("font-size", "16px");

        Anchor titleLink = new Anchor("forum/post/" + post.getId(), post.getTitle());
        titleLink.getStyle()
            .set("color", "var(--lumo-header-text-color)")
            .set("font-weight", "500")
            .set("font-size", "16px")
            .set("text-decoration", "none");

        titleLine.add(categoryBadge, titleLink);

        // Meta info
        HorizontalLayout meta = new HorizontalLayout();
        meta.setSpacing(true);
        meta.getStyle().set("margin-top", "8px");

        Span author = new Span(post.getAuthor().getUsername());
        author.getStyle()
            .set("color", "#4CAF50")
            .set("font-size", "13px");

        Span timeAgo = new Span(formatTimeAgo(post.getCreatedAt()));
        timeAgo.getStyle()
            .set("color", "#888")
            .set("font-size", "13px");

        Span comments = new Span("💬 " + post.getCommentCount());
        comments.getStyle()
            .set("color", "#888")
            .set("font-size", "13px");

        Span views = new Span("👁 " + post.getViews());
        views.getStyle()
            .set("color", "#888")
            .set("font-size", "13px");

        meta.add(author, new Span("·"), timeAgo, new Span("·"), comments, views);

        // Tags
        if (!post.getTags().isEmpty()) {
            HorizontalLayout tags = new HorizontalLayout();
            tags.setSpacing(true);
            tags.getStyle().set("margin-top", "8px");

            post.getTags().stream().limit(3).forEach(tag -> {
                Span tagSpan = new Span("#" + tag);
                tagSpan.getStyle()
                    .set("background-color", "var(--lumo-contrast-10pct)")
                    .set("padding", "2px 8px")
                    .set("border-radius", "4px")
                    .set("font-size", "12px")
                    .set("color", "#4CAF50");
                tags.add(tagSpan);
            });

            content.add(titleLine, meta, tags);
        } else {
            content.add(titleLine, meta);
        }

        card.add(voteSection, content);
        card.setFlexGrow(1, content);

        card.addClickListener(e ->
            card.getUI().ifPresent(ui -> ui.navigate("forum/post/" + post.getId()))
        );

        return card;
    }

    private VerticalLayout createVoteSection(ForumPost post) {
        VerticalLayout voteSection = new VerticalLayout();
        voteSection.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.CENTER);
        voteSection.setSpacing(false);
        voteSection.setPadding(false);
        voteSection.setWidth("60px");

        Button upvoteBtn = new Button(VaadinIcon.ARROW_UP.create());
        upvoteBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);

        Span voteCount = new Span(String.valueOf(post.getVoteDifferential()));
        voteCount.getStyle()
            .set("font-weight", "bold")
            .set("font-size", "16px")
            .set("color", post.getVoteDifferential() > 0 ? "#4CAF50" :
                         post.getVoteDifferential() < 0 ? "#f44336" : "#888");

        Button downvoteBtn = new Button(VaadinIcon.ARROW_DOWN.create());
        downvoteBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);

        // Vote handlers
        upvoteBtn.addClickListener(e -> {
            e.getSource().getUI().ifPresent(ui -> {
                if (!authService.isAuthenticated()) {
                    ui.navigate(LoginView.class);
                    return;
                }
                handleVote(post.getId(), 1, voteCount);
            });
        });

        downvoteBtn.addClickListener(e -> {
            e.getSource().getUI().ifPresent(ui -> {
                if (!authService.isAuthenticated()) {
                    ui.navigate(LoginView.class);
                    return;
                }
                handleVote(post.getId(), -1, voteCount);
            });
        });

        voteSection.add(upvoteBtn, voteCount, downvoteBtn);

        return voteSection;
    }

    private void handleVote(UUID postId, int value, Span voteCountSpan) {
        try {
            UUID userId = authService.getCurrentUserId().orElseThrow();
            ForumService.VoteResult result = forumService.votePost(postId, userId, value);

            int diff = result.upvotes() - result.downvotes();
            voteCountSpan.setText(String.valueOf(diff));
            voteCountSpan.getStyle().set("color",
                diff > 0 ? "#4CAF50" : diff < 0 ? "#f44336" : "#888");

        } catch (Exception e) {
            Notification.show("投票失败", 2000, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void openNewPostDialog() {
        if (!authService.isAuthenticated()) {
            getUI().ifPresent(ui -> ui.navigate(LoginView.class));
            return;
        }

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("发布新帖子");
        dialog.setWidth("600px");

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);

        TextField titleField = new TextField("标题");
        titleField.setWidthFull();
        titleField.setPlaceholder("输入帖子标题");
        titleField.setRequired(true);
        titleField.setMaxLength(100);

        ComboBox<PostCategory> categoryBox = new ComboBox<>("分类");
        categoryBox.setWidthFull();
        categoryBox.setItems(Arrays.stream(PostCategory.values())
            .filter(c -> !c.isAdminOnly())
            .toList());
        categoryBox.setItemLabelGenerator(PostCategory::getFullName);
        categoryBox.setValue(PostCategory.GENERAL);
        categoryBox.setRequired(true);

        TextArea contentArea = new TextArea("内容");
        contentArea.setWidthFull();
        contentArea.setMinHeight("200px");
        contentArea.setPlaceholder("输入帖子内容...");
        contentArea.setRequired(true);

        TextField tagsField = new TextField("标签");
        tagsField.setWidthFull();
        tagsField.setPlaceholder("用逗号分隔多个标签");
        tagsField.setHelperText("例如: 建筑,红石,教程");

        content.add(titleField, categoryBox, contentArea, tagsField);

        Button cancelBtn = new Button("取消", e -> dialog.close());

        Button submitBtn = new Button("发布", e -> {
            if (titleField.isEmpty() || contentArea.isEmpty()) {
                Notification.show("请填写标题和内容", 2000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            try {
                Set<String> tags = new HashSet<>();
                if (!tagsField.isEmpty()) {
                    Arrays.stream(tagsField.getValue().split(","))
                        .map(String::trim)
                        .filter(t -> !t.isEmpty())
                        .forEach(tags::add);
                }

                ForumService.CreatePostRequest request = new ForumService.CreatePostRequest(
                    titleField.getValue(),
                    contentArea.getValue(),
                    categoryBox.getValue(),
                    tags
                );

                UUID userId = authService.getCurrentUserId().orElseThrow();
                ForumPost post = forumService.createPost(request, userId);

                dialog.close();
                loadPosts();

                Notification.show("帖子发布成功！", 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            } catch (Exception ex) {
                Notification.show("发布失败: " + ex.getMessage(), 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        submitBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        submitBtn.getStyle().set("background-color", "#4CAF50");

        dialog.add(content);
        dialog.getFooter().add(cancelBtn, submitBtn);

        dialog.open();
    }

    private Component createPagination() {
        HorizontalLayout pagination = new HorizontalLayout();
        pagination.setWidthFull();
        pagination.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        pagination.setSpacing(true);

        Button prevBtn = new Button(VaadinIcon.ANGLE_LEFT.create());
        prevBtn.setEnabled(currentPage > 0);
        prevBtn.addClickListener(e -> {
            if (currentPage > 0) {
                currentPage--;
                loadPosts();
            }
        });

        Span pageInfo = new Span("第 " + (currentPage + 1) + " 页");
        pageInfo.getStyle().set("color", "#888");

        Button nextBtn = new Button(VaadinIcon.ANGLE_RIGHT.create());
        nextBtn.addClickListener(e -> {
            currentPage++;
            loadPosts();
        });

        pagination.add(prevBtn, pageInfo, nextBtn);

        return pagination;
    }

    private String formatTimeAgo(Instant instant) {
        Duration duration = Duration.between(instant, Instant.now());

        if (duration.toMinutes() < 1) return "刚刚";
        if (duration.toMinutes() < 60) return duration.toMinutes() + " 分钟前";
        if (duration.toHours() < 24) return duration.toHours() + " 小时前";
        if (duration.toDays() < 7) return duration.toDays() + " 天前";
        if (duration.toDays() < 30) return (duration.toDays() / 7) + " 周前";
        if (duration.toDays() < 365) return (duration.toDays() / 30) + " 个月前";
        return (duration.toDays() / 365) + " 年前";
    }
}
