package com.yahya.view.chat;

import com.vaadin.collaborationengine.CollaborationAvatarGroup;
import com.vaadin.collaborationengine.CollaborationMessageInput;
import com.vaadin.collaborationengine.CollaborationMessageList;
import com.vaadin.collaborationengine.UserInfo;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Aside;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Page;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.theme.lumo.LumoUtility.AlignItems;
import com.vaadin.flow.theme.lumo.LumoUtility.Background;
import com.vaadin.flow.theme.lumo.LumoUtility.BoxSizing;
import com.vaadin.flow.theme.lumo.LumoUtility.Display;
import com.vaadin.flow.theme.lumo.LumoUtility.Flex;
import com.vaadin.flow.theme.lumo.LumoUtility.FlexDirection;
import com.vaadin.flow.theme.lumo.LumoUtility.JustifyContent;
import com.vaadin.flow.theme.lumo.LumoUtility.Margin;
import com.vaadin.flow.theme.lumo.LumoUtility.Overflow;
import com.vaadin.flow.theme.lumo.LumoUtility.Padding;
import com.vaadin.flow.theme.lumo.LumoUtility.Width;
import com.vaadin.flow.component.textfield.EmailField;
import com.yahya.model.Friendship;
import com.yahya.model.Users;
import com.yahya.service.FriendshipService;
import com.yahya.service.UsersService;
import com.yahya.view.MainLayout;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@PageTitle("Chat")
@Route(value = "", layout = MainLayout.class)
@RouteAlias(value = "chat", layout = MainLayout.class)
@Menu(order = 0, title = "Chat")
public class ChatView extends HorizontalLayout {

    private final UsersService usersService;
    private final FriendshipService friendshipService;

    private Users currentUser;
    private Users activeFriend;
    private UserInfo userInfo;

    private CollaborationMessageList messageList;
    private CollaborationMessageInput messageInput;
    private final VerticalLayout friendListLayout = new VerticalLayout();
    private final VerticalLayout incomingLayout = new VerticalLayout();
    private final VerticalLayout outgoingLayout = new VerticalLayout();
    private final Span conversationTitle = new Span();
    private final Div emptyState = new Div();

    public ChatView(UsersService usersService, FriendshipService friendshipService) {
        this.usersService = usersService;
        this.friendshipService = friendshipService;

        addClassNames("chat-view", Width.FULL, Display.FLEX, Flex.AUTO);
        setSpacing(false);
        setSizeFull();

        initCurrentUser();
        if (currentUser == null) {
            return;
        }
        buildLayout();
        refreshFriends();
    }

    private void initCurrentUser() {
        String email = (String) VaadinSession.getCurrent().getAttribute("user");
        currentUser = email != null ? usersService.findByEmail(email) : null;
        if (currentUser == null) {
            UI.getCurrent().navigate("login");
            return;
        }
        userInfo = new UserInfo(
                currentUser.getId(),
                currentUser.getName() != null ? currentUser.getName() : currentUser.getEmail(),
                currentUser.getPictureUrl()
        );
    }

    private void buildLayout() {
        Aside side = buildSidebar();

        messageList = new CollaborationMessageList(userInfo, friendshipService.topicFor(currentUser.getId(), UUID.randomUUID().toString()));
        messageList.setSizeFull();
        messageInput = new CollaborationMessageInput(messageList);
        messageInput.setWidthFull();

        conversationTitle.addClassName("muted");
        emptyState.setText("Tambah teman dan pilih teman untuk memulai chat.");
        emptyState.addClassName("muted");

        VerticalLayout header = new VerticalLayout(conversationTitle, emptyState);
        header.setPadding(false);
        header.setSpacing(false);
        header.setAlignItems(FlexComponent.Alignment.START);

        VerticalLayout chatContainer = new VerticalLayout(header, messageList, messageInput);
        chatContainer.addClassNames(Flex.AUTO, Overflow.HIDDEN);
        chatContainer.setSizeFull();
        chatContainer.setPadding(true);
        chatContainer.setSpacing(true);

        add(chatContainer, side);
        expand(chatContainer);
    }

