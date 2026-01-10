/*
 * ═══════════════════════════════════════════════════════════════════════════
 *                          REGISTER VIEW
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * User registration with real-time password strength feedback
 * powered by Shannon entropy analysis.
 */
package world.haorenfu.ui.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import world.haorenfu.core.algorithm.EntropyAnalyzer;
import world.haorenfu.domain.user.UserService;

/**
 * Registration view with entropy-based password strength indicator.
 */
@Route("register")
@PageTitle("注册 | 好人服")
@AnonymousAllowed
public class RegisterView extends VerticalLayout {

    private final UserService userService;

    // Form fields
    private final TextField usernameField = new TextField("用户名");
    private final EmailField emailField = new EmailField("邮箱");
    private final PasswordField passwordField = new PasswordField("密码");
    private final PasswordField confirmPasswordField = new PasswordField("确认密码");
    private final TextField minecraftIdField = new TextField("Minecraft ID (可选)");

    // Password strength indicator
    private final ProgressBar strengthBar = new ProgressBar();
    private final Span strengthLabel = new Span();
    private final Span strengthFeedback = new Span();

    public RegisterView(UserService userService) {
        this.userService = userService;

        setSizeFull();
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        getStyle()
            .set("background", "linear-gradient(135deg, #1a1a2e 0%, #16213e 100%)")
            .set("padding", "20px");

        add(createRegistrationForm());
    }

    private VerticalLayout createRegistrationForm() {
        VerticalLayout container = new VerticalLayout();
        container.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.CENTER);
        container.setWidth("400px");
        container.setMaxWidth("100%");
        container.getStyle()
            .set("background-color", "#1a1a2e")
            .set("padding", "40px")
            .set("border-radius", "12px")
            .set("box-shadow", "0 4px 20px rgba(0,0,0,0.3)");

        // Header
        Span logo = new Span("⛏️");
        logo.getStyle().set("font-size", "48px");

        H1 title = new H1("加入好人服");
        title.getStyle()
            .set("color", "#4CAF50")
            .set("margin", "8px 0")
            .set("font-size", "1.8rem");

        Paragraph subtitle = new Paragraph("创建账号，开启你的冒险之旅");
        subtitle.getStyle().set("color", "#888");

        // Form
        FormLayout formLayout = new FormLayout();
        formLayout.setWidthFull();

        // Configure fields
        usernameField.setPlaceholder("3-20个字符");
        usernameField.setRequired(true);
        usernameField.setMinLength(3);
        usernameField.setMaxLength(20);
        usernameField.setHelperText("支持中文、字母、数字和下划线");

        emailField.setPlaceholder("your@email.com");
        emailField.setRequired(true);
        emailField.setHelperText("用于账号验证和找回密码");

        passwordField.setRequired(true);
        passwordField.setMinLength(6);
        passwordField.setHelperText("至少6个字符");

        confirmPasswordField.setRequired(true);

        minecraftIdField.setPlaceholder("你的正版ID");
        minecraftIdField.setMaxLength(16);
        minecraftIdField.setHelperText("用于白名单绑定");

        // Password strength indicator
        setupPasswordStrengthIndicator();

        formLayout.add(
            usernameField,
            emailField,
            passwordField,
            createPasswordStrengthSection(),
            confirmPasswordField,
            minecraftIdField
        );

        // Submit button
        Button submitBtn = new Button("创建账号");
        submitBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        submitBtn.setWidthFull();
        submitBtn.getStyle()
            .set("background-color", "#4CAF50")
            .set("margin-top", "16px");
        submitBtn.addClickListener(e -> handleRegistration());

        // Login link
        HorizontalLayout loginSection = new HorizontalLayout();
        loginSection.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);

        Span loginText = new Span("已有账号？");
        loginText.getStyle().set("color", "#888");

        RouterLink loginLink = new RouterLink("立即登录", LoginView.class);
        loginLink.getStyle().set("color", "#4CAF50");

        loginSection.add(loginText, loginLink);

        container.add(logo, title, subtitle, formLayout, submitBtn, loginSection);

        return container;
    }

    private void setupPasswordStrengthIndicator() {
        passwordField.addValueChangeListener(e -> {
            String password = e.getValue();
            updatePasswordStrength(password);
        });
    }

    private VerticalLayout createPasswordStrengthSection() {
        VerticalLayout section = new VerticalLayout();
        section.setSpacing(false);
        section.setPadding(false);

        HorizontalLayout strengthHeader = new HorizontalLayout();
        strengthHeader.setWidthFull();
        strengthHeader.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        Span label = new Span("密码强度：");
        label.getStyle()
            .set("font-size", "12px")
            .set("color", "#888");

        strengthLabel.getStyle()
            .set("font-size", "12px")
            .set("font-weight", "bold");

        strengthHeader.add(label, strengthLabel);

        strengthBar.setWidth("100%");
        strengthBar.setHeight("4px");
        strengthBar.setValue(0);

        strengthFeedback.getStyle()
            .set("font-size", "11px")
            .set("color", "#888")
            .set("margin-top", "4px");

        section.add(strengthHeader, strengthBar, strengthFeedback);

        return section;
    }

    private void updatePasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            strengthBar.setValue(0);
            strengthLabel.setText("");
            strengthFeedback.setText("");
            return;
        }

        EntropyAnalyzer.PasswordStrength strength = EntropyAnalyzer.analyzePassword(password);

        double percentage = strength.getPercentage() / 100.0;
        strengthBar.setValue(Math.min(percentage, 1.0));

        strengthLabel.setText(strength.level().getLabel());

        // Color based on strength
        String color = switch (strength.level()) {
            case WEAK -> "#f44336";
            case FAIR -> "#ff9800";
            case GOOD -> "#ffc107";
            case STRONG -> "#8bc34a";
            case EXCELLENT -> "#4caf50";
            default -> "#888";
        };

        strengthLabel.getStyle().set("color", color);
        strengthBar.getStyle().set("--lumo-primary-color", color);

        strengthFeedback.setText(strength.feedback());
    }

    private void handleRegistration() {
        // Validate passwords match
        if (!passwordField.getValue().equals(confirmPasswordField.getValue())) {
            Notification.show("两次输入的密码不一致", 3000, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        // Validate password strength
        EntropyAnalyzer.PasswordStrength strength =
            EntropyAnalyzer.analyzePassword(passwordField.getValue());

        if (strength.level().getScore() < EntropyAnalyzer.StrengthLevel.FAIR.getScore()) {
            Notification.show("密码强度不足，请设置更复杂的密码", 3000, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        try {
            UserService.RegistrationRequest request = new UserService.RegistrationRequest(
                usernameField.getValue(),
                emailField.getValue(),
                passwordField.getValue(),
                minecraftIdField.getValue().isBlank() ? null : minecraftIdField.getValue()
            );

            userService.register(request);

            Notification.show("注册成功！请查收验证邮件", 5000, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            getUI().ifPresent(ui -> ui.navigate(LoginView.class));

        } catch (UserService.UserRegistrationException e) {
            Notification.show(e.getMessage(), 3000, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}
