package com.yahya.view.chat;

import com.vaadin.collaborationengine.CollaborationMessageInput;
import com.vaadin.collaborationengine.CollaborationMessageList;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.shared.Registration;

/**
 * Collaboration message input that exposes submit events so we can hook logging.
 */
public class LoggingCollaborationMessageInput extends CollaborationMessageInput {

    public LoggingCollaborationMessageInput(CollaborationMessageList messageList) {
        super(messageList);
    }

    public Registration addSubmitListener(ComponentEventListener<MessageInput.SubmitEvent> listener) {
        return getContent().addSubmitListener(listener);
    }
}
