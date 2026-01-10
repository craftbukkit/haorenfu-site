/*
 * ═══════════════════════════════════════════════════════════════════════════
 *                           LOGIN VIEW
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * User authentication view with a clean, focused design.
 */
package world.haorenfu.ui.view;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.auth.AnonymousAllowed;

/**
 * Login view.
 */
@Route("login")
@PageTitle("登录 | 好人服")
@AnonymousAllowed
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private final LoginForm loginForm;

    public LoginView() {
        setSizeFull();
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        getStyle()
            .set("background", "linear-gradient(135deg, #1a1a2e 0%, #16213e 100%)")
            .set("padding", "20px");

        // Logo
        Span logo = new Span("⛏️");
        logo.getStyle().set("font-size", "48px");

        H1 title = new H1("欢迎回来");
        title.getStyle()
            .set("color", "#4CAF50")
            .set("margin", "8px 0");

        Paragraph subtitle = new Paragraph("登录到好人服宇宙");
        subtitle.getStyle().set("color", "#888");

        // Login form with Chinese localization
        loginForm = new LoginForm();
        loginForm.setI18n(createChineseI18n());
        loginForm.setAction("login");
        loginForm.setForgotPasswordButtonVisible(true);

        loginForm.addForgotPasswordListener(e -> {
            // Navigate to password reset
            getUI().ifPresent(ui -> ui.navigate("forgot-password"));
        });

        // Style the form
        loginForm.getElement().getStyle()
            .set("background-color", "#1a1a2e")
            .set("border-radius", "12px")
            .set("padding", "20px")
            .set("box-shadow", "0 4px 20px rgba(0,0,0,0.3)");

        // Register link
        RouterLink registerLink = new RouterLink("还没有账号？立即注册", RegisterView.class);
        registerLink.getStyle()
            .set("color", "#4CAF50")
            .set("margin-top", "16px");

        add(logo, title, subtitle, loginForm, registerLink);
    }

    private LoginI18n createChineseI18n() {
        LoginI18n i18n = LoginI18n.createDefault();

        LoginI18n.Form form = i18n.getForm();
        form.setTitle("登录");
        form.setUsername("用户名或邮箱");
        form.setPassword("密码");
        form.setSubmit("登录");
        form.setForgotPassword("忘记密码？");

        LoginI18n.ErrorMessage errorMessage = i18n.getErrorMessage();
        errorMessage.setTitle("登录失败");
        errorMessage.setMessage("请检查用户名和密码是否正确");

        i18n.setForm(form);
        i18n.setErrorMessage(errorMessage);

        return i18n;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (event.getLocation()
                .getQueryParameters()
                .getParameters()
                .containsKey("error")) {
            loginForm.setError(true);
        }
    }
}
