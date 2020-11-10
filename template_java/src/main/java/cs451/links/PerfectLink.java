package cs451.links;

import cs451.Host;
import cs451.Receiver;
import cs451.messages.Message;
import cs451.tools.Pair;

import java.util.*;

public class PerfectLink implements Runnable, Receiver {
    private final FairLossLink fairLossLink;
    //Map Host, MessageID -> Message
    private final HashSet<Pair<Host, Message>> sendBuffer;
    private final Set<Message> receivedMessages;
    private final Receiver receiver;
    private final Host myHost;

    public PerfectLink(Receiver receiver, Host myHost) {
        this.receiver = receiver;
        this.myHost = myHost;
        fairLossLink = new FairLossLink(this, myHost.getPort());
        sendBuffer = new HashSet<>();
        receivedMessages = new HashSet<>();
        Thread fairLossThread = new Thread(fairLossLink);
        fairLossThread.start();
    }

    @Override
    public void run() {
        while (true) {
            //TODO resend a message only if it timed out (one timer per message)
            try {
                sendBuffer.forEach((keypair) -> fairLossLink.send(keypair.y, keypair.x));
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("The content of sendbuffer was : ");
                /*for (Pair pair : sendBuffer) {
                    System.err.println(pair.toString());
                }*/
                System.err.println("The length of sendbuffer was : ");
                System.err.println(sendBuffer.size());
            }
            try {
                Thread.sleep(100);
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
            sendBuffer.remove(new Pair<>(message.getSender(), new Message(message.getId(), message.getContent(), myHost, message.getOriginalSender())));
            /*sendBuffer.removeIf(pair ->
                    message.getSender().equals(pair.x) && message.getId() == pair.y.getId()
                    && message.getOriginalSender() == pair.y.getOriginalSender());*/

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
