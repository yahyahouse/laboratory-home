package com.yahya.view.login;

import com.yahya.model.Users;
import com.yahya.service.JWTService;
import com.yahya.service.UsersService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import lombok.extern.slf4j.Slf4j;


@Route(value = "/login", autoLayout = false)
@Slf4j
public class LoginView extends VerticalLayout implements BeforeEnterObserver {
    private final UsersService usersService;
    private final JWTService jwtUtil;

    public LoginView(UsersService usersService, JWTService jwtUtil) {
        this.usersService = usersService;
        this.jwtUtil = jwtUtil;
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        addClassName("auth-shell");

        Div card = new Div();
        card.addClassNames("auth-card", "login-card");

        H1 title = new H1("Laboratory Home");
        title.addClassName("auth-title");
        Paragraph subtitle = new Paragraph("Masuk untuk mengelola dan memantau data laboratorium Anda.");
        subtitle.addClassName("muted");
        LoginForm loginForm = new LoginForm();
        loginForm.setI18n(createLoginI18n());
        loginForm.setForgotPasswordButtonVisible(false);
        loginForm.addLoginListener(event -> {
            String email = event.getUsername();
            String password = event.getPassword();
            Users user = usersService.findByEmailAndPassword(email, password);

            if (user != null) {
                String token = jwtUtil.generateToken(user);
                VaadinSession.getCurrent().setAttribute("user", user.getEmail());
                VaadinSession.getCurrent().setAttribute("jwt", token);
                Notification.show("Login successful!", 2000, Notification.Position.BOTTOM_CENTER);
                loginForm.getUI().ifPresent(ui -> ui.navigate("")); // Redirect to chat (root)
            } else {
                loginForm.setError(true);
            }
        });
        Button registerButton = new Button("Buat akun");
        registerButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        registerButton.addClickListener(event -> getUI().ifPresent(ui -> ui.navigate("register")));

        card.add(title, subtitle, loginForm, registerButton);
        add(card);
    }

    private LoginI18n createLoginI18n() {
        LoginI18n i18n = LoginI18n.createDefault();
        i18n.setHeader(new LoginI18n.Header());
        i18n.getHeader().setTitle("Vaadin App");
        i18n.getHeader().setDescription("Login with your credentials.");
        i18n.getForm().setUsername("Email");
        i18n.getForm().setPassword("Password");
        i18n.getForm().setSubmit("Login");
        i18n.getForm().setForgotPassword("Forgot password?");
        i18n.getErrorMessage().setTitle("Invalid credentials");
        i18n.getErrorMessage().setMessage("Check your username and password and try again.");
        return i18n;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        String token = (String) VaadinSession.getCurrent().getAttribute("jwt");
        String email = (String) VaadinSession.getCurrent().getAttribute("user");

        if (token == null || email == null) {
            beforeEnterEvent.forwardTo("login");
            return;
        }

        Users user = usersService.findByEmail(email);

        try {
            if (user == null || !jwtUtil.isTokenValid(token, user)) {
                beforeEnterEvent.forwardTo("login");
            }
        } catch (IllegalStateException e) {
            // Handle token expiration gracefully
            VaadinSession.getCurrent().setAttribute("jwt", null);
            VaadinSession.getCurrent().setAttribute("user", null);
            beforeEnterEvent.forwardTo("login");
        }
    }

}