    private Aside buildSidebar() {
        Aside side = new Aside();
        side.addClassNames(Display.FLEX, FlexDirection.COLUMN, Flex.GROW_NONE, Flex.SHRINK_NONE, Background.CONTRAST_5);
        side.setWidth("22rem");

        Header header = new Header();
        header.addClassNames(Display.FLEX, FlexDirection.ROW, Width.FULL, AlignItems.CENTER, Padding.MEDIUM,
                BoxSizing.BORDER);
        H3 title = new H3("Teman");
        title.addClassNames(Flex.GROW, Margin.NONE);
        CollaborationAvatarGroup avatarGroup = new CollaborationAvatarGroup(userInfo, "chat");
        avatarGroup.setMaxItemsVisible(4);
        avatarGroup.addClassNames(Width.AUTO);
        header.add(title, avatarGroup);

        friendListLayout.setPadding(false);
        friendListLayout.setSpacing(true);
        friendListLayout.addClassName("friend-list");

        incomingLayout.setPadding(false);
        incomingLayout.setSpacing(false);
        outgoingLayout.setPadding(false);
        outgoingLayout.setSpacing(false);

        EmailField emailField = new EmailField("Tambah teman");
        emailField.setPlaceholder("email teman");
        emailField.setWidthFull();

        Button addFriend = new Button("Kirim permintaan");
        addFriend.addClickListener(e -> {
            if (emailField.isEmpty()) {
                return;
            }
            try {
                friendshipService.sendRequest(currentUser.getId(), emailField.getValue());
                emailField.clear();
                com.vaadin.flow.component.notification.Notification.show("Permintaan dikirim", 2000, com.vaadin.flow.component.notification.Notification.Position.BOTTOM_CENTER);
                refreshFriends();
            } catch (Exception ex) {
                com.vaadin.flow.component.notification.Notification.show(ex.getMessage(), 2500, com.vaadin.flow.component.notification.Notification.Position.BOTTOM_CENTER);
            }
        });
        addFriend.setWidthFull();

        VerticalLayout addFriendBlock = new VerticalLayout(emailField, addFriend);
        addFriendBlock.setPadding(false);
        addFriendBlock.setSpacing(true);

        Div incomingTitle = new Div(new Span("Permintaan masuk"));
        incomingTitle.addClassName("muted");
        Div outgoingTitle = new Div(new Span("Permintaan terkirim"));
        outgoingTitle.addClassName("muted");

        VerticalLayout sideContent = new VerticalLayout(header, new Div(new Span("Teman kamu")), friendListLayout,
                addFriendBlock, incomingTitle, incomingLayout, outgoingTitle, outgoingLayout);
        sideContent.setPadding(true);
        sideContent.setSpacing(true);
        sideContent.setSizeFull();
        sideContent.addClassName("friend-panel");

        side.add(sideContent);
        return side;
    }

    private void refreshFriends() {
        List<Users> friends = friendshipService.getFriends(currentUser.getId());
        friendListLayout.removeAll();
        if (friends.isEmpty()) {
            friendListLayout.add(new Span("Belum ada teman. Kirim permintaan untuk mulai chat."));
        } else {
            friends.forEach(friend -> {
                Span name = new Span(friend.getName() != null ? friend.getName() : friend.getEmail());
                name.getElement().getStyle().set("cursor", "pointer");
                name.addClickListener(e -> selectFriend(friend));
                friendListLayout.add(name);
            });
        }

        List<Friendship> incoming = friendshipService.getIncoming(currentUser.getId());
        incomingLayout.removeAll();
        if (incoming.isEmpty()) {
            incomingLayout.add(new Span("Tidak ada permintaan."));
        } else {
            incoming.forEach(req -> {
                Users requester = usersService.findById(req.getRequesterId());
                String name = requester != null ? displayName(requester) : "Pengguna";
                Button accept = new Button("Terima", e -> {
                    try {
                        friendshipService.accept(req.getId(), currentUser.getId());
                        com.vaadin.flow.component.notification.Notification.show("Permintaan diterima", 2000, com.vaadin.flow.component.notification.Notification.Position.BOTTOM_CENTER);
                        refreshFriends();
                    } catch (Exception ex) {
                        com.vaadin.flow.component.notification.Notification.show(ex.getMessage(), 2500, com.vaadin.flow.component.notification.Notification.Position.BOTTOM_CENTER);
                    }
                });
                accept.addClassName("friend-accept");
                HorizontalLayout row = new HorizontalLayout(new Span(name), accept);
                row.setAlignItems(FlexComponent.Alignment.CENTER);
                incomingLayout.add(row);
            });
        }

        List<Friendship> outgoing = friendshipService.getOutgoing(currentUser.getId());
        outgoingLayout.removeAll();
        if (outgoing.isEmpty()) {
            outgoingLayout.add(new Span("Tidak ada permintaan."));
        } else {
            outgoing.forEach(req -> {
                Users target = usersService.findById(req.getAddresseeId());
                String name = target != null ? displayName(target) : "Pengguna";
                HorizontalLayout row = new HorizontalLayout(new Span(name), new Span("Menunggu"));
                row.setAlignItems(FlexComponent.Alignment.CENTER);
                outgoingLayout.add(row);
            });
        }

        if (activeFriend == null && !friends.isEmpty()) {
            selectFriend(friends.get(0));
        }
        if (friends.isEmpty()) {
            activeFriend = null;
            updateActiveTopic();
        }
    }

    private void selectFriend(Users friend) {
        this.activeFriend = friend;
        updateActiveTopic();
    }

    private void updateActiveTopic() {
        boolean hasFriend = activeFriend != null;
        String titleText = hasFriend ? "Chat dengan " + displayName(activeFriend) : "Belum ada teman dipilih";
        conversationTitle.setText(titleText);
        emptyState.setVisible(!hasFriend);
        messageInput.setEnabled(hasFriend);
        if (hasFriend) {
            String topic = friendshipService.topicFor(currentUser.getId(), activeFriend.getId());
            messageList.setTopic(topic);
        } else {
            messageList.setTopic("chat/none/" + currentUser.getId());
        }
    }

    private String displayName(Users user) {
        return Objects.requireNonNullElse(user.getName(), user.getEmail());
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        Page page = attachEvent.getUI().getPage();
        page.retrieveExtendedClientDetails(details -> setMobile(details.getWindowInnerWidth() < 740));
        page.addBrowserWindowResizeListener(e -> setMobile(e.getWidth() < 740));
    }

    private void setMobile(boolean mobile) {
        // collapse sidebar? no-op for now
    }
}
