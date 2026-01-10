/*
 * ═══════════════════════════════════════════════════════════════════════════
 *                         CUSTOM ERROR VIEW
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Custom error handling view for Vaadin routing errors.
 */
package world.haorenfu.ui.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Custom error view for handling routing exceptions.
 */
@ParentLayout(world.haorenfu.ui.layout.MainLayout.class)
@AnonymousAllowed
public class ErrorView extends VerticalLayout implements HasErrorParameter<Exception> {

    private H1 errorTitle;
    private Paragraph errorMessage;

    public ErrorView() {
        setSizeFull();
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        add(createErrorContent());
    }

    private Component createErrorContent() {
        VerticalLayout content = new VerticalLayout();
        content.setAlignItems(FlexComponent.Alignment.CENTER);
        content.setMaxWidth("500px");

        // Error icon
        Span icon = new Span("⚠️");
        icon.getElement().getStyle()
            .set("font-size", "64px")
            .set("margin-bottom", "16px");

        // Error title
        errorTitle = new H1("出错了");
        errorTitle.getElement().getStyle()
            .set("color", "var(--lumo-error-color)")
            .set("margin", "0");

        // Error message
        errorMessage = new Paragraph("抱歉，发生了一个错误。请稍后再试。");
        errorMessage.getElement().getStyle()
            .set("color", "var(--lumo-secondary-text-color)")
            .set("text-align", "center");

        // Home button
        Button homeBtn = new Button("返回首页", e -> 
            getUI().ifPresent(ui -> ui.navigate("")));
        homeBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        // Back button
        Button backBtn = new Button("返回上一页", e -> 
            getUI().ifPresent(ui -> ui.getPage().getHistory().back()));
        backBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        content.add(icon, errorTitle, errorMessage, homeBtn, backBtn);
        return content;
    }

    @Override
    public int setErrorParameter(BeforeEnterEvent event, 
                                  ErrorParameter<Exception> parameter) {
        
        Exception exception = parameter.getException();
        
        if (exception instanceof NotFoundException) {
            errorTitle.setText("页面不存在");
            errorMessage.setText("您访问的页面不存在或已被删除。");
            return HttpServletResponse.SC_NOT_FOUND;
        }
        
        if (exception instanceof AccessDeniedException || 
            exception.getMessage() != null && 
            exception.getMessage().contains("Access denied")) {
            errorTitle.setText("访问被拒绝");
            errorMessage.setText("您没有权限访问此页面。请登录或联系管理员。");
            return HttpServletResponse.SC_FORBIDDEN;
        }

        // Generic error
        errorTitle.setText("服务器错误");
        errorMessage.setText("服务器遇到了一个问题，请稍后再试。如果问题持续存在，请联系管理员。");
        
        return HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
    }
}
