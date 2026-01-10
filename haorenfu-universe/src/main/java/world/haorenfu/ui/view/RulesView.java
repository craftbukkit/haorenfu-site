/*
 * ═══════════════════════════════════════════════════════════════════════════
 *                           RULES VIEW
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Server rules and guidelines for the community.
 */
package world.haorenfu.ui.view;

import com.vaadin.flow.component.Component;
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
 * Server rules view.
 */
@Route(value = "rules", layout = MainLayout.class)
@PageTitle("服务器规则 | 好人服")
@AnonymousAllowed
public class RulesView extends VerticalLayout {

    public RulesView() {
        setWidthFull();
        setMaxWidth("900px");
        getStyle().set("margin", "0 auto");
        setPadding(true);

        add(
            createHeader(),
            createIntroduction(),
            createRulesSection(),
            createPunishmentsSection(),
            createFooterNote()
        );
    }

    private Component createHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);

        Icon icon = VaadinIcon.FILE_TEXT.create();
        icon.setSize("32px");
        icon.setColor("#4CAF50");

        H1 title = new H1("服务器规则");
        title.getStyle().set("margin", "0 0 0 12px");

        header.add(icon, title);

        return header;
    }

    private Component createIntroduction() {
        Div intro = new Div();
        intro.getStyle()
            .set("background-color", "var(--lumo-contrast-5pct)")
            .set("padding", "20px")
            .set("border-radius", "8px")
            .set("margin", "20px 0")
            .set("border-left", "4px solid #4CAF50");

        Paragraph text = new Paragraph(
            "为了维护一个和谐、友善、互助的游戏社区，请所有玩家自觉遵守以下规则。" +
            "这些规则的目的是确保每位玩家都能享受游戏的乐趣。"
        );
        text.getStyle().set("margin", "0").set("color", "var(--lumo-body-text-color)");

        intro.add(text);

        return intro;
    }

    private Component createRulesSection() {
        VerticalLayout section = new VerticalLayout();
        section.setSpacing(true);
        section.setPadding(false);

        section.add(createRuleCard(
            "1", "互相尊重",
            "禁止任何形式的人身攻击、歧视、骚扰或引战行为。请友善对待每一位玩家，" +
            "尊重不同的观点和游戏风格。",
            VaadinIcon.HEART
        ));

        section.add(createRuleCard(
            "2", "禁止作弊",
            "严禁使用任何作弊客户端、模组或外挂（如飞行、透视、加速等）来获取不公平的优势。" +
            "小地图、光影等非作弊性质的模组可以使用。",
            VaadinIcon.BAN
        ));

        section.add(createRuleCard(
            "3", "保护环境",
            "请勿肆意破坏服务器的地形和自然景观。合理规划您的建筑，" +
            "避免在公共区域进行大规模改造。废弃的建筑请自行清理。",
            VaadinIcon.TREE
        ));

        section.add(createRuleCard(
            "4", "财产安全",
            "禁止偷窃、抢劫或破坏他人财产。他人的箱子、建筑和农田都归其所有者，" +
            "未经许可请勿触碰。发现无主建筑请联系管理员确认。",
            VaadinIcon.SAFE_LOCK
        ));

        section.add(createRuleCard(
            "5", "公共设施",
            "欢迎使用服务器的公共设施（如公共熔炉、农场、交通系统），" +
            "但请在使用后及时补充资源，保持整洁。损坏的公共设施请及时报告。",
            VaadinIcon.HOME
        ));

        section.add(createRuleCard(
            "6", "红石与养殖",
            "在建造高频红石电路或大型自动化养殖场之前，请先咨询服主或管理员，" +
            "以避免造成服务器卡顿。单个区块内的实体数量不应超过100。",
            VaadinIcon.COG
        ));

        section.add(createRuleCard(
            "7", "和谐沟通",
            "如果与其他玩家产生矛盾，请尝试友好沟通解决。" +
            "如果无法解决，请联系服主或管理员介入，不要私下报复或恶意举报。",
            VaadinIcon.COMMENTS
        ));

        section.add(createRuleCard(
            "8", "账号安全",
            "请妥善保管你的账号信息，不要与他人共享账号。" +
            "因账号共享导致的任何问题由账号所有者承担责任。",
            VaadinIcon.KEY
        ));

        return section;
    }

    private Component createRuleCard(String number, String title, String description, VaadinIcon iconType) {
        HorizontalLayout card = new HorizontalLayout();
        card.setWidthFull();
        card.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.START);
        card.getStyle()
            .set("background-color", "var(--lumo-contrast-5pct)")
            .set("padding", "20px")
            .set("border-radius", "8px");

        // Number badge
        Div numberBadge = new Div();
        numberBadge.setText(number);
        numberBadge.getStyle()
            .set("background-color", "#4CAF50")
            .set("color", "white")
            .set("width", "32px")
            .set("height", "32px")
            .set("border-radius", "50%")
            .set("display", "flex")
            .set("align-items", "center")
            .set("justify-content", "center")
            .set("font-weight", "bold")
            .set("flex-shrink", "0");

        // Content
        VerticalLayout content = new VerticalLayout();
        content.setSpacing(false);
        content.setPadding(false);

        HorizontalLayout titleRow = new HorizontalLayout();
        titleRow.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);

        Icon icon = iconType.create();
        icon.setSize("20px");
        icon.setColor("#4CAF50");

        H3 ruleTitle = new H3(title);
        ruleTitle.getStyle()
            .set("margin", "0 0 0 8px")
            .set("font-size", "18px");

        titleRow.add(icon, ruleTitle);

        Paragraph desc = new Paragraph(description);
        desc.getStyle()
            .set("color", "var(--lumo-secondary-text-color)")
            .set("margin", "8px 0 0 0")
            .set("line-height", "1.6");

        content.add(titleRow, desc);

        card.add(numberBadge, content);
        card.setFlexGrow(1, content);

        return card;
    }

    private Component createPunishmentsSection() {
        VerticalLayout section = new VerticalLayout();
        section.setSpacing(true);
        section.setPadding(false);
        section.getStyle().set("margin-top", "32px");

        H2 title = new H2("违规处罚");
        title.getStyle().set("margin-bottom", "16px");

        Div table = new Div();
        table.getStyle()
            .set("background-color", "var(--lumo-contrast-5pct)")
            .set("padding", "20px")
            .set("border-radius", "8px");

        VerticalLayout tableContent = new VerticalLayout();
        tableContent.setSpacing(true);
        tableContent.setPadding(false);

        tableContent.add(createPunishmentRow("轻微违规", "口头警告，记录在案", "#ffc107"));
        tableContent.add(createPunishmentRow("一般违规", "临时封禁 1-7 天", "#ff9800"));
        tableContent.add(createPunishmentRow("严重违规", "临时封禁 7-30 天", "#ff5722"));
        tableContent.add(createPunishmentRow("极其严重", "永久封禁", "#f44336"));

        table.add(tableContent);
        section.add(title, table);

        return section;
    }

    private Component createPunishmentRow(String level, String punishment, String color) {
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        row.getStyle()
            .set("padding", "12px")
            .set("border-left", "4px solid " + color)
            .set("background-color", "var(--lumo-contrast-5pct)")
            .set("border-radius", "0 4px 4px 0");

        Span levelSpan = new Span(level);
        levelSpan.getStyle()
            .set("font-weight", "bold")
            .set("width", "120px")
            .set("color", color);

        Span punishmentSpan = new Span(punishment);
        punishmentSpan.getStyle().set("color", "var(--lumo-secondary-text-color)");

        row.add(levelSpan, punishmentSpan);

        return row;
    }

    private Component createFooterNote() {
        Div footer = new Div();
        footer.getStyle()
            .set("margin-top", "32px")
            .set("padding", "20px")
            .set("background-color", "var(--lumo-contrast-5pct)")
            .set("border-radius", "8px")
            .set("text-align", "center");

        Paragraph note = new Paragraph(
            "以上规则的最终解释权归服主所有。规则可能会根据实际情况进行调整，" +
            "请定期查看本页面了解最新规则。感谢你的配合，希望大家共同营造一个美好的游戏环境！"
        );
        note.getStyle()
            .set("margin", "0")
            .set("color", "var(--lumo-secondary-text-color)")
            .set("font-size", "14px");

        footer.add(note);

        return footer;
    }
}
