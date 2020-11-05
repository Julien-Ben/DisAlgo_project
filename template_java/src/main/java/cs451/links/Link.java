package cs451.links;

import cs451.messages.Message;

import java.util.Optional;

public interface Link {
    void send(Message message);
    Optional<Message> deliver();

}
