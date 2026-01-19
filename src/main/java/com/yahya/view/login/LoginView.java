package com.yahya.view.login;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.yahya.model.Users;
import com.yahya.service.JWTService;
import com.yahya.service.LoginLoggingService;
import com.yahya.service.UsersService;

@Route(value = "/login", autoLayout = false)
public class LoginView extends Div implements BeforeEnterObserver {
    private final UsersService usersService;
    private final JWTService jwtUtil;
    private final LoginLoggingService loginLoggingService;

    public LoginView(UsersService usersService, JWTService jwtUtil, LoginLoggingService loginLoggingService) {
        this.usersService = usersService;
        this.jwtUtil = jwtUtil;
        this.loginLoggingService = loginLoggingService;

        getStyle()
                .set("background-color", "var(--lumo-contrast-5pct)")
                .set("display", "flex")
                .set("justify-content", "center")
                .set("align-items", "center")
                .set("min-height", "100vh")
                .set("padding", "var(--lumo-space-l)");

        Div card = new Div();
        card.getStyle()
                .set("padding", "var(--lumo-space-l)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("background", "var(--lumo-base-color)")
                .set("box-shadow", "0 4px 20px rgba(0,0,0,0.08)")
                .set("width", "360px")
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("gap", "var(--lumo-space-m)");

        H1 title = new H1("Masuk");
        title.getStyle().set("margin", "0");

        LoginForm loginForm = new LoginForm();
        loginForm.setI18n(createLoginI18n());
        loginForm.setForgotPasswordButtonVisible(false);
        loginForm.getElement().setAttribute("no-autofocus", "");
        loginForm.addLoginListener(event -> {
            String email = event.getUsername();
            String password = event.getPassword();
            Users user = usersService.findByEmailAndPassword(email, password);

            if (user != null) {
                String token = jwtUtil.generateToken(user);
                VaadinSession.getCurrent().setAttribute("user", user.getEmail());
                VaadinSession.getCurrent().setAttribute("jwt", token);
                loginLoggingService.logLoginAttempt(email, true);
                loginForm.getUI().ifPresent(ui -> ui.navigate("")); // Redirect to chat (root)
            } else {
                loginLoggingService.logLoginAttempt(email, false);
                loginForm.setError(true);
            }
        });

        Button registerButton = new Button("Buat akun");
        registerButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        registerButton.addClickListener(event -> getUI().ifPresent(ui -> ui.navigate("register")));

        card.add(title, loginForm, registerButton);
        add(card);
    }

    private LoginI18n createLoginI18n() {
        LoginI18n i18n = LoginI18n.createDefault();
        i18n.setHeader(new LoginI18n.Header());
        i18n.getHeader().setTitle("Laboratory Home");
        i18n.getHeader().setDescription("Masuk dengan email dan kata sandi.");
        i18n.getForm().setUsername("Email");
        i18n.getForm().setPassword("Password");
        i18n.getForm().setSubmit("Login");
        i18n.getForm().setForgotPassword("Lupa kata sandi?");
        i18n.getErrorMessage().setTitle("Email atau password salah");
        i18n.getErrorMessage().setMessage("Periksa kembali email dan password Anda.");
        return i18n;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        String token = (String) VaadinSession.getCurrent().getAttribute("jwt");
        String email = (String) VaadinSession.getCurrent().getAttribute("user");

        if (token == null || email == null) {
            return;
        }

        Users user = usersService.findByEmail(email);

        try {
            if (user == null || !jwtUtil.isTokenValid(token, user)) {
                VaadinSession.getCurrent().setAttribute("jwt", null);
                VaadinSession.getCurrent().setAttribute("user", null);
            } else {
                beforeEnterEvent.forwardTo("");
            }
        } catch (IllegalStateException e) {
            VaadinSession.getCurrent().setAttribute("jwt", null);
            VaadinSession.getCurrent().setAttribute("user", null);
        }
    }
}
