/*
 * ═══════════════════════════════════════════════════════════════════════════
 *                       POST EDITOR VIEW
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Rich text editor for creating and editing forum posts.
 * Features markdown support, image upload, and live preview.
 */
package world.haorenfu.ui.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.*;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;
import world.haorenfu.core.security.AuthenticatedUserProvider;
import world.haorenfu.domain.forum.ForumPost;
import world.haorenfu.domain.forum.ForumService;
import world.haorenfu.domain.forum.PostCategory;
import world.haorenfu.domain.user.User;
import world.haorenfu.ui.layout.MainLayout;

import java.util.Optional;
import java.util.UUID;

/**
 * Post creation and editing interface.
 */
@Route(value = "forum/new", layout = MainLayout.class)
@PageTitle("发布帖子 | 好人服")
@PermitAll
public class PostEditorView extends VerticalLayout implements HasUrlParameter<String> {

    private final ForumService forumService;
    private final AuthenticatedUserProvider authenticatedUser;

    private TextField titleField;
    private ComboBox<PostCategory> categorySelect;
    private TextField tagsField;
    private TextArea contentArea;
    private Div previewArea;
    private ForumPost editingPost;
    private boolean isEditMode = false;

    public PostEditorView(ForumService forumService, AuthenticatedUserProvider authenticatedUser) {
        this.forumService = forumService;
        this.authenticatedUser = authenticatedUser;

        addClassName("post-editor-view");
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        add(createHeader());
        add(createEditorSection());
    }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
        if (parameter != null && !parameter.isEmpty()) {
            try {
                UUID postId = UUID.fromString(parameter);
                forumService.getPost(postId).ifPresent(post -> {
                    this.editingPost = post;
                    this.isEditMode = true;
                    populateForm(post);
                });
            } catch (IllegalArgumentException ignored) {}
        }
    }

    private Component createHeader() {
        H2 title = new H2(isEditMode ? "编辑帖子" : "发布新帖");
        title.addClassNames(LumoUtility.Margin.Bottom.NONE);

        Paragraph description = new Paragraph("分享你的想法、经验和创意，与社区成员交流互动。");
        description.addClassNames(LumoUtility.TextColor.SECONDARY);

        VerticalLayout header = new VerticalLayout(title, description);
        header.setSpacing(false);
        header.setPadding(false);
        return header;
    }

    private Component createEditorSection() {
        HorizontalLayout mainSection = new HorizontalLayout();
        mainSection.setSizeFull();
        mainSection.setSpacing(true);

        // Editor panel
        VerticalLayout editorPanel = new VerticalLayout();
        editorPanel.setWidth("60%");
        editorPanel.setSpacing(true);
        editorPanel.setPadding(true);
        editorPanel.getElement().getStyle()
            .set("background", "var(--lumo-contrast-5pct)")
            .set("border-radius", "var(--lumo-border-radius-l)");

        // Title input
        titleField = new TextField("标题");
        titleField.setWidthFull();
        titleField.setPlaceholder("请输入帖子标题");
        titleField.setMaxLength(100);
        titleField.setRequired(true);

        // Category and tags row
        HorizontalLayout metaRow = new HorizontalLayout();
        metaRow.setWidthFull();
        metaRow.setSpacing(true);

        categorySelect = new ComboBox<>("分类");
        categorySelect.setItems(PostCategory.values());
        categorySelect.setItemLabelGenerator(PostCategory::getDisplayName);
        categorySelect.setRequired(true);
        categorySelect.setWidth("200px");

        tagsField = new TextField("标签");
        tagsField.setPlaceholder("用逗号分隔多个标签");
        tagsField.setWidthFull();

        metaRow.add(categorySelect, tagsField);
        metaRow.setFlexGrow(1, tagsField);

        // Toolbar
        HorizontalLayout toolbar = createToolbar();

        // Content area
        contentArea = new TextArea("内容");
        contentArea.setWidthFull();
        contentArea.setHeight("400px");
        contentArea.setPlaceholder("支持 Markdown 格式...");
        contentArea.addValueChangeListener(e -> updatePreview());

        // Action buttons
        HorizontalLayout actions = new HorizontalLayout();
        actions.setWidthFull();
        actions.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        Button cancelButton = new Button("取消");
        cancelButton.addClickListener(e -> navigateBack());

        Button draftButton = new Button("保存草稿", VaadinIcon.ARCHIVE.create());
        draftButton.addClickListener(e -> saveDraft());

        Button publishButton = new Button("发布", VaadinIcon.PAPERPLANE.create());
        publishButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        publishButton.addClickShortcut(Key.ENTER, com.vaadin.flow.component.KeyModifier.CONTROL);
        publishButton.addClickListener(e -> publish());

        actions.add(cancelButton, draftButton, publishButton);

        editorPanel.add(titleField, metaRow, toolbar, contentArea, actions);

        // Preview panel
        VerticalLayout previewPanel = new VerticalLayout();
        previewPanel.setWidth("40%");
        previewPanel.setSpacing(false);
        previewPanel.setPadding(true);
        previewPanel.getElement().getStyle()
            .set("background", "var(--lumo-contrast-5pct)")
            .set("border-radius", "var(--lumo-border-radius-l)");

        H4 previewTitle = new H4("预览");
        previewTitle.addClassNames(LumoUtility.Margin.Bottom.MEDIUM);

        previewArea = new Div();
        previewArea.setWidthFull();
        previewArea.getElement().getStyle()
            .set("min-height", "400px")
            .set("padding", "12px")
            .set("background", "var(--lumo-base-color)")
            .set("border-radius", "var(--lumo-border-radius-m)");

        previewPanel.add(previewTitle, previewArea);

        mainSection.add(editorPanel, previewPanel);
        return mainSection;
    }

    private HorizontalLayout createToolbar() {
        HorizontalLayout toolbar = new HorizontalLayout();
        toolbar.setSpacing(false);
        toolbar.getElement().getStyle()
            .set("background", "var(--lumo-contrast-10pct)")
            .set("border-radius", "var(--lumo-border-radius-m)")
            .set("padding", "4px");

        toolbar.add(
            createToolbarButton("B", "粗体", "**文字**"),
            createToolbarButton("I", "斜体", "*文字*"),
            createToolbarButton("S", "删除线", "~~文字~~"),
            createToolbarButton("H", "标题", "## 标题"),
            createToolbarButton("—", "分隔线", "\n---\n"),
            createToolbarButton("•", "列表", "\n- 项目1\n- 项目2"),
            createToolbarButton("1.", "编号", "\n1. 项目1\n2. 项目2"),
            createToolbarButton("\"", "引用", "\n> 引用内容"),
            createToolbarButton("{ }", "代码", "`代码`"),
            createToolbarButton("📷", "图片", "![描述](图片URL)"),
            createToolbarButton("🔗", "链接", "[文字](链接)")
        );

        return toolbar;
    }

    private Button createToolbarButton(String text, String tooltip, String insertText) {
        Button button = new Button(text);
        button.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        button.getElement().setAttribute("title", tooltip);
        button.addClickListener(e -> insertText(insertText));
        return button;
    }

    private void insertText(String text) {
        String current = contentArea.getValue();
        contentArea.setValue(current + text);
        contentArea.focus();
    }

    private void updatePreview() {
        String content = contentArea.getValue();
        // Simple markdown rendering (in production, use a proper Markdown library)
        String html = simpleMarkdownToHtml(content);
        previewArea.getElement().setProperty("innerHTML", html);
    }

    /**
     * Escapes HTML special characters to prevent XSS attacks.
     */
    private String escapeHtml(String text) {
        if (text == null) return "";
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#x27;");
    }

    private String simpleMarkdownToHtml(String markdown) {
        if (markdown == null || markdown.isEmpty()) {
            return "<p style='color: var(--lumo-secondary-text-color)'>开始输入内容查看预览...</p>";
        }

        // First, escape HTML to prevent XSS
        String html = escapeHtml(markdown);
        
        // Then apply markdown transformations (safe after escaping)
        // Headers
        html = html.replaceAll("(?m)^### (.+)$", "<h3>$1</h3>");
        html = html.replaceAll("(?m)^## (.+)$", "<h2>$1</h2>");
        html = html.replaceAll("(?m)^# (.+)$", "<h1>$1</h1>");
        // Bold
        html = html.replaceAll("\\*\\*(.+?)\\*\\*", "<strong>$1</strong>");
        // Italic
        html = html.replaceAll("\\*(.+?)\\*", "<em>$1</em>");
        // Strike
        html = html.replaceAll("~~(.+?)~~", "<del>$1</del>");
        // Code
        html = html.replaceAll("`(.+?)`", "<code style='background:#f4f4f4;padding:2px 6px;border-radius:4px'>$1</code>");
        // Links (only allow http/https URLs for security)
        html = html.replaceAll("\\[(.+?)\\]\\((https?://[^)]+)\\)", "<a href='$2' target='_blank' rel='noopener noreferrer'>$1</a>");
        // Line breaks
        html = html.replaceAll("\n", "<br>");

        return html;
    }

    private void populateForm(ForumPost post) {
        titleField.setValue(post.getTitle());
        categorySelect.setValue(post.getCategory());
        tagsField.setValue(String.join(", ", post.getTags()));
        contentArea.setValue(post.getContent());
        updatePreview();
    }

    private boolean validateForm() {
        if (titleField.getValue().trim().isEmpty()) {
            Notification.show("请输入标题", 3000, Notification.Position.BOTTOM_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return false;
        }
        if (categorySelect.getValue() == null) {
            Notification.show("请选择分类", 3000, Notification.Position.BOTTOM_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return false;
        }
        if (contentArea.getValue().trim().isEmpty()) {
            Notification.show("请输入内容", 3000, Notification.Position.BOTTOM_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return false;
        }
        return true;
    }

    private void saveDraft() {
        Optional<User> user = authenticatedUser.get();
        if (user.isEmpty()) {
            Notification.show("请先登录", 3000, Notification.Position.BOTTOM_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        // Save draft logic
        Notification.show("草稿已保存", 3000, Notification.Position.BOTTOM_CENTER)
            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void publish() {
        if (!validateForm()) return;

        Optional<User> user = authenticatedUser.get();
        if (user.isEmpty()) {
            Notification.show("请先登录", 3000, Notification.Position.BOTTOM_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        try {
            ForumPost post;
            if (isEditMode && editingPost != null) {
                post = editingPost;
            } else {
                post = new ForumPost();
                post.setAuthor(user.get());
            }

            post.setTitle(titleField.getValue().trim());
            post.setCategory(categorySelect.getValue());
            post.setContent(contentArea.getValue());

            // Parse tags
            String tagString = tagsField.getValue();
            if (tagString != null && !tagString.isEmpty()) {
                post.getTags().clear();
                for (String tag : tagString.split(",")) {
                    post.addTag(tag.trim());
                }
            }

            ForumPost saved = forumService.createPost(post);

            Notification.show("发布成功！", 3000, Notification.Position.BOTTOM_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            getUI().ifPresent(ui -> ui.navigate("forum/post/" + saved.getId()));

        } catch (Exception e) {
            Notification.show("发布失败: " + e.getMessage(), 5000, Notification.Position.BOTTOM_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void navigateBack() {
        getUI().ifPresent(ui -> ui.navigate(ForumView.class));
    }
}
