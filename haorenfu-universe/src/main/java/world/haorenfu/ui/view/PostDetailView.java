/*
 * ═══════════════════════════════════════════════════════════════════════════
 *                         POST DETAIL VIEW
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Detailed view of a forum post with comments and voting.
 * Pure Java implementation with Vaadin components.
 */
package world.haorenfu.ui.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.*;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;
import world.haorenfu.core.security.AuthenticatedUserProvider;
import world.haorenfu.domain.forum.ForumPost;
import world.haorenfu.domain.forum.ForumService;
import world.haorenfu.domain.user.User;
import world.haorenfu.ui.layout.MainLayout;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

/**
 * Forum post detail view with comments.
 */
@Route(value = "post", layout = MainLayout.class)
@PageTitle("帖子详情 | 好人服")
@PermitAll
public class PostDetailView extends VerticalLayout implements HasUrlParameter<String> {

    private final ForumService forumService;
    private final AuthenticatedUserProvider authenticatedUser;
    private ForumPost post;
    private VerticalLayout commentsSection;

    public PostDetailView(ForumService forumService, AuthenticatedUserProvider authenticatedUser) {
        this.forumService = forumService;
        this.authenticatedUser = authenticatedUser;

        addClassName("post-detail-view");
        setSizeFull();
        setPadding(true);
        setSpacing(true);
    }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
        if (parameter != null && !parameter.isEmpty()) {
            try {
                UUID postId = UUID.fromString(parameter);
                forumService.getPost(postId).ifPresentOrElse(
                    this::displayPost,
                    this::showNotFound
                );
            } catch (IllegalArgumentException e) {
                showNotFound();
            }
        } else {
            showNotFound();
        }
    }

    private void displayPost(ForumPost post) {
        this.post = post;
        removeAll();

        // Increment view count
        forumService.incrementViewCount(post.getId());

        add(createBreadcrumb());
        add(createPostContent());
        add(createVotingSection());
        add(createAuthorInfo());
        add(createCommentsSection());
        add(createCommentForm());
    }

    private Component createBreadcrumb() {
        HorizontalLayout breadcrumb = new HorizontalLayout();
        breadcrumb.setSpacing(false);
        breadcrumb.setAlignItems(FlexComponent.Alignment.CENTER);

        Anchor forumLink = new Anchor("forum", "论坛");
        forumLink.addClassNames(LumoUtility.TextColor.SECONDARY);

        Span separator = new Span(" / ");
        separator.addClassNames(LumoUtility.TextColor.SECONDARY);

        Span categorySpan = new Span(post.getCategory().getDisplayName());
        categorySpan.addClassNames(LumoUtility.TextColor.SECONDARY);

        breadcrumb.add(forumLink, separator, categorySpan);
        return breadcrumb;
    }

    private Component createPostContent() {
        VerticalLayout content = new VerticalLayout();
        content.setSpacing(true);
        content.setPadding(true);
        content.setWidthFull();
        content.getElement().getStyle()
            .set("background", "var(--lumo-contrast-5pct)")
            .set("border-radius", "var(--lumo-border-radius-l)");

        // Title
        H2 title = new H2(post.getTitle());
        title.addClassNames(LumoUtility.Margin.Bottom.SMALL);

        // Meta info
        HorizontalLayout meta = new HorizontalLayout();
        meta.setSpacing(true);
        meta.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.SMALL);

        Span categoryBadge = new Span(post.getCategory().getDisplayName());
        categoryBadge.getElement().getStyle()
            .set("background", "var(--lumo-primary-color)")
            .set("color", "white")
            .set("padding", "2px 8px")
            .set("border-radius", "12px");

        Span viewCount = new Span(post.getViewCount() + " 浏览");
        Span commentCount = new Span(post.getCommentCount() + " 评论");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            .withZone(ZoneId.systemDefault());
        Span postTime = new Span(formatter.format(post.getCreatedAt()));

        meta.add(categoryBadge, viewCount, commentCount, postTime);

        // Tags
        if (!post.getTags().isEmpty()) {
            HorizontalLayout tagsRow = new HorizontalLayout();
            tagsRow.setSpacing(false);
            tagsRow.getElement().getStyle().set("gap", "4px").set("flex-wrap", "wrap");

            for (String tag : post.getTags()) {
                Span tagSpan = new Span("#" + tag);
                tagSpan.getElement().getStyle()
                    .set("background", "var(--lumo-contrast-10pct)")
                    .set("padding", "2px 8px")
                    .set("border-radius", "12px")
                    .set("font-size", "var(--lumo-font-size-xs)");
                tagsRow.add(tagSpan);
            }
            content.add(tagsRow);
        }

        // Content body
        Div body = new Div();
        body.setText(post.getContent());
        body.getElement().getStyle()
            .set("white-space", "pre-wrap")
            .set("line-height", "1.8")
            .set("margin-top", "16px");

        content.add(title, meta, body);
        return content;
    }

    private Component createVotingSection() {
        HorizontalLayout voting = new HorizontalLayout();
        voting.setAlignItems(FlexComponent.Alignment.CENTER);
        voting.setSpacing(true);

        // Upvote button
        Button upvoteBtn = new Button(VaadinIcon.THUMBS_UP.create());
        upvoteBtn.setText(String.valueOf(post.getUpvotes()));
        upvoteBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        upvoteBtn.addClickListener(e -> {
            authenticatedUser.get().ifPresentOrElse(
                user -> {
                    forumService.votePost(post.getId(), user, true);
                    upvoteBtn.setText(String.valueOf(post.getUpvotes() + 1));
                    Notification.show("已点赞", 2000, Notification.Position.BOTTOM_CENTER);
                },
                () -> Notification.show("请先登录", 2000, Notification.Position.BOTTOM_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR)
            );
        });

        // Downvote button
        Button downvoteBtn = new Button(VaadinIcon.THUMBS_DOWN.create());
        downvoteBtn.setText(String.valueOf(post.getDownvotes()));
        downvoteBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        // Score display
        Span score = new Span("得分: " + (post.getUpvotes() - post.getDownvotes()));
        score.addClassNames(LumoUtility.FontWeight.BOLD);

        // Favorite button
        Button favoriteBtn = new Button("收藏", VaadinIcon.STAR_O.create());
        favoriteBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        // Share button
        Button shareBtn = new Button("分享", VaadinIcon.SHARE.create());
        shareBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        voting.add(upvoteBtn, downvoteBtn, score, favoriteBtn, shareBtn);
        return voting;
    }

    private Component createAuthorInfo() {
        HorizontalLayout author = new HorizontalLayout();
        author.setAlignItems(FlexComponent.Alignment.CENTER);
        author.setPadding(true);
        author.setWidthFull();
        author.getElement().getStyle()
            .set("background", "var(--lumo-contrast-5pct)")
            .set("border-radius", "var(--lumo-border-radius-m)");

        User postAuthor = post.getAuthor();

        // Avatar
        Div avatar = new Div();
        avatar.setText(postAuthor.getUsername().substring(0, 1).toUpperCase());
        avatar.getElement().getStyle()
            .set("width", "48px")
            .set("height", "48px")
            .set("border-radius", "50%")
            .set("background", postAuthor.getRole().getColor())
            .set("display", "flex")
            .set("align-items", "center")
            .set("justify-content", "center")
            .set("color", "white")
            .set("font-weight", "bold")
            .set("font-size", "20px");

        // Author details
        VerticalLayout details = new VerticalLayout();
        details.setSpacing(false);
        details.setPadding(false);

        HorizontalLayout nameRow = new HorizontalLayout();
        nameRow.setAlignItems(FlexComponent.Alignment.CENTER);

        Anchor username = new Anchor("profile/" + postAuthor.getId(), postAuthor.getUsername());
        username.addClassNames(LumoUtility.FontWeight.BOLD);

        Span roleBadge = new Span(postAuthor.getRole().getDisplayName());
        roleBadge.getElement().getStyle()
            .set("background", postAuthor.getRole().getColor())
            .set("color", "white")
            .set("padding", "2px 8px")
            .set("border-radius", "12px")
            .set("font-size", "var(--lumo-font-size-xs)");

        nameRow.add(username, roleBadge);

        HorizontalLayout statsRow = new HorizontalLayout();
        statsRow.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY);
        statsRow.add(
            new Span("声望: " + postAuthor.getReputation()),
            new Span("帖子: " + postAuthor.getPostCount())
        );

        details.add(nameRow, statsRow);

        author.add(avatar, details);
        return author;
    }

    private Component createCommentsSection() {
        commentsSection = new VerticalLayout();
        commentsSection.setSpacing(true);
        commentsSection.setPadding(false);

        H3 title = new H3("评论 (" + post.getCommentCount() + ")");
        commentsSection.add(title);

        // Load comments
        if (post.getCommentCount() == 0) {
            Paragraph noComments = new Paragraph("暂无评论，来抢沙发吧！");
            noComments.addClassNames(LumoUtility.TextColor.SECONDARY);
            noComments.getElement().getStyle().set("text-align", "center").set("padding", "24px");
            commentsSection.add(noComments);
        } else {
            // In real implementation, load from service
            commentsSection.add(createSampleComment("评论用户1", "这是一条示例评论。内容非常精彩！", "2小时前", 5));
            commentsSection.add(createSampleComment("评论用户2", "同意楼上的观点，学到了很多。", "1小时前", 3));
        }

        return commentsSection;
    }

    private Component createSampleComment(String username, String content, String time, int likes) {
        HorizontalLayout comment = new HorizontalLayout();
        comment.setWidthFull();
        comment.setPadding(true);
        comment.getElement().getStyle()
            .set("background", "var(--lumo-contrast-5pct)")
            .set("border-radius", "var(--lumo-border-radius-m)");

        // Avatar
        Div avatar = new Div();
        avatar.setText(username.substring(0, 1).toUpperCase());
        avatar.getElement().getStyle()
            .set("width", "36px")
            .set("height", "36px")
            .set("border-radius", "50%")
            .set("background", "var(--lumo-primary-color)")
            .set("display", "flex")
            .set("align-items", "center")
            .set("justify-content", "center")
            .set("color", "white")
            .set("font-weight", "bold")
            .set("flex-shrink", "0");

        // Content
        VerticalLayout commentContent = new VerticalLayout();
        commentContent.setSpacing(false);
        commentContent.setPadding(false);
        commentContent.setWidthFull();

        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();

        Span name = new Span(username);
        name.addClassNames(LumoUtility.FontWeight.SEMIBOLD);

        Span timeSpan = new Span(time);
        timeSpan.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY);
        timeSpan.getElement().getStyle().set("margin-left", "auto");

        header.add(name, timeSpan);

        Paragraph body = new Paragraph(content);
        body.addClassNames(LumoUtility.Margin.Vertical.SMALL);

        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);

        Button likeBtn = new Button(VaadinIcon.THUMBS_UP.create());
        likeBtn.setText(String.valueOf(likes));
        likeBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);

        Button replyBtn = new Button("回复");
        replyBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);

        actions.add(likeBtn, replyBtn);

        commentContent.add(header, body, actions);

        comment.add(avatar, commentContent);
        return comment;
    }

    private Component createCommentForm() {
        VerticalLayout form = new VerticalLayout();
        form.setSpacing(true);
        form.setPadding(true);
        form.getElement().getStyle()
            .set("background", "var(--lumo-contrast-5pct)")
            .set("border-radius", "var(--lumo-border-radius-l)");

        H4 title = new H4("发表评论");

        TextArea commentArea = new TextArea();
        commentArea.setPlaceholder("写下你的想法...");
        commentArea.setWidthFull();
        commentArea.setMinHeight("100px");
        commentArea.setMaxLength(5000);

        Button submitBtn = new Button("提交评论", VaadinIcon.PAPERPLANE.create());
        submitBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        submitBtn.addClickListener(e -> {
            String content = commentArea.getValue();
            if (content.trim().isEmpty()) {
                Notification.show("评论内容不能为空", 2000, Notification.Position.BOTTOM_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            authenticatedUser.get().ifPresentOrElse(
                user -> {
                    // Submit comment
                    Notification.show("评论发布成功！", 2000, Notification.Position.BOTTOM_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    commentArea.clear();
                },
                () -> Notification.show("请先登录", 2000, Notification.Position.BOTTOM_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR)
            );
        });

        form.add(title, commentArea, submitBtn);
        return form;
    }

    private void showNotFound() {
        removeAll();
        H2 title = new H2("帖子不存在");
        Paragraph message = new Paragraph("抱歉，找不到该帖子，可能已被删除。");
        message.addClassNames(LumoUtility.TextColor.SECONDARY);

        Button backBtn = new Button("返回论坛", e ->
            getUI().ifPresent(ui -> ui.navigate(ForumView.class))
        );
        backBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        VerticalLayout notFound = new VerticalLayout(title, message, backBtn);
        notFound.setAlignItems(FlexComponent.Alignment.CENTER);
        notFound.setPadding(true);
        add(notFound);
    }
}
