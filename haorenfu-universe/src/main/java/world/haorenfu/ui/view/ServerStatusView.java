/*
 * ═══════════════════════════════════════════════════════════════════════════
 *                      SERVER STATUS VIEW
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Real-time server status monitoring with player list and statistics.
 */
package world.haorenfu.ui.view;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import world.haorenfu.domain.server.MinecraftServer;
import world.haorenfu.domain.server.ServerStatusService;
import world.haorenfu.ui.layout.MainLayout;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Server status view with real-time updates.
 */
@Route(value = "server", layout = MainLayout.class)
@PageTitle("服务器状态 | 好人服")
@AnonymousAllowed
public class ServerStatusView extends VerticalLayout {

    private final ServerStatusService serverStatusService;
    private final VerticalLayout serverCardsContainer = new VerticalLayout();
    
    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> updateTask;

    public ServerStatusView(ServerStatusService serverStatusService) {
        this.serverStatusService = serverStatusService;

        setWidthFull();
        setMaxWidth("1200px");
        getStyle().set("margin", "0 auto");
        setPadding(true);

        add(
            createHeader(),
            createQuickStats(),
            serverCardsContainer
        );

        loadServerStatus();
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        
        // Set up periodic refresh
        UI ui = attachEvent.getUI();
        scheduler = Executors.newSingleThreadScheduledExecutor();
        updateTask = scheduler.scheduleAtFixedRate(() -> {
            ui.access(this::loadServerStatus);
        }, 30, 30, TimeUnit.SECONDS);
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        
        if (updateTask != null) {
            updateTask.cancel(true);
        }
        if (scheduler != null) {
            scheduler.shutdown();
        }
    }

