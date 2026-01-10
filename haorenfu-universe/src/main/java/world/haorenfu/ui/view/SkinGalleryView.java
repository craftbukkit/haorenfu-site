/*
 * ═══════════════════════════════════════════════════════════════════════════
 *                          SKIN GALLERY VIEW
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Community skin gallery for browsing, uploading, and sharing skins.
 */
package world.haorenfu.ui.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;
import world.haorenfu.ui.layout.MainLayout;

import java.util.List;

/**
 * Skin gallery view for browsing and uploading skins.
 */
@Route(value = "skins", layout = MainLayout.class)
@PageTitle("皮肤画廊 | 好人服")
@PermitAll
public class SkinGalleryView extends VerticalLayout {

    private FlexLayout skinGrid;

    public SkinGalleryView() {
        addClassName("skin-gallery-view");
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        add(createHeader());
        add(createFilters());

        skinGrid = new FlexLayout();
        skinGrid.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        skinGrid.getElement().getStyle().set("gap", "20px");
        skinGrid.setWidthFull();

        add(skinGrid);

        loadSkins();
    }

    private Component createHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);

        VerticalLayout titleSection = new VerticalLayout();
        titleSection.setSpacing(false);
        titleSection.setPadding(false);

        H2 title = new H2("皮肤画廊");
        title.addClassNames(LumoUtility.Margin.NONE);

        Paragraph description = new Paragraph("浏览社区分享的皮肤，或上传你自己的创作！");
        description.addClassNames(LumoUtility.TextColor.SECONDARY);

        titleSection.add(title, description);

        // Upload button
        Button uploadButton = new Button("上传皮肤", VaadinIcon.UPLOAD.create());
        uploadButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        uploadButton.addClickListener(e -> openUploadDialog());

        header.add(titleSection);
        header.addAndExpand(new Span());
        header.add(uploadButton);

        return header;
    }

    private Component createFilters() {
        HorizontalLayout filters = new HorizontalLayout();
        filters.setWidthFull();
        filters.setAlignItems(FlexComponent.Alignment.CENTER);
        filters.setSpacing(true);

        // Search
        TextField searchField = new TextField();
        searchField.setPlaceholder("搜索皮肤...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setWidth("300px");

        // Model filter
        Select<String> modelSelect = new Select<>();
        modelSelect.setLabel("模型");
        modelSelect.setItems("全部", "Steve (经典)", "Alex (纤细)");
        modelSelect.setValue("全部");

        // Sort by
        Select<String> sortSelect = new Select<>();
        sortSelect.setLabel("排序");
        sortSelect.setItems("最新上传", "最多喜欢", "最多下载", "最多浏览");
        sortSelect.setValue("最新上传");

        // Tag filter
        Select<String> tagSelect = new Select<>();
        tagSelect.setLabel("标签");
        tagSelect.setItems("全部", "原创", "角色扮演", "动漫", "游戏", "简约", "像素艺术");
        tagSelect.setValue("全部");

        filters.add(searchField, modelSelect, sortSelect, tagSelect);

        return filters;
    }

    private void loadSkins() {
        skinGrid.removeAll();

        // Sample skins
        List<SkinData> skins = List.of(
            new SkinData("酷炫骑士", "Player_123", "classic", 256, 128, 1520, true),
            new SkinData("末影人", "Enderman_Fan", "classic", 189, 95, 980, false),
            new SkinData("可爱女孩", "CuteGirl", "slim", 312, 156, 2100, true),
            new SkinData("机械战士", "TechWarrior", "classic", 445, 234, 3200, true),
            new SkinData("像素艺术家", "PixelMaster", "classic", 78, 42, 450, false),
            new SkinData("忍者", "ShadowNinja", "slim", 223, 112, 1680, true),
            new SkinData("海盗船长", "PirateKing", "classic", 156, 89, 890, false),
            new SkinData("魔法师", "Wizard_001", "slim", 198, 104, 1240, true)
        );

        for (SkinData skin : skins) {
            skinGrid.add(createSkinCard(skin));
        }
    }

    private Component createSkinCard(SkinData skin) {
        VerticalLayout card = new VerticalLayout();
        card.setSpacing(false);
        card.setPadding(false);
        card.setWidth("200px");
        card.getElement().getStyle()
            .set("background", "var(--lumo-contrast-5pct)")
            .set("border-radius", "var(--lumo-border-radius-l)")
            .set("overflow", "hidden")
            .set("cursor", "pointer")
            .set("transition", "transform 0.2s, box-shadow 0.2s");

        // Skin preview (3D render would go here)
        Div preview = new Div();
        preview.setWidthFull();
        preview.setHeight("200px");
        preview.getElement().getStyle()
            .set("background", "linear-gradient(135deg, #667eea 0%, #764ba2 100%)")
            .set("display", "flex")
            .set("align-items", "center")
            .set("justify-content", "center");

        // Placeholder skin icon
        Span skinIcon = new Span("🧑");
        skinIcon.getElement().getStyle()
            .set("font-size", "64px")
            .set("filter", "drop-shadow(0 4px 8px rgba(0,0,0,0.3))");

        preview.add(skinIcon);

        // Model badge
        Span modelBadge = new Span(skin.model.equals("classic") ? "Steve" : "Alex");
        modelBadge.getElement().getStyle()
            .set("position", "absolute")
            .set("top", "8px")
            .set("right", "8px")
            .set("background", "rgba(0,0,0,0.5)")
            .set("color", "white")
            .set("padding", "2px 8px")
            .set("border-radius", "10px")
            .set("font-size", "var(--lumo-font-size-xs)");

        Div previewContainer = new Div(preview, modelBadge);
        previewContainer.getElement().getStyle().set("position", "relative");

        // Info section
        VerticalLayout info = new VerticalLayout();
        info.setSpacing(false);
        info.setPadding(true);

        Span name = new Span(skin.name);
        name.addClassNames(LumoUtility.FontWeight.SEMIBOLD);

        Span author = new Span("by " + skin.author);
        author.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY);

        // Stats
        HorizontalLayout stats = new HorizontalLayout();
        stats.setSpacing(true);
        stats.setWidthFull();
        stats.getElement().getStyle().set("margin-top", "8px");

        stats.add(createStatSpan("❤️", skin.likes));
        stats.add(createStatSpan("⬇️", skin.downloads));
        stats.add(createStatSpan("👁️", skin.views));

        info.add(name, author, stats);

        // Action buttons (shown on hover)
        HorizontalLayout actions = new HorizontalLayout();
        actions.setWidthFull();
        actions.setJustifyContentMode(FlexComponent.JustifyContentMode.EVENLY);
        actions.setPadding(true);
        actions.getElement().getStyle()
            .set("background", "var(--lumo-contrast-10pct)")
            .set("border-top", "1px solid var(--lumo-contrast-10pct)");

        Button likeButton = new Button(VaadinIcon.HEART.create());
        likeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        if (skin.liked) {
            likeButton.getElement().getStyle().set("color", "#FF4081");
        }
        likeButton.addClickListener(e -> {
            Notification.show("已添加到喜欢", 2000, Notification.Position.BOTTOM_CENTER);
        });

        Button downloadButton = new Button(VaadinIcon.DOWNLOAD.create());
        downloadButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        downloadButton.addClickListener(e -> {
            Notification.show("下载已开始", 2000, Notification.Position.BOTTOM_CENTER);
        });

        Button useButton = new Button("使用");
        useButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        useButton.addClickListener(e -> {
            Notification.show("皮肤已应用！", 2000, Notification.Position.BOTTOM_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });

        actions.add(likeButton, downloadButton, useButton);

        card.add(previewContainer, info, actions);
        return card;
    }

    private Span createStatSpan(String icon, int value) {
        Span span = new Span(icon + " " + formatNumber(value));
        span.addClassNames(LumoUtility.FontSize.XSMALL, LumoUtility.TextColor.SECONDARY);
        return span;
    }

    private String formatNumber(int value) {
        if (value >= 1000) {
            return String.format("%.1fk", value / 1000.0);
        }
        return String.valueOf(value);
    }

    private void openUploadDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("上传皮肤");
        dialog.setWidth("500px");

        VerticalLayout content = new VerticalLayout();
        content.setSpacing(true);
        content.setPadding(false);

        // Name field
        TextField nameField = new TextField("皮肤名称");
        nameField.setWidthFull();
        nameField.setRequired(true);

        // Model selection
        Select<String> modelSelect = new Select<>();
        modelSelect.setLabel("皮肤模型");
        modelSelect.setItems("Steve (经典 4px 手臂)", "Alex (纤细 3px 手臂)");
        modelSelect.setValue("Steve (经典 4px 手臂)");
        modelSelect.setWidthFull();

        // File upload
        MemoryBuffer buffer = new MemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.setAcceptedFileTypes("image/png");
        upload.setMaxFileSize(1024 * 1024); // 1MB
        upload.setWidthFull();

        Paragraph uploadHint = new Paragraph("上传 64x64 或 64x32 的 PNG 图片");
        uploadHint.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY);

        // Tags
        TextField tagsField = new TextField("标签");
        tagsField.setWidthFull();
        tagsField.setPlaceholder("用逗号分隔，如: 原创, 动漫, 角色扮演");

        // Privacy
        Select<String> privacySelect = new Select<>();
        privacySelect.setLabel("隐私设置");
        privacySelect.setItems("公开 (所有人可见)", "私密 (仅自己可见)");
        privacySelect.setValue("公开 (所有人可见)");
        privacySelect.setWidthFull();

        content.add(nameField, modelSelect, upload, uploadHint, tagsField, privacySelect);
        dialog.add(content);

        // Footer buttons
        Button cancelButton = new Button("取消", e -> dialog.close());
        Button uploadBtn = new Button("上传", e -> {
            // Handle upload
            Notification.show("皮肤上传成功！", 3000, Notification.Position.BOTTOM_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            dialog.close();
        });
        uploadBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        dialog.getFooter().add(cancelButton, uploadBtn);

        dialog.open();
    }

    // Data record
    private record SkinData(
        String name,
        String author,
        String model,
        int likes,
        int downloads,
        int views,
        boolean liked
    ) {}
}
