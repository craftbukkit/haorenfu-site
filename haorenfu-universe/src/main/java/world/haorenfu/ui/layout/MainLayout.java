/*
 * ═══════════════════════════════════════════════════════════════════════════
 *                          MAIN LAYOUT
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * The primary application layout providing navigation and structure.
 * Built entirely in Java using Vaadin components - no HTML/CSS/JS required.
 *
 * Design Philosophy:
 * - Responsive design adapts to all screen sizes
 * - Dark theme inspired by Minecraft's aesthetic
 * - Intuitive navigation hierarchy
 */
package world.haorenfu.ui.layout;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.theme.lumo.LumoUtility;
import world.haorenfu.core.security.AuthenticationService;
import world.haorenfu.domain.user.User;
import world.haorenfu.ui.view.*;

import java.util.Optional;

/**
 * Main application layout with navigation.
 */
public class MainLayout extends AppLayout {

    private final AuthenticationService authService;
    private H1 viewTitle;

    public MainLayout(AuthenticationService authService) {
        this.authService = authService;

        setPrimarySection(Section.DRAWER);
        addDrawerContent();
        addHeaderContent();

        // Apply dark theme styling
        getElement().getStyle()
            .set("--lumo-primary-color", "#4CAF50")
            .set("--lumo-primary-text-color", "#4CAF50")
            .set("--lumo-base-color", "#1a1a2e")
            .set("--lumo-header-text-color", "#ffffff")
            .set("--lumo-body-text-color", "#e0e0e0");
    }

    private void addHeaderContent() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.setAriaLabel("菜单切换");

        viewTitle = new H1();
        viewTitle.addClassNames(
            LumoUtility.FontSize.LARGE,
            LumoUtility.Margin.NONE
        );

        HorizontalLayout header = new HorizontalLayout();
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.setWidthFull();
        header.addClassNames(
            LumoUtility.Padding.Vertical.NONE,
            LumoUtility.Padding.Horizontal.MEDIUM
        );

        header.add(toggle, viewTitle);

        // Add user section
        header.add(createUserSection());

