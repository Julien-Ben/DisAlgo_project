package cs451.links;

import cs451.Message;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class StubbornLink implements Link, Runnable{
    FairLossLink fairLossLink;
    Map<Integer, Message> buffer;

    public StubbornLink(int port) {
        fairLossLink = new FairLossLink(port);
        buffer = new HashMap<Integer, Message>();
    }

    public void run() {
        while (true) {
            deliver();
            buffer.forEach((id,message) -> fairLossLink.send(message));
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                System.out.print("Thread interrupted");
                e.printStackTrace();
            }
        }
    }

    public void send(Message message) {
        buffer.put(message.getId(), message);
    }

    public Optional<Message> deliver() {
        Optional<Message> optMessage = fairLossLink.deliver();
        if (optMessage.isPresent()) {
            Message message = optMessage.get();
            if (!message.getContent().startsWith("ack")) {
                fairLossLink.send(new Message(0, "ack"+message.getId(), message.getDest(), message.getSender()));
            } else if (message.getContent().startsWith("ack")) {
                String stringId = message.getContent().substring(3);
                int messageId = 0;
                try {
                    messageId = Integer.parseInt(stringId);
                } catch (NumberFormatException e) {
                    System.out.println("Impossible to parse the string into a message id");
                    e.printStackTrace();
                }
                buffer.remove(messageId);
            }
        }
        return optMessage;
    }
}
