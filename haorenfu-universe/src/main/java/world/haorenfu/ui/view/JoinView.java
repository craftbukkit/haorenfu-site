/*
 * ═══════════════════════════════════════════════════════════════════════════
 *                          JOIN VIEW
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Guide for new players to join the server.
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
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import world.haorenfu.ui.layout.MainLayout;

/**
 * View with instructions for joining the server.
 */
@Route(value = "join", layout = MainLayout.class)
@PageTitle("加入我们 | 好人服")
@AnonymousAllowed
public class JoinView extends VerticalLayout {

    public JoinView() {
        setWidthFull();
        setMaxWidth("900px");
        getStyle().set("margin", "0 auto");
        setPadding(true);

        add(
            createHeader(),
            createInfoBanner(),
            createStepsSection(),
            createRequirementsSection(),
            createFAQSection()
        );
    }

    private Component createHeader() {
        VerticalLayout header = new VerticalLayout();
        header.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.setPadding(false);

        Icon icon = VaadinIcon.PLUS_CIRCLE.create();
        icon.setSize("48px");
        icon.setColor("#4CAF50");

        H1 title = new H1("加入好人服");
        title.getStyle().set("margin", "16px 0 8px 0");

        Paragraph subtitle = new Paragraph("欢迎来到我们的Minecraft大家庭！按照以下步骤加入服务器。");
        subtitle.getStyle()
            .set("color", "var(--lumo-secondary-text-color)")
            .set("text-align", "center");

        header.add(icon, title, subtitle);

        return header;
    }

    private Component createInfoBanner() {
        HorizontalLayout banner = new HorizontalLayout();
        banner.setWidthFull();
        banner.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        banner.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        banner.getStyle()
            .set("background", "linear-gradient(135deg, #4CAF50 0%, #45a049 100%)")
            .set("padding", "24px")
            .set("border-radius", "12px")
            .set("margin", "24px 0")
            .set("flex-wrap", "wrap")
            .set("gap", "24px");

        // Server address
        VerticalLayout addressSection = new VerticalLayout();
        addressSection.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.CENTER);
        addressSection.setPadding(false);
        addressSection.setSpacing(false);

        Span addressLabel = new Span("服务器地址");
        addressLabel.getStyle()
            .set("color", "rgba(255,255,255,0.8)")
            .set("font-size", "14px");

        Span address = new Span("haorenfu.cn");
        address.getStyle()
            .set("color", "white")
            .set("font-size", "28px")
            .set("font-weight", "bold")
            .set("font-family", "monospace");

        Button copyBtn = new Button("复制地址", VaadinIcon.COPY.create());
        copyBtn.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        copyBtn.getStyle()
            .set("margin-top", "8px")
            .set("background-color", "rgba(255,255,255,0.2)")
            .set("color", "white");
        copyBtn.addClickListener(e -> {
            copyBtn.getElement().executeJs(
                "navigator.clipboard.writeText('haorenfu.cn');" +
                "this.querySelector('span').innerText = '已复制!';" +
                "setTimeout(() => this.querySelector('span').innerText = '复制地址', 2000);"
            );
        });

        addressSection.add(addressLabel, address, copyBtn);

