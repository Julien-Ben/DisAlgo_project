package cs451;

import cs451.messages.Message;

public interface Receiver {
    void deliver (Message message);
}
