package cs451.links;

import cs451.Host;
import cs451.Receiver;
import cs451.messages.Message;
import cs451.tools.Pair;
import cs451.tools.Utils;

import java.util.*;

public class PerfectLink implements Runnable, Receiver {
    private final FairLossLink fairLossLink;
    private final HashSet<Pair<Host, Message>> sendBuffer;
    private final Set<Message> receivedMessages;
    private final Receiver receiver;
    private final Host myHost;
    private final List<Host> hosts;

    public PerfectLink(Receiver receiver, Host myHost, List<Host> hosts) {
        this.receiver = receiver;
        this.myHost = myHost;
        this.hosts = hosts;
        fairLossLink = new FairLossLink(this, myHost.getPort());
        sendBuffer = new HashSet<>();
        receivedMessages = new HashSet<>();
        Thread fairLossThread = new Thread(fairLossLink);
        fairLossThread.start();
        throw new IllegalArgumentException("Don't use me");
    }

    @Override
    public void run() {
        while (true) {
            //TODO resend a message only if it timed out (one timer per message)
            try {
                sendBuffer.forEach((keypair) -> fairLossLink.send(keypair.y, keypair.x));
            } catch (Exception e) {
                e.printStackTrace();
            }
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
        sendBuffer.add(new Pair(dest, message));
    }

    @Override
    public void deliver(Message message) {
        //TODO : add an atribute "isAck" in Message or Inheritance to avoid random conversion
        if (message.getContent().equals("ack")) {
            sendBuffer.remove(new Pair<>(message.getSenderId(), new Message(message, myHost.getId())));

        } else if (receivedMessages.contains(message)){
            //Do nothing
        } else {
            fairLossLink.send(new Message(message.getId(), "ack", myHost.getId(), message.getOriginalSenderId(), message.getClock()), Utils.getHostFromId(hosts, message.getSenderId()));
            receivedMessages.add(message);
            receiver.deliver(message);
        }
    }
}
