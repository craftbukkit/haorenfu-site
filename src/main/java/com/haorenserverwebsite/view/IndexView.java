package com.haorenserverwebsite.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

/**
 * 网站主页
 * 
 * 设计参考了一些日系游戏官网的风格，主要特点：
 * - 毛玻璃质感的卡片
 * - 柔和的渐变配色
 * - 响应式布局适配手机
 */
@Route("")
@PageTitle("Minecraft好人服务器")
@AnonymousAllowed
@CssImport("./themes/mctheme/main.css")
public class IndexView extends VerticalLayout {

    // 服务器基本信息，后面可能会改成从配置文件读取
    private static final String VERSION = "1.21.10";
    private static final String CORE_TYPE = "Paper";
    private static final String QQ_NUM = "302805107";

    public IndexView() {
        addClassName("page-root");
        setSizeFull();
        setPadding(false);
        setSpacing(false);

        // 背景层
        add(buildBackground());

        // 内容区
        Div mainContent = new Div();
        mainContent.addClassName("main-content");
        mainContent.add(
            buildNavBar(),
            buildHeroArea(),
            buildInfoCards(),
            buildNoticeBoard(),
            buildJoinSection(),
            buildPageFooter()
        );
        add(mainContent);

        // 页面加载后初始化JS
        getElement().executeJs(
            "setTimeout(function(){" +
            "  window.initCountdown && window.initCountdown();" +
            "}, 100);"
        );
    }

    private Div buildBackground() {
        Div bg = new Div();
        bg.addClassName("bg-layer");

        Div img = new Div();
        img.addClassName("bg-img");
        bg.add(img);

        Div mask = new Div();
        mask.addClassName("bg-mask");
        bg.add(mask);

        return bg;
    }

    private Nav buildNavBar() {
        Nav nav = new Nav();
        nav.addClassName("top-nav");

        // 站名
        Div logo = new Div();
        logo.addClassName("nav-logo");

        Span siteName = new Span("Minecraft好人服务器");
        siteName.addClassName("site-name");

        logo.add(siteName);

        // 导航菜单
        HorizontalLayout menu = new HorizontalLayout();
        menu.addClassName("nav-menu");
        menu.setSpacing(false);

        menu.add(createNavItem("首页", "#top"));
        menu.add(createNavItem("信息", "#info"));
        menu.add(createNavItem("公告", "#notice"));
        menu.add(createNavItem("加入", "#join"));

        // 手机端菜单按钮
        Button menuBtn = new Button(VaadinIcon.MENU.create());
        menuBtn.addClassName("mobile-menu-btn");
        menuBtn.addClickListener(e -> {
            UI.getCurrent().getPage().executeJs(
                "document.querySelector('.nav-menu').classList.toggle('show')"
            );
        });

        HorizontalLayout wrapper = new HorizontalLayout(logo, menu, menuBtn);
        wrapper.addClassName("nav-wrapper");
        wrapper.setWidthFull();
        wrapper.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        wrapper.setAlignItems(FlexComponent.Alignment.CENTER);

        nav.add(wrapper);
        return nav;
    }

    private Anchor createNavItem(String text, String href) {
        Anchor a = new Anchor(href, text);
        a.addClassName("nav-item");
        return a;
    }

    private Section buildHeroArea() {
        Section hero = new Section();
        hero.addClassName("hero");
        hero.setId("top");

        Div content = new Div();
        content.addClassName("hero-inner");

        H1 title = new H1("Minecraft好人服务器");
        title.addClassName("hero-title");

        Paragraph descP = new Paragraph("一个拥有众多高级玩家和技术高手的运营了13年的服务器");
        descP.addClassName("hero-desc");

        // 版本标签
        Div tags = new Div();
        tags.addClassName("hero-tags");

        Span v1 = new Span(VERSION);
        v1.addClassName("tag tag-blue");

        Span v2 = new Span(CORE_TYPE);
        v2.addClassName("tag tag-purple");

        tags.add(v1, v2);

        // 按钮组
        Div btns = new Div();
        btns.addClassName("hero-btns");

        Button joinBtn = new Button("加入服务器", VaadinIcon.SIGN_IN.create());
        joinBtn.addClassName("btn btn-main");
        joinBtn.addClickListener(e -> showJoinGuide());

        Button moreBtn = new Button("了解更多");
        moreBtn.addClassName("btn btn-ghost");
        moreBtn.addClickListener(e -> {
            UI.getCurrent().getPage().executeJs(
                "document.getElementById('info').scrollIntoView({behavior:'smooth'})"
            );
        });

        btns.add(joinBtn, moreBtn);

        content.add(title, desc, tags, btns);

        // 向下滚动提示
        Div scrollTip = new Div();
        scrollTip.addClassName("scroll-tip");
        scrollTip.add(new Span("向下滚动"));
        scrollTip.add(VaadinIcon.ANGLE_DOWN.create());

        hero.add(content, scrollTip);
        return hero;
    }

