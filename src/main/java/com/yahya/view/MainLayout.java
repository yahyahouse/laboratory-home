package com.yahya.view;

import com.yahya.model.Users;
import com.yahya.service.JWTService;
import com.yahya.service.UsersService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.menu.MenuConfiguration;
import com.vaadin.flow.server.menu.MenuEntry;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.util.List;


@Layout
public class MainLayout extends AppLayout implements BeforeEnterObserver, AfterNavigationObserver{


    private final UsersService usersService;

    private final JWTService jwtUtil;
    private H1 viewTitle;

    public MainLayout(UsersService usersService, JWTService jwtUtil) {
        this.usersService = usersService;
        this.jwtUtil = jwtUtil;
        addClassName("app-shell");
        setPrimarySection(Section.DRAWER);
        addDrawerContent();
        addHeaderContent();
    }

    private void addHeaderContent() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.setAriaLabel("Menu toggle");
        toggle.addClassName("app-drawer-toggle");

        viewTitle = new H1();
        viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);
        viewTitle.addClassName("app-view-title");

        HorizontalLayout headerBar = new HorizontalLayout(toggle, viewTitle);
        headerBar.setAlignItems(FlexComponent.Alignment.CENTER);
        headerBar.setPadding(true);
        headerBar.setSpacing(true);
        headerBar.setWidthFull();
        headerBar.addClassName("app-header");

        addToNavbar(true, headerBar);
    }

    private void addDrawerContent() {
        Span appName = new Span("Laboratory Home");
        appName.addClassNames(LumoUtility.FontWeight.SEMIBOLD, LumoUtility.FontSize.LARGE);
        appName.addClassName("app-name");
        Header header = new Header(appName);
        header.addClassName("drawer-header");

        Scroller scroller = new Scroller(createNavigation());
        scroller.addClassName("drawer-scroller");

        addToDrawer(header, scroller, createFooter());
    }

    private SideNav createNavigation() {
        SideNav nav = new SideNav();
        nav.addClassName("drawer-nav");

        List<MenuEntry> menuEntries = MenuConfiguration.getMenuEntries();
        menuEntries.forEach(entry -> {
            if (entry.icon() != null) {
                nav.addItem(new SideNavItem(entry.title(), entry.path(), new SvgIcon(entry.icon())));
            } else {
                nav.addItem(new SideNavItem(entry.title(), entry.path()));
            }
        });

        return nav;
    }

    private Footer createFooter() {
        Footer footer = new Footer();
        footer.addClassName("drawer-footer");

        Button logoutButton = new Button("Logout");
        logoutButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        logoutButton.addClassName("logout-button");
        logoutButton.addClickListener(e -> {
            UI.getCurrent().getSession().close();
            UI.getCurrent().navigate("login");
            logout();
            Notification.show("Logout successful", 3000, Notification.Position.BOTTOM_CENTER);

        });
        HorizontalLayout footerBar = new HorizontalLayout(logoutButton);
        footerBar.setWidthFull();
        footerBar.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        footerBar.setAlignItems(FlexComponent.Alignment.CENTER);
        footerBar.addClassName("drawer-footer-bar");
        footer.add(footerBar);

        return footer;
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        viewTitle.setText(getCurrentPageTitle());
    }

    private String getCurrentPageTitle() {
        return MenuConfiguration.getPageHeader(getContent()).orElse("");
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        checkTokenValidity();
        if (VaadinSession.getCurrent().getAttribute("jwt") == null) {
            beforeEnterEvent.forwardTo("login");
        }
    }
    @Override
    public void afterNavigation(AfterNavigationEvent afterNavigationEvent) {
        checkTokenValidity();
    }
    private void checkTokenValidity() {
        try {
            String token = (String) VaadinSession.getCurrent().getAttribute("jwt");
            String email = (String) VaadinSession.getCurrent().getAttribute("user");

            if (token == null || email == null) {
                logout();
                return;
            }

            Users user = usersService.findByEmail(email);
            if (user == null || !jwtUtil.isTokenValid(token, user)) {
                Notification.show("Invalid Authentication", 3000, Notification.Position.BOTTOM_CENTER);
                logout();
            }
        } catch (IllegalStateException e) {
            // Handle expired token exception
            Notification.show("Token Expired", 3000, Notification.Position.BOTTOM_CENTER);
            logout();
        }
    }
    private void logout() {

        VaadinSession.getCurrent().setAttribute("user", null);
        VaadinSession.getCurrent().setAttribute("jwt", null);
        VaadinSession.getCurrent().close();
        UI.getCurrent().getPage().setLocation("login");
    }
}
