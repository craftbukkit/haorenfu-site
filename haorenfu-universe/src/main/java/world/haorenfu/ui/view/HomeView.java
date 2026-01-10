/*
 * ═══════════════════════════════════════════════════════════════════════════
 *                           HOME VIEW
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * The landing page showcasing the server and community.
 * Features a hero section, server info, and recent activity.
 */
package world.haorenfu.ui.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.theme.lumo.LumoUtility;
import world.haorenfu.domain.forum.ForumService;
import world.haorenfu.domain.server.ServerStatusService;
import world.haorenfu.ui.layout.MainLayout;

/**
 * Home page view.
 */
@Route(value = "home", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
@PageTitle("首页 | 好人服")
@AnonymousAllowed
public class HomeView extends VerticalLayout {

    private final ServerStatusService serverStatusService;
    private final ForumService forumService;

    public HomeView(ServerStatusService serverStatusService, ForumService forumService) {
        this.serverStatusService = serverStatusService;
        this.forumService = forumService;

        setSpacing(false);
        setPadding(false);
        setSizeFull();

        add(
            createHeroSection(),
            createInfoCardsSection(),
            createFeaturesSection(),
            createRecentActivitySection(),
            createFooter()
        );
    }

    private Component createHeroSection() {
        Div hero = new Div();
        hero.setWidthFull();
        hero.getStyle()
            .set("background", "linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%)")
            .set("padding", "80px 20px")
            .set("text-align", "center")
            .set("position", "relative")
            .set("overflow", "hidden");

        // Animated background particles (CSS animation via style)
        Div particles = new Div();
        particles.getStyle()
            .set("position", "absolute")
            .set("top", "0")
            .set("left", "0")
            .set("right", "0")
            .set("bottom", "0")
            .set("background-image", "radial-gradient(circle, rgba(76,175,80,0.1) 1px, transparent 1px)")
            .set("background-size", "50px 50px")
            .set("opacity", "0.5");

        // Content
        VerticalLayout content = new VerticalLayout();
        content.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.CENTER);
        content.setSpacing(true);
        content.getStyle().set("position", "relative").set("z-index", "1");

        // Logo/Icon
        Span minecraftIcon = new Span("⛏️");
        minecraftIcon.getStyle()
            .set("font-size", "64px")
            .set("display", "block")
            .set("margin-bottom", "16px");

        H1 title = new H1("好人服");
        title.getStyle()
            .set("color", "#4CAF50")
            .set("font-size", "4rem")
            .set("margin", "0")
            .set("text-shadow", "0 0 20px rgba(76,175,80,0.5)");

        Paragraph subtitle = new Paragraph("一个和谐的 Minecraft 社区");
        subtitle.getStyle()
            .set("color", "#e0e0e0")
            .set("font-size", "1.5rem")
            .set("margin-top", "8px");

        // Server address (copyable)
        HorizontalLayout addressBox = new HorizontalLayout();
        addressBox.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        addressBox.getStyle()
            .set("background-color", "rgba(255,255,255,0.1)")
            .set("padding", "16px 32px")
            .set("border-radius", "8px")
            .set("margin-top", "24px");

        Span addressLabel = new Span("服务器地址：");
        addressLabel.getStyle().set("color", "#aaa");

        Span address = new Span("haorenfu.cn");
        address.getStyle()
            .set("color", "#4CAF50")
            .set("font-size", "1.5rem")
            .set("font-weight", "bold")
            .set("font-family", "monospace");

        Button copyBtn = new Button(VaadinIcon.COPY.create());
        copyBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        copyBtn.addClickListener(e -> {
            copyBtn.getElement().executeJs(
                "navigator.clipboard.writeText('haorenfu.cn');" +
                "this.querySelector('vaadin-icon').setAttribute('icon', 'vaadin:check');" +
                "setTimeout(() => this.querySelector('vaadin-icon').setAttribute('icon', 'vaadin:copy'), 2000);"
            );
        });

        addressBox.add(addressLabel, address, copyBtn);

        // CTA Buttons
        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);
        buttons.getStyle().set("margin-top", "32px");

        Button joinBtn = new Button("立即加入", VaadinIcon.PLAY.create());
        joinBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        joinBtn.getStyle()
            .set("background-color", "#4CAF50")
            .set("color", "white");
        joinBtn.addClickListener(e -> joinBtn.getUI().ifPresent(ui -> ui.navigate(JoinView.class)));

        Button forumBtn = new Button("浏览论坛", VaadinIcon.COMMENTS.create());
        forumBtn.addThemeVariants(ButtonVariant.LUMO_LARGE, ButtonVariant.LUMO_CONTRAST);
        forumBtn.addClickListener(e -> forumBtn.getUI().ifPresent(ui -> ui.navigate(ForumView.class)));

        buttons.add(joinBtn, forumBtn);

        content.add(minecraftIcon, title, subtitle, addressBox, buttons);
        hero.add(particles, content);

        return hero;
    }

    private Component createInfoCardsSection() {
        HorizontalLayout section = new HorizontalLayout();
        section.setWidthFull();
        section.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        section.setSpacing(true);
        section.getStyle()
            .set("padding", "40px 20px")
            .set("background-color", "#16213e")
            .set("flex-wrap", "wrap");

        // Server status card
        section.add(createInfoCard(
            "🟢",
            "服务器状态",
            "在线",
            "Minecraft 1.21"
        ));

        // Founded card
        section.add(createInfoCard(
            "📅",
            "创立时间",
            "2013年5月",
            "超过10年历史"
        ));

        // Players card
        int onlinePlayers = serverStatusService.getTotalOnlinePlayers();
        section.add(createInfoCard(
            "👥",
            "在线玩家",
            String.valueOf(onlinePlayers),
            "快来加入我们"
        ));

        // Community card
        section.add(createInfoCard(
            "💬",
            "QQ群",
            "302805107",
            "活跃的玩家社区"
        ));

        return section;
    }

    private Component createInfoCard(String icon, String title, String value, String description) {
        VerticalLayout card = new VerticalLayout();
        card.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.CENTER);
        card.setWidth("200px");
        card.getStyle()
            .set("background-color", "#1a1a2e")
            .set("padding", "24px")
            .set("border-radius", "12px")
            .set("box-shadow", "0 4px 6px rgba(0,0,0,0.3)")
            .set("transition", "transform 0.3s")
            .set("cursor", "default");

        // Hover effect
        card.getElement().addEventListener("mouseenter", e ->
            card.getStyle().set("transform", "translateY(-5px)")
        );
        card.getElement().addEventListener("mouseleave", e ->
            card.getStyle().set("transform", "translateY(0)")
        );

        Span iconSpan = new Span(icon);
        iconSpan.getStyle().set("font-size", "32px");

        Span titleSpan = new Span(title);
        titleSpan.getStyle()
            .set("color", "#888")
            .set("font-size", "14px");

        Span valueSpan = new Span(value);
        valueSpan.getStyle()
            .set("color", "#4CAF50")
            .set("font-size", "24px")
            .set("font-weight", "bold");

        Span descSpan = new Span(description);
        descSpan.getStyle()
            .set("color", "#666")
            .set("font-size", "12px");

        card.add(iconSpan, titleSpan, valueSpan, descSpan);

        return card;
    }

    private Component createFeaturesSection() {
        VerticalLayout section = new VerticalLayout();
        section.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.CENTER);
        section.setWidthFull();
        section.getStyle()
            .set("padding", "60px 20px")
            .set("background-color", "#1a1a2e");

        H2 sectionTitle = new H2("为什么选择好人服？");
        sectionTitle.getStyle()
            .set("color", "#fff")
            .set("margin-bottom", "40px");

        HorizontalLayout features = new HorizontalLayout();
        features.setWidthFull();
        features.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        features.setSpacing(true);
        features.getStyle().set("flex-wrap", "wrap");

        features.add(createFeatureCard(
            VaadinIcon.SHIELD,
            "纯净生存",
            "无作弊、无P2W，公平的游戏环境让每位玩家都能享受原汁原味的Minecraft体验。"
        ));

        features.add(createFeatureCard(
            VaadinIcon.USERS,
            "友善社区",
            "超过10年的社区积淀，这里聚集了一群热爱Minecraft、乐于分享的好朋友。"
        ));

        features.add(createFeatureCard(
            VaadinIcon.ROCKET,
            "稳定运行",
            "专业的服务器硬件和优化，确保24/7稳定运行，让你随时畅玩。"
        ));

        features.add(createFeatureCard(
            VaadinIcon.STAR,
            "精彩活动",
            "定期举办各种社区活动，从建筑比赛到生存挑战，乐趣无穷。"
        ));

        section.add(sectionTitle, features);

        return section;
    }

    private Component createFeatureCard(VaadinIcon iconType, String title, String description) {
        VerticalLayout card = new VerticalLayout();
        card.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.CENTER);
        card.setWidth("280px");
        card.getStyle()
            .set("padding", "32px")
            .set("text-align", "center");

        Icon icon = iconType.create();
        icon.setSize("48px");
        icon.setColor("#4CAF50");

        H3 cardTitle = new H3(title);
        cardTitle.getStyle()
            .set("color", "#fff")
            .set("margin", "16px 0 8px 0");

        Paragraph desc = new Paragraph(description);
        desc.getStyle()
            .set("color", "#aaa")
            .set("font-size", "14px")
            .set("line-height", "1.6");

        card.add(icon, cardTitle, desc);

        return card;
    }

    private Component createRecentActivitySection() {
        VerticalLayout section = new VerticalLayout();
        section.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.CENTER);
        section.setWidthFull();
        section.getStyle()
            .set("padding", "60px 20px")
            .set("background-color", "#16213e");

        H2 sectionTitle = new H2("最新动态");
        sectionTitle.getStyle()
            .set("color", "#fff")
            .set("margin-bottom", "32px");

        // Trending posts
        var trendingPosts = forumService.getTrendingPosts(5);

        VerticalLayout postList = new VerticalLayout();
        postList.setWidth("800px");
        postList.setMaxWidth("100%");
        postList.setSpacing(true);

        if (trendingPosts.isEmpty()) {
            Paragraph empty = new Paragraph("暂无帖子，成为第一个发帖的人吧！");
            empty.getStyle().set("color", "#888");
            postList.add(empty);
        } else {
            trendingPosts.forEach(post -> {
                HorizontalLayout postItem = new HorizontalLayout();
                postItem.setWidthFull();
                postItem.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
                postItem.getStyle()
                    .set("background-color", "#1a1a2e")
                    .set("padding", "16px")
                    .set("border-radius", "8px")
                    .set("cursor", "pointer");

                Span categoryIcon = new Span(post.getCategory().getIcon());
                categoryIcon.getStyle().set("font-size", "24px");

                VerticalLayout postInfo = new VerticalLayout();
                postInfo.setSpacing(false);
                postInfo.setPadding(false);

                Span postTitle = new Span(post.getTitle());
                postTitle.getStyle()
                    .set("color", "#fff")
                    .set("font-weight", "500");

                Span postMeta = new Span(
                    post.getAuthor().getUsername() + " · " +
                    post.getCommentCount() + " 回复 · " +
                    post.getUpvotes() + " 赞"
                );
                postMeta.getStyle()
                    .set("color", "#888")
                    .set("font-size", "12px");

                postInfo.add(postTitle, postMeta);

                postItem.add(categoryIcon, postInfo);
                postItem.setFlexGrow(1, postInfo);

                postItem.addClickListener(e ->
                    postItem.getUI().ifPresent(ui ->
                        ui.navigate("forum/post/" + post.getId())
                    )
                );

                postList.add(postItem);
            });
        }

        Button moreBtn = new Button("查看更多", VaadinIcon.ARROW_RIGHT.create());
        moreBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        moreBtn.getStyle().set("margin-top", "16px");
        moreBtn.addClickListener(e ->
            moreBtn.getUI().ifPresent(ui -> ui.navigate(ForumView.class))
        );

        section.add(sectionTitle, postList, moreBtn);

        return section;
    }

    private Component createFooter() {
        VerticalLayout footer = new VerticalLayout();
        footer.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.CENTER);
        footer.setWidthFull();
        footer.getStyle()
            .set("padding", "40px 20px")
            .set("background-color", "#0f0f1a")
            .set("border-top", "1px solid #333");

        Paragraph copyright = new Paragraph("© 2013-2026 好人服 · 与玩家共同成长的Minecraft社区");
        copyright.getStyle()
            .set("color", "#666")
            .set("font-size", "14px");

        HorizontalLayout links = new HorizontalLayout();
        links.setSpacing(true);

        Anchor rulesLink = new Anchor("rules", "服务器规则");
        rulesLink.getStyle().set("color", "#888");

        Anchor joinLink = new Anchor("join", "加入我们");
        joinLink.getStyle().set("color", "#888");

        Anchor githubLink = new Anchor("#", "项目源码");
        githubLink.getStyle().set("color", "#888");

        links.add(rulesLink, new Span("·"), joinLink, new Span("·"), githubLink);

        footer.add(copyright, links);

        return footer;
    }
}
