package com.yahya.view.registration;

import com.yahya.model.Users;
import com.yahya.service.JWTService;
import com.yahya.service.UsersService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

@Route(value = "register", autoLayout = false)
public class RegistrationView extends VerticalLayout implements BeforeEnterObserver {

    private final UsersService usersService;
    private final JWTService jwtService;

    private final TextField nameField = new TextField("Nama");
    private final EmailField emailField = new EmailField("Email");
    private final PasswordField passwordField = new PasswordField("Password");
    private final PasswordField confirmPasswordField = new PasswordField("Konfirmasi Password");

    public RegistrationView(UsersService usersService, JWTService jwtService) {
        this.usersService = usersService;
        this.jwtService = jwtService;

        setSizeFull();
        setPadding(false);
        setSpacing(false);
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        addClassName("auth-shell");

        Div card = new Div();
        card.addClassNames("auth-card", "login-card");

        H1 title = new H1("Buat Akun");
        title.addClassName("auth-title");
        Paragraph subtitle = new Paragraph("Daftarkan email untuk mulai chat di Laboratorium Home.");
        subtitle.addClassName("muted");

        nameField.setRequired(true);
        nameField.setWidthFull();
        emailField.setRequired(true);
        emailField.setWidthFull();
        passwordField.setRequired(true);
        passwordField.setWidthFull();
        confirmPasswordField.setRequired(true);
        confirmPasswordField.setWidthFull();

        Button submit = new Button("Daftar");
        submit.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        submit.setWidthFull();
        submit.addClickListener(e -> register());

        Button toLogin = new Button("Sudah punya akun? Masuk");
        toLogin.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        toLogin.addClickListener(e -> UI.getCurrent().navigate("login"));

        VerticalLayout form = new VerticalLayout(nameField, emailField, passwordField, confirmPasswordField, submit, toLogin);
        form.setPadding(false);
        form.setSpacing(true);
        form.setWidthFull();
        form.setAlignItems(Alignment.STRETCH);

        card.add(title, subtitle, form);
        add(card);
    }

    private void register() {
        if (nameField.isEmpty() || emailField.isEmpty() || passwordField.isEmpty() || confirmPasswordField.isEmpty()) {
            Notification.show("Semua kolom wajib diisi", 3000, Notification.Position.BOTTOM_CENTER);
            return;
        }
        if (!passwordField.getValue().equals(confirmPasswordField.getValue())) {
            Notification.show("Password tidak sama", 3000, Notification.Position.BOTTOM_CENTER);
            return;
        }

        try {
            Users newUser = new Users();
            newUser.setName(nameField.getValue());
            newUser.setEmail(emailField.getValue());
            newUser.setPassword(passwordField.getValue());
            newUser.setRole("USER");
            newUser.setMessage("");
            newUser.setPictureUrl("https://randomuser.me/api/portraits/men/42.jpg");
            usersService.saveUsers(newUser);

            Users saved = usersService.findByEmail(newUser.getEmail());
            if (saved != null) {
                String token = jwtService.generateToken(saved);
                VaadinSession.getCurrent().setAttribute("user", saved.getEmail());
                VaadinSession.getCurrent().setAttribute("jwt", token);
            }
            Notification.show("Akun berhasil dibuat", 2500, Notification.Position.BOTTOM_CENTER);
            UI.getCurrent().navigate("");
        } catch (Exception ex) {
            Notification.show("Gagal membuat akun: " + ex.getMessage(), 3000, Notification.Position.BOTTOM_CENTER);
        }
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        String token = (String) VaadinSession.getCurrent().getAttribute("jwt");
        String email = (String) VaadinSession.getCurrent().getAttribute("user");
        if (token != null && email != null) {
            event.forwardTo("");
        }
    }
}
