package cs451.links;

import cs451.Host;
import cs451.Receiver;
import cs451.messages.Message;
import cs451.tools.Pair;

import java.util.*;

public class PerfectLink implements Runnable, Receiver {
    private final FairLossLink fairLossLink;
    private final Map<Pair<Host, Long>, Message> sendBuffer;
    private final Set<Message> receivedMessages;
    private final Receiver receiver;
    private final Host myHost;

    public PerfectLink(Receiver receiver, Host myHost) {
        this.receiver = receiver;
        this.myHost = myHost;
        fairLossLink = new FairLossLink(this, myHost.getPort());
        sendBuffer = new HashMap<Pair<Host, Long>, Message>();
        receivedMessages = new HashSet<>();
        Thread fairLossThread = new Thread(fairLossLink);
        fairLossThread.start();
    }

    @Override
    public void run() {
        while (true) {
            //TODO resend a message only if it timed out (one timer per message)
            sendBuffer.forEach((id, message) -> fairLossLink.send(message, id.x));
            try {
                Thread.sleep(150);
            } catch (InterruptedException e) {
                System.out.print("Thread interrupted");
                e.printStackTrace();
            }
        }
    }

    public void send(Message message, Host dest) {
        if (message.getContent().equals("ack")) {
            //TODO improve
            return;
        }
        sendBuffer.put(new Pair(dest, message.getId()), message);
    }

    @Override
    public void deliver(Message message) {
        //TODO : add an atribute "isAck" in Message or Inheritance to avoid random conversion
        if (message.getContent().equals("ack")) {
            sendBuffer.remove(new Pair<Integer, Long>(message.getSender().getId(), message.getId()));
        } else if (receivedMessages.contains(message)){
            System.out.println("###DEBUG message "+message.getContent() + "already delivered");
            System.out.flush();
        } else {
            fairLossLink.send(new Message(message.getId(), "ack", myHost, myHost), message.getSender());
            receivedMessages.add(message);
            receiver.deliver(message);
        }
    }
}