        // QQ Group
        VerticalLayout qqSection = new VerticalLayout();
        qqSection.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.CENTER);
        qqSection.setPadding(false);
        qqSection.setSpacing(false);

        Span qqLabel = new Span("QQ群号");
        qqLabel.getStyle()
            .set("color", "rgba(255,255,255,0.8)")
            .set("font-size", "14px");

        Span qqNumber = new Span("302805107");
        qqNumber.getStyle()
            .set("color", "white")
            .set("font-size", "28px")
            .set("font-weight", "bold")
            .set("font-family", "monospace");

        Button qqCopyBtn = new Button("复制群号", VaadinIcon.COPY.create());
        qqCopyBtn.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        qqCopyBtn.getStyle()
            .set("margin-top", "8px")
            .set("background-color", "rgba(255,255,255,0.2)")
            .set("color", "white");
        qqCopyBtn.addClickListener(e -> {
            qqCopyBtn.getElement().executeJs(
                "navigator.clipboard.writeText('302805107');" +
                "this.querySelector('span').innerText = '已复制!';" +
                "setTimeout(() => this.querySelector('span').innerText = '复制群号', 2000);"
            );
        });

        qqSection.add(qqLabel, qqNumber, qqCopyBtn);

        banner.add(addressSection, qqSection);

        return banner;
    }

    private Component createStepsSection() {
        VerticalLayout section = new VerticalLayout();
        section.setSpacing(true);
        section.setPadding(false);

        H2 title = new H2("加入步骤");
        title.getStyle().set("margin-bottom", "16px");

        section.add(title);
        section.add(createStep(1, "阅读服务器规则", 
            "在加入之前，请先阅读并同意我们的服务器规则。规则旨在维护和谐的游戏环境。",
            "rules", "查看规则", VaadinIcon.FILE_TEXT));

        section.add(createStep(2, "加入QQ群",
            "加入我们的QQ群 302805107，这是获取服务器动态、与其他玩家交流的主要渠道。",
            null, null, VaadinIcon.USERS));

        section.add(createStep(3, "注册网站账号",
            "在本网站注册一个账号，用于管理你的服务器资料和参与社区互动。",
            "register", "立即注册", VaadinIcon.USER));

        section.add(createStep(4, "提交白名单申请",
            "在QQ群内联系服主，提供你的正版Minecraft ID，服主会为你添加白名单。",
            null, null, VaadinIcon.CHECK));

        section.add(createStep(5, "开始游戏！",
            "打开Minecraft，添加服务器地址 haorenfu.cn，加入游戏，开始你的冒险之旅！",
            null, null, VaadinIcon.GAMEPAD));

        return section;
    }

    private Component createStep(int number, String title, String description, 
                                  String linkTarget, String linkText, VaadinIcon iconType) {
        HorizontalLayout step = new HorizontalLayout();
        step.setWidthFull();
        step.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.START);
        step.getStyle()
            .set("background-color", "var(--lumo-contrast-5pct)")
            .set("padding", "24px")
            .set("border-radius", "12px");

        // Step number
        Div numberCircle = new Div();
        numberCircle.setText(String.valueOf(number));
        numberCircle.getStyle()
            .set("background-color", "#4CAF50")
            .set("color", "white")
            .set("width", "40px")
            .set("height", "40px")
            .set("border-radius", "50%")
            .set("display", "flex")
            .set("align-items", "center")
            .set("justify-content", "center")
            .set("font-weight", "bold")
            .set("font-size", "18px")
            .set("flex-shrink", "0");

        // Content
        VerticalLayout content = new VerticalLayout();
        content.setSpacing(false);
        content.setPadding(false);

        HorizontalLayout titleRow = new HorizontalLayout();
        titleRow.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);

        Icon icon = iconType.create();
        icon.setSize("24px");
        icon.setColor("#4CAF50");

        H3 stepTitle = new H3(title);
        stepTitle.getStyle()
            .set("margin", "0 0 0 8px")
            .set("font-size", "18px");

        titleRow.add(icon, stepTitle);

        Paragraph desc = new Paragraph(description);
        desc.getStyle()
            .set("color", "var(--lumo-secondary-text-color)")
            .set("margin", "8px 0 0 0")
            .set("line-height", "1.6");

        content.add(titleRow, desc);

        // Optional action button
        if (linkTarget != null && linkText != null) {
            Button actionBtn = new Button(linkText, VaadinIcon.ARROW_RIGHT.create());
            actionBtn.addThemeVariants(ButtonVariant.LUMO_SMALL);
            actionBtn.getStyle()
                .set("margin-top", "12px")
                .set("background-color", "#4CAF50")
                .set("color", "white");
            actionBtn.addClickListener(e ->
                actionBtn.getUI().ifPresent(ui -> ui.navigate(linkTarget))
            );
            content.add(actionBtn);
        }

        step.add(numberCircle, content);
        step.setFlexGrow(1, content);

        return step;
    }

    private Component createRequirementsSection() {
        VerticalLayout section = new VerticalLayout();
        section.setSpacing(true);
        section.setPadding(false);
        section.getStyle().set("margin-top", "32px");

        H2 title = new H2("加入要求");
        title.getStyle().set("margin-bottom", "16px");

        HorizontalLayout requirements = new HorizontalLayout();
        requirements.setWidthFull();
        requirements.setSpacing(true);
        requirements.getStyle().set("flex-wrap", "wrap");

        requirements.add(createRequirementCard("正版Minecraft", 
            "我们是正版服务器，需要使用正版Minecraft Java版才能加入。", 
            VaadinIcon.CHECK_CIRCLE));

        requirements.add(createRequirementCard("遵守规则", 
            "愿意遵守服务器规则，与其他玩家和谐相处。", 
            VaadinIcon.HANDSHAKE));

        requirements.add(createRequirementCard("活跃参与", 
            "保持一定的活跃度，长期不上线可能会被移出白名单。", 
            VaadinIcon.CLOCK));

        section.add(title, requirements);

        return section;
    }

    private Component createRequirementCard(String title, String description, VaadinIcon iconType) {
        VerticalLayout card = new VerticalLayout();
        card.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.CENTER);
        card.setWidth("280px");
        card.getStyle()
            .set("background-color", "var(--lumo-contrast-5pct)")
            .set("padding", "24px")
            .set("border-radius", "12px")
            .set("text-align", "center");

        Icon icon = iconType.create();
        icon.setSize("32px");
        icon.setColor("#4CAF50");

        H3 cardTitle = new H3(title);
        cardTitle.getStyle().set("margin", "16px 0 8px 0");

        Paragraph desc = new Paragraph(description);
        desc.getStyle()
            .set("color", "var(--lumo-secondary-text-color)")
            .set("margin", "0")
            .set("font-size", "14px");

        card.add(icon, cardTitle, desc);

        return card;
    }

    private Component createFAQSection() {
        VerticalLayout section = new VerticalLayout();
        section.setSpacing(true);
        section.setPadding(false);
        section.getStyle().set("margin-top", "32px");

        H2 title = new H2("常见问题");
        title.getStyle().set("margin-bottom", "16px");

        section.add(title);
        section.add(createFAQItem("我没有正版Minecraft怎么办？",
            "很抱歉，好人服是正版服务器，必须使用正版Minecraft才能加入。" +
            "你可以在Minecraft官网或各大游戏平台购买正版游戏。"));

        section.add(createFAQItem("服务器是什么版本？",
            "我们目前运行的是Minecraft 1.21版本。服务器会随官方更新而更新，" +
            "具体版本信息请关注QQ群公告。"));

        section.add(createFAQItem("我可以使用模组吗？",
            "可以使用优化类模组（如Optifine、Sodium）和小地图模组。" +
            "但严禁使用任何作弊性质的模组。"));

        section.add(createFAQItem("怎么联系管理员？",
            "可以在QQ群内直接@管理员，或者在论坛发帖寻求帮助。"));

        return section;
    }

    private Component createFAQItem(String question, String answer) {
        VerticalLayout item = new VerticalLayout();
        item.setSpacing(false);
        item.setPadding(true);
        item.getStyle()
            .set("background-color", "var(--lumo-contrast-5pct)")
            .set("border-radius", "8px");

        H4 q = new H4("❓ " + question);
        q.getStyle().set("margin", "0 0 8px 0");

        Paragraph a = new Paragraph(answer);
        a.getStyle()
            .set("color", "var(--lumo-secondary-text-color)")
            .set("margin", "0")
            .set("line-height", "1.6");

        item.add(q, a);

        return item;
    }
}