    private Section buildInfoCards() {
        Section sec = new Section();
        sec.addClassName("info-sec");
        sec.setId("info");

        // 标题
        Div header = new Div();
        header.addClassName("sec-header");
        header.add(new Span("SERVER INFO"));
        header.add(new H2("服务器信息"));
        Div line = new Div();
        line.addClassName("header-line");
        header.add(line);

        sec.add(header);

        // 卡片网格
        Div grid = new Div();
        grid.addClassName("card-grid");

        grid.add(makeInfoCard(VaadinIcon.CLOCK, "游戏版本", VERSION, "支持Java版客户端", "blue"));
        grid.add(makeInfoCard(VaadinIcon.COG, "服务端", CORE_TYPE, "高性能Paper核心", "purple"));
        grid.add(makeInfoCard(VaadinIcon.USERS, "QQ群", QQ_NUM, "点击复制群号", "pink"));
        grid.add(makeInfoCard(VaadinIcon.GLOBE, "状态", "在线", "7×24小时运行", "green"));

        sec.add(grid);
        return sec;
    }

    private Div makeInfoCard(VaadinIcon iconType, String label, String val, String tip, String color) {
        Div card = new Div();
        card.addClassName("info-card");

        Div iconWrap = new Div();
        iconWrap.addClassName("card-icon " + color);
        Icon icon = iconType.create();
        iconWrap.add(icon);

        Paragraph labelP = new Paragraph(label);
        labelP.addClassName("card-label");

        Paragraph valP = new Paragraph(val);
        valP.addClassName("card-val");
        if ("green".equals(color)) {
            valP.getStyle().set("color", "#22c55e");
        }

        Paragraph tipP = new Paragraph(tip);
        tipP.addClassName("card-tip");

        card.add(iconWrap, labelP, valP, tipP);

        // QQ群卡片点击复制
        if (label.contains("QQ")) {
            card.addClassName("clickable");
            card.addClickListener(ev -> copyQQ());
        }

        return card;
    }

    private Section buildNoticeBoard() {
        Section sec = new Section();
        sec.addClassName("notice-sec");
        sec.setId("notice");

        Div header = new Div();
        header.addClassName("sec-header");
        header.add(new Span("ANNOUNCEMENT"));
        header.add(new H2("重要公告"));
        Div line = new Div();
        line.addClassName("header-line");
        header.add(line);

        sec.add(header);

        Div card = new Div();
        card.addClassName("notice-card");

        // 标签
        Span badge = new Span("即将更新");
        badge.addClassName("notice-badge");

        H3 cardTitle = new H3("服务器升级预告");
        cardTitle.addClassName("notice-title");

        Div body = new Div();
        body.addClassName("notice-body");

        // 用图标代替emoji
        Div d1 = new Div();
        d1.addClassName("notice-line");
        Icon calIcon = VaadinIcon.CALENDAR.create();
        calIcon.addClassName("line-icon blue");
        d1.add(calIcon, new Html("<span><b>更新时间：</b>2026.1.19</span>"));

        Div d2 = new Div();
        d2.addClassName("notice-line");
        Icon pkgIcon = VaadinIcon.PACKAGE.create();
        pkgIcon.addClassName("line-icon purple");
        d2.add(pkgIcon, new Html("<span><b>更新内容：</b>升级为<em class='hl'>GTL整合包</em>服务器</span>"));

        Div d3 = new Div();
        d3.addClassName("notice-line");
        Icon starIcon = VaadinIcon.STAR.create();
        starIcon.addClassName("line-icon gold");
        d3.add(starIcon, new Html("<span><b>新特性：</b></span>"));

        UnorderedList ul = new UnorderedList();
        ul.addClassName("feature-list");
        ul.add(new ListItem("GTL科技模组体验"));
        ul.add(new ListItem("更多创造性玩法"));
        ul.add(new ListItem("服务器性能优化"));
        ul.add(new ListItem("丰富游戏内容"));

        body.add(d1, d2, d3, ul);

        // 倒计时
        Div countdown = new Div();
        countdown.addClassName("countdown-box");
        countdown.add(new Html("<p class='cd-label'>距离更新</p>"));
        countdown.add(new Html("<p class='cd-time' id='cdTimer'>--</p>"));

        card.add(badge, cardTitle, body, countdown);
        sec.add(card);

        // 注入倒计时脚本
        UI.getCurrent().getPage().executeJs(
            "window.initCountdown = function() {" +
            "  var target = new Date('2026-01-19T00:00:00').getTime();" +
            "  setInterval(function() {" +
            "    var now = Date.now();" +
            "    var diff = target - now;" +
            "    if (diff <= 0) {" +
            "      document.getElementById('cdTimer').innerText = '已更新';" +
            "      return;" +
            "    }" +
            "    var d = Math.floor(diff / 86400000);" +
            "    var h = Math.floor((diff % 86400000) / 3600000);" +
            "    var m = Math.floor((diff % 3600000) / 60000);" +
            "    var s = Math.floor((diff % 60000) / 1000);" +
            "    document.getElementById('cdTimer').innerText = d + '天 ' + h + '时 ' + m + '分 ' + s + '秒';" +
            "  }, 1000);" +
            "};"
        );

        return sec;
    }

