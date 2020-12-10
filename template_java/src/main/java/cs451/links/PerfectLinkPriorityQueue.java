package cs451.links;

import cs451.Host;
import cs451.Receiver;
import cs451.messages.Message;
import cs451.tools.Pair;
import cs451.tools.Utils;
import jdk.jshell.execution.Util;

import java.util.*;
import java.util.concurrent.PriorityBlockingQueue;

public class PerfectLinkPriorityQueue implements Runnable, Receiver {
    private final FairLossLink fairLossLink;
    private final PriorityBlockingQueue<SendQueueElement> sendQueue;
    private final Set<Message> receivedMessages;
    //Pair Dest, Message
    private final Set<Pair<Integer,Message>> sentMessages;
    private final Receiver receiver;
    private final List<Host> hosts;
    private final Host myHost;
    private final Map<Host, Long> hostToTimeout;

    public PerfectLinkPriorityQueue(Receiver receiver, Host myHost, List<Host> hosts) {
        this.receiver = receiver;
        this.myHost = myHost;
        this.hosts = hosts;
        fairLossLink = new FairLossLink(this, myHost.getPort());
        sendQueue = new PriorityBlockingQueue<>();
        receivedMessages = new HashSet<>();
        sentMessages = new HashSet<>();
        hostToTimeout = new HashMap<>();
        Thread fairLossThread = new Thread(fairLossLink);
        fairLossThread.start();
    }

    @Override
    public void run() {
        while (true) {
            try {
                SendQueueElement elem = sendQueue.take();
                if (!sentMessages.contains(elem.getMessagePair())) {
                    long deltaTime = elem.getNextTimeStamp() - System.currentTimeMillis();
                    if (deltaTime > 0) {
                        sleep(deltaTime);
                    }
                    fairLossLink.send(elem.getMessagePair().y, Utils.getHostFromId(hosts,elem.getMessagePair().x));
                    if (elem.firstTransmission) {
                        elem.firstTransmission = false;
                    } else {
                        updateTimeStamp(elem, true);
                    }
                    sendQueue.put(elem);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void updateTimeStamp(SendQueueElement elem, boolean inc) {
        //Updating retransmission delay for host
        Host messageHost = Utils.getHostFromId(hosts, elem.getMessagePair().y.getSenderId());
        long currentDelay = hostToTimeout.computeIfAbsent(messageHost, x -> SendQueueElement.INITIAL_TIMEOUT);
        int MAXDELAY = 1000;
        int MINDELAY = 10;
        double increaserate = 0.2 * (1 - currentDelay/MAXDELAY);
        double decreaserate = 0.2 *  (1 - (10/MINDELAY));
        long newDelay = (long) (currentDelay * (inc ? 1 + increaserate : 1 - decreaserate));
        hostToTimeout.put(messageHost, newDelay);
        elem.updateTimeStamp(newDelay);
    }

    public void sleep(long milis) {
        try {
            Thread.sleep(milis);
        } catch (InterruptedException e) {
            System.out.print("Thread interrupted");
            e.printStackTrace();
        }
    }

    public void send(Message message, Host dest) {
        if (message.getContent().equals("ack")) {
            //TODO improve
            return;
        }
        sendQueue.add(new SendQueueElement(new Pair(dest.getId(), message)));
    }

    @Override
    public void deliver(Message message) {
        if (message.getContent().equals("ack")) {
            Pair comparePair = new Pair<>(message.getSenderId(), new Message(message, myHost.getId()));
            sentMessages.add(comparePair);
        } else if (receivedMessages.contains(message)){
            //Do nothing
        } else {
            fairLossLink.send(new Message(message.getId(), "ack", myHost.getId(), message.getOriginalSenderId(),
                    message.getClock()), Utils.getHostFromId(hosts, message.getSenderId()));
            receivedMessages.add(message);
            receiver.deliver(message);
        }
    }

    class SendQueueElement implements Comparable<SendQueueElement>{
        //Dest host id, message
        private final Pair<Integer, Message> messagePair;
        private long nextTimeStamp;
        private long timeout;
        private static final long INITIAL_TIMEOUT = 20;
        private boolean firstTransmission;

        public SendQueueElement(Pair<Integer, Message> messagePair) {
            this.messagePair = messagePair;
            this.timeout = INITIAL_TIMEOUT;
            this.nextTimeStamp = System.currentTimeMillis() + INITIAL_TIMEOUT;
            this.firstTransmission = true;
        }

        public void updateTimeStamp(long newDelay) {
            timeout = newDelay;
            nextTimeStamp = System.currentTimeMillis() + timeout;
        }

        public Pair<Integer, Message> getMessagePair() {
            return messagePair;
        }

        public long getNextTimeStamp() {
            return nextTimeStamp;
        }

        @Override
        public int compareTo(SendQueueElement o) {
            //We want to inverse the priority
            return o.nextTimeStamp < this.nextTimeStamp ? 1 : -1;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SendQueueElement that = (SendQueueElement) o;
            return Objects.equals(messagePair, that.messagePair);
        }

        @Override
        public int hashCode() {
            return Objects.hash(messagePair);
        }

        @Override
        public String toString() {
            return "SendQueueElement{" +
                    "messagePair=" + messagePair.toString() +
                    ", nextTimeStamp=" + nextTimeStamp +
                    ", timeout=" + timeout +
                    '}' + System.lineSeparator();
        }
    }
}