        addToNavbar(true, header);
    }

    private Component createUserSection() {
        HorizontalLayout userSection = new HorizontalLayout();
        userSection.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        userSection.setSpacing(true);
        userSection.getStyle().set("margin-left", "auto");

        Optional<User> userOpt = authService.getAuthenticatedUser();

        if (userOpt.isPresent()) {
            User user = userOpt.get();

            // User avatar and name
            Avatar avatar = new Avatar(user.getUsername());
            if (user.getAvatarUrl() != null) {
                avatar.setImage(user.getAvatarUrl());
            }

            Span userName = new Span(user.getUsername());
            userName.addClassNames(LumoUtility.FontWeight.SEMIBOLD);

            // Reputation badge
            Span repBadge = new Span("⭐ " + user.getReputation());
            repBadge.getStyle()
                .set("background-color", "var(--lumo-primary-color)")
                .set("color", "white")
                .set("padding", "2px 8px")
                .set("border-radius", "12px")
                .set("font-size", "12px");

            Button logoutBtn = new Button("退出", e -> authService.logout());
            logoutBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);

            userSection.add(repBadge, avatar, userName, logoutBtn);
        } else {
            Button loginBtn = new Button("登录");
            loginBtn.addClickListener(e ->
                loginBtn.getUI().ifPresent(ui -> ui.navigate(LoginView.class))
            );

            Button registerBtn = new Button("注册");
            registerBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            registerBtn.addClickListener(e ->
                registerBtn.getUI().ifPresent(ui -> ui.navigate(RegisterView.class))
            );

            userSection.add(loginBtn, registerBtn);
        }

        return userSection;
    }

    private void addDrawerContent() {
        // Logo section
        HorizontalLayout logoLayout = new HorizontalLayout();
        logoLayout.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        logoLayout.addClassNames(
            LumoUtility.Padding.Horizontal.LARGE,
            LumoUtility.Padding.Vertical.MEDIUM
        );

        // Minecraft-style logo
        Span logo = new Span("⛏");
        logo.getStyle()
            .set("font-size", "32px")
            .set("margin-right", "8px");

        H2 title = new H2("好人服");
        title.addClassNames(
            LumoUtility.FontSize.XLARGE,
            LumoUtility.Margin.NONE,
            LumoUtility.FontWeight.BOLD
        );
        title.getStyle().set("color", "#4CAF50");

        Span subtitle = new Span("Universe");
        subtitle.addClassNames(LumoUtility.FontSize.SMALL);
        subtitle.getStyle()
            .set("color", "#888")
            .set("margin-left", "4px");

        logoLayout.add(logo, title, subtitle);

        // Navigation
        SideNav nav = createNavigation();
        Scroller scroller = new Scroller(nav);

        // Server status mini display
        Component serverStatus = createServerStatusWidget();

        addToDrawer(logoLayout, scroller, serverStatus);
    }

    private SideNav createNavigation() {
        SideNav nav = new SideNav();

        // Main navigation items
        nav.addItem(new SideNavItem("首页", HomeView.class, VaadinIcon.HOME.create()));
        nav.addItem(new SideNavItem("服务器状态", ServerStatusView.class, VaadinIcon.SERVER.create()));
        nav.addItem(new SideNavItem("论坛", ForumView.class, VaadinIcon.COMMENTS.create()));
        nav.addItem(new SideNavItem("百科", WikiView.class, VaadinIcon.BOOK.create()));
        nav.addItem(new SideNavItem("排行榜", RankingView.class, VaadinIcon.TROPHY.create()));
        nav.addItem(new SideNavItem("成就", AchievementView.class, VaadinIcon.MEDAL.create()));

        // Rules and join
        nav.addItem(new SideNavItem("服务器规则", RulesView.class, VaadinIcon.FILE_TEXT.create()));
        nav.addItem(new SideNavItem("加入我们", JoinView.class, VaadinIcon.PLUS_CIRCLE.create()));

        // Admin section (conditional)
        if (authService.isAuthenticated()) {
            authService.getAuthenticatedUser().ifPresent(user -> {
                if (user.getRole().getLevel() >= 3) {
                    SideNavItem adminItem = new SideNavItem("管理后台", AdminView.class, VaadinIcon.COG.create());
                    nav.addItem(adminItem);
                }
            });
        }

        return nav;
    }

    private Component createServerStatusWidget() {
        VerticalLayout widget = new VerticalLayout();
        widget.setSpacing(false);
        widget.setPadding(true);
        widget.addClassNames(LumoUtility.Margin.Top.AUTO);
        widget.getStyle()
            .set("background-color", "rgba(76, 175, 80, 0.1)")
            .set("border-radius", "8px")
            .set("margin", "16px");

        // Status indicator
        HorizontalLayout statusLine = new HorizontalLayout();
        statusLine.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);

        Span statusDot = new Span("●");
        statusDot.getStyle()
            .set("color", "#4CAF50")
            .set("font-size", "12px")
            .set("animation", "pulse 2s infinite");

        Span statusText = new Span("服务器在线");
        statusText.addClassNames(LumoUtility.FontSize.SMALL);

        statusLine.add(statusDot, statusText);

        // Server address
        Span address = new Span("haorenfu.cn");
        address.addClassNames(
            LumoUtility.FontSize.SMALL,
            LumoUtility.FontWeight.BOLD
        );
        address.getStyle()
            .set("color", "#4CAF50")
            .set("cursor", "pointer");

        // Copy to clipboard on click
        address.getElement().addEventListener("click", e -> {
            address.getElement().executeJs(
                "navigator.clipboard.writeText('haorenfu.cn');" +
                "this.innerText = '已复制!';" +
                "setTimeout(() => this.innerText = 'haorenfu.cn', 2000);"
            );
        });

        widget.add(statusLine, address);

        return widget;
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        viewTitle.setText(getCurrentPageTitle());
    }

    private String getCurrentPageTitle() {
        PageTitle title = getContent().getClass().getAnnotation(PageTitle.class);
        return title == null ? "" : title.value();
    }
}