    private Section buildJoinSection() {
        Section sec = new Section();
        sec.addClassName("join-sec");
        sec.setId("join");

        Div header = new Div();
        header.addClassName("sec-header");
        header.add(new Span("JOIN US"));
        header.add(new H2("加入我们"));
        Div line = new Div();
        line.addClassName("header-line");
        header.add(line);

        sec.add(header);

        Div card = new Div();
        card.addClassName("join-card");

        // 二维码
        Div qrBox = new Div();
        qrBox.addClassName("qr-box");
        Image qr = new Image("images/qq-qrcode.jpg", "QQ群二维码");
        qr.addClassName("qr-img");
        qrBox.add(qr);

        // 信息
        Div info = new Div();
        info.addClassName("join-info");

        H3 t = new H3("扫码加入QQ群");
        t.addClassName("join-title");

        Paragraph num = new Paragraph("群号：" + QQ_NUM);
        num.addClassName("qq-num");

        Button copyBtn = new Button("复制群号", VaadinIcon.COPY.create());
        copyBtn.addClassName("btn btn-purple");
        copyBtn.addClickListener(e -> copyQQ());

        info.add(t, num, copyBtn);

        card.add(qrBox, info);
        sec.add(card);

        return sec;
    }

    private Footer buildPageFooter() {
        Footer ft = new Footer();
        ft.addClassName("page-footer");

        Div inner = new Div();
        inner.addClassName("footer-inner");

        Paragraph c1 = new Paragraph("© 2013-2026 Minecraft好人服务器");
        c1.addClassName("copyright");

        Paragraph c2 = new Paragraph("Vaadin + Spring Boot 全Java实现");
        c2.addClassName("tech-note");

        inner.add(c1, c2);
        ft.add(inner);

        return ft;
    }

    private void copyQQ() {
        UI.getCurrent().getPage().executeJs(
            "navigator.clipboard.writeText('" + QQ_NUM + "')"
        );
        Notification n = Notification.show("群号已复制", 2500, Position.TOP_CENTER);
        n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void showJoinGuide() {
        Dialog dlg = new Dialog();
        dlg.addClassName("guide-dialog");
        dlg.setHeaderTitle("加入指南");

        VerticalLayout body = new VerticalLayout();
        body.setPadding(true);
        body.setSpacing(true);

        body.add(new Paragraph("欢迎加入好人服务器！请按以下步骤："));

        OrderedList steps = new OrderedList();
        steps.add(new ListItem("确认Minecraft版本为 " + VERSION));
        steps.add(new ListItem("加入QQ群：" + QQ_NUM));
        steps.add(new ListItem("在群内获取服务器IP"));
        steps.add(new ListItem("添加服务器并进入游戏"));

        Paragraph note = new Paragraph("注：1月19日后需安装GTL整合包");
        note.addClassName("note-text");

        body.add(steps, note);
        dlg.add(body);

        Button closeBtn = new Button("知道了", e -> dlg.close());
        closeBtn.addClassName("btn btn-main");
        dlg.getFooter().add(closeBtn);

        dlg.open();
    }
}