    private Component createHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);

        Icon icon = VaadinIcon.SERVER.create();
        icon.setSize("32px");
        icon.setColor("#4CAF50");

        H1 title = new H1("服务器状态");
        title.getStyle().set("margin", "0 0 0 12px");

        Span refreshNote = new Span("每30秒自动刷新");
        refreshNote.getStyle()
            .set("color", "#888")
            .set("font-size", "14px")
            .set("margin-left", "auto");

        header.add(icon, title, refreshNote);

        return header;
    }

    private Component createQuickStats() {
        HorizontalLayout stats = new HorizontalLayout();
        stats.setWidthFull();
        stats.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        stats.setSpacing(true);
        stats.getStyle()
            .set("margin", "24px 0")
            .set("flex-wrap", "wrap");

        List<MinecraftServer> servers = serverStatusService.getAllServers();

        int totalOnline = servers.stream()
            .filter(MinecraftServer::isOnline)
            .mapToInt(MinecraftServer::getOnlinePlayers)
            .sum();

        int totalMax = servers.stream()
            .filter(MinecraftServer::isOnline)
            .mapToInt(MinecraftServer::getMaxPlayers)
            .sum();

        long onlineServers = servers.stream()
            .filter(MinecraftServer::isOnline)
            .count();

        double avgLatency = servers.stream()
            .filter(MinecraftServer::isOnline)
            .mapToLong(MinecraftServer::getLatencyMs)
            .average()
            .orElse(0);

        stats.add(createStatCard("在线玩家", String.valueOf(totalOnline), "/" + totalMax, "#4CAF50"));
        stats.add(createStatCard("服务器", onlineServers + "/" + servers.size(), "在线", 
            onlineServers == servers.size() ? "#4CAF50" : "#ff9800"));
        stats.add(createStatCard("平均延迟", String.format("%.0f", avgLatency), "ms", 
            avgLatency < 100 ? "#4CAF50" : avgLatency < 200 ? "#ff9800" : "#f44336"));

        return stats;
    }

    private Component createStatCard(String label, String value, String suffix, String color) {
        VerticalLayout card = new VerticalLayout();
        card.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.CENTER);
        card.setWidth("180px");
        card.getStyle()
            .set("background-color", "var(--lumo-contrast-5pct)")
            .set("padding", "24px")
            .set("border-radius", "12px");

        Span labelSpan = new Span(label);
        labelSpan.getStyle()
            .set("color", "#888")
            .set("font-size", "14px");

        HorizontalLayout valueRow = new HorizontalLayout();
        valueRow.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.BASELINE);
        valueRow.setSpacing(false);

        Span valueSpan = new Span(value);
        valueSpan.getStyle()
            .set("color", color)
            .set("font-size", "32px")
            .set("font-weight", "bold");

        Span suffixSpan = new Span(suffix);
        suffixSpan.getStyle()
            .set("color", "#888")
            .set("font-size", "14px")
            .set("margin-left", "4px");

        valueRow.add(valueSpan, suffixSpan);

        card.add(labelSpan, valueRow);

        return card;
    }

    private void loadServerStatus() {
        serverCardsContainer.removeAll();

        List<MinecraftServer> servers = serverStatusService.getAllServers();

        if (servers.isEmpty()) {
            Paragraph empty = new Paragraph("暂无配置的服务器");
            empty.getStyle()
                .set("color", "#888")
                .set("text-align", "center")
                .set("padding", "40px");
            serverCardsContainer.add(empty);
        } else {
            servers.forEach(server -> serverCardsContainer.add(createServerCard(server)));
        }
    }

    private Component createServerCard(MinecraftServer server) {
        VerticalLayout card = new VerticalLayout();
        card.setWidthFull();
        card.getStyle()
            .set("background-color", "var(--lumo-contrast-5pct)")
            .set("padding", "24px")
            .set("border-radius", "12px")
            .set("border-left", "4px solid " + (server.isOnline() ? "#4CAF50" : "#f44336"));

        // Header
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);

        // Status indicator
        Span statusDot = new Span("●");
        statusDot.getStyle()
            .set("color", server.isOnline() ? "#4CAF50" : "#f44336")
            .set("font-size", "16px");

        H3 serverName = new H3(server.getName());
        serverName.getStyle().set("margin", "0 0 0 8px");

        // Badges
        HorizontalLayout badges = new HorizontalLayout();
        badges.setSpacing(true);
        badges.getStyle().set("margin-left", "auto");

        if (server.isPrimary()) {
            Span primaryBadge = new Span("主服务器");
            primaryBadge.getStyle()
                .set("background-color", "#4CAF50")
                .set("color", "white")
                .set("padding", "2px 8px")
                .set("border-radius", "4px")
                .set("font-size", "12px");
            badges.add(primaryBadge);
        }

        Span typeBadge = new Span(server.getType().getDisplayName());
        typeBadge.getStyle()
            .set("background-color", "var(--lumo-contrast-10pct)")
            .set("padding", "2px 8px")
            .set("border-radius", "4px")
            .set("font-size", "12px");
        badges.add(typeBadge);

        header.add(statusDot, serverName, badges);

        card.add(header);

        if (server.isOnline()) {
            // Server info
            HorizontalLayout info = new HorizontalLayout();
            info.setWidthFull();
            info.setSpacing(true);
            info.getStyle()
                .set("margin-top", "16px")
                .set("flex-wrap", "wrap");

            info.add(createInfoItem("地址", server.getAddress(), VaadinIcon.LINK));
            info.add(createInfoItem("版本", server.getVersion(), VaadinIcon.CODE));
            info.add(createInfoItem("延迟", server.getLatencyMs() + " ms", VaadinIcon.TIMER));

            card.add(info);

            // Player count bar
            VerticalLayout playerSection = new VerticalLayout();
            playerSection.setSpacing(false);
            playerSection.setPadding(false);
            playerSection.getStyle().set("margin-top", "16px");

            HorizontalLayout playerHeader = new HorizontalLayout();
            playerHeader.setWidthFull();
            playerHeader.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

            Span playerLabel = new Span("在线玩家");
            playerLabel.getStyle().set("color", "#888");

            Span playerCount = new Span(server.getOnlinePlayers() + " / " + server.getMaxPlayers());
            playerCount.getStyle()
                .set("font-weight", "bold")
                .set("color", "#4CAF50");

            playerHeader.add(playerLabel, playerCount);

            ProgressBar playerBar = new ProgressBar();
            playerBar.setWidthFull();
            playerBar.setValue((double) server.getOnlinePlayers() / server.getMaxPlayers());
            playerBar.getStyle()
                .set("--lumo-primary-color", "#4CAF50")
                .set("margin-top", "8px");

            playerSection.add(playerHeader, playerBar);
            card.add(playerSection);

            // Player list
            if (!server.getPlayerList().isEmpty()) {
                VerticalLayout playerList = new VerticalLayout();
                playerList.setSpacing(false);
                playerList.setPadding(false);
                playerList.getStyle().set("margin-top", "16px");

                Span listLabel = new Span("在线玩家列表:");
                listLabel.getStyle()
                    .set("color", "#888")
                    .set("font-size", "14px");

                HorizontalLayout players = new HorizontalLayout();
                players.setSpacing(true);
                players.getStyle()
                    .set("flex-wrap", "wrap")
                    .set("margin-top", "8px");

                server.getPlayerList().forEach(playerName -> {
                    Span playerTag = new Span(playerName);
                    playerTag.getStyle()
                        .set("background-color", "var(--lumo-contrast-10pct)")
                        .set("padding", "4px 12px")
                        .set("border-radius", "16px")
                        .set("font-size", "14px");
                    players.add(playerTag);
                });

                playerList.add(listLabel, players);
                card.add(playerList);
            }

            // MOTD
            if (server.getMotd() != null && !server.getMotd().isBlank()) {
                Paragraph motd = new Paragraph(server.getMotd());
                motd.getStyle()
                    .set("color", "#888")
                    .set("font-style", "italic")
                    .set("margin-top", "16px")
                    .set("padding", "12px")
                    .set("background-color", "var(--lumo-contrast-5pct)")
                    .set("border-radius", "8px");
                card.add(motd);
            }
        } else {
            // Offline message
            Paragraph offlineMsg = new Paragraph("服务器当前离线");
            offlineMsg.getStyle()
                .set("color", "#f44336")
                .set("margin-top", "16px");

            if (server.getLastOnline() != null) {
                Duration downtime = Duration.between(server.getLastOnline(), Instant.now());
                Span lastOnline = new Span("上次在线: " + formatDuration(downtime) + "前");
                lastOnline.getStyle()
                    .set("color", "#888")
                    .set("font-size", "14px")
                    .set("display", "block");
                card.add(offlineMsg, lastOnline);
            } else {
                card.add(offlineMsg);
            }
        }

        return card;
    }

    private Component createInfoItem(String label, String value, VaadinIcon iconType) {
        HorizontalLayout item = new HorizontalLayout();
        item.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        item.setSpacing(true);

        Icon icon = iconType.create();
        icon.setSize("16px");
        icon.setColor("#888");

        Span labelSpan = new Span(label + ":");
        labelSpan.getStyle()
            .set("color", "#888")
            .set("font-size", "14px");

        Span valueSpan = new Span(value);
        valueSpan.getStyle()
            .set("font-size", "14px")
            .set("font-family", "monospace");

        item.add(icon, labelSpan, valueSpan);

        return item;
    }

    private String formatDuration(Duration duration) {
        if (duration.toMinutes() < 1) return "不到1分钟";
        if (duration.toMinutes() < 60) return duration.toMinutes() + "分钟";
        if (duration.toHours() < 24) return duration.toHours() + "小时";
        return duration.toDays() + "天";
    }
}
