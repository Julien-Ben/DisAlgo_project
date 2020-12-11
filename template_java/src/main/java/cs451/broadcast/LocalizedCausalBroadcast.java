package cs451.broadcast;

import cs451.Host;
import cs451.Receiver;
import cs451.messages.Message;
import cs451.tools.Pair;

import java.util.*;
import java.util.concurrent.Semaphore;

public class LocalizedCausalBroadcast implements Broadcaster, Receiver {
    private final UniformReliableBroadcast urb;
    private final Receiver receiver;
    private final List<Host> hosts;
    private final Host myHost;

    //Limit the number of messages transmitted at the same time on the network
    //Improvment : better congestion control and move it to perfect link
    //Found empirically
    private static final int MAX_MESSAGES = 6000 * 3;
    private final Semaphore semaphore;
    
    private final Map<Integer, HashSet<Integer>> causalities;
    //Map (OriginalSenderID, SeqNb) -> Message
    private final Map<Pair<Integer, Long>, Message> pending;
    private final Map<Integer, PriorityQueue<Message>> pendingQueues;
    private final long[] vClockDeliver;
    private final long[] vClockSend;
    private long lsn;
    private final int myRank;

    public LocalizedCausalBroadcast(Receiver receiver, List<Host> hosts, Host myHost, Map<Integer,
            HashSet<Integer>> causalities) {
        this.urb = new UniformReliableBroadcast(this, hosts, myHost);
        this.receiver = receiver;
        this.hosts = hosts;
        this.myHost = myHost;
        this.semaphore = new Semaphore(MAX_MESSAGES/hosts.size());
        if (causalities==null) {
            this.causalities = new HashMap<>();
            for (Host host : hosts) {
                causalities.put(host.getId(), new HashSet<>());
            }
            causalities.get(myHost.getId()).add(myHost.getId());
        } else {
            this.causalities = Map.copyOf(causalities);
        }
        this.pending = new HashMap<>();
        this.pendingQueues = new HashMap<>();
        for (Host host : hosts) {
            pendingQueues.put(host.getId(), new PriorityQueue<Message>((elem1, elem2) -> Long.compare(elem1.getId(), elem2.getId())));
        }
        vClockDeliver = new long[hosts.size()];
        vClockSend = new long[hosts.size()];
        myRank = myHost.getId();
        lsn = 1;
        for (int i = 0; i < vClockDeliver.length; i++) {
            vClockDeliver[i]=1;
            if (causalities.get(myHost.getId()).contains(i+1)) {
                vClockSend[i] = 1;
            }
        }
    }

    @Override
    public void broadcast(Message message) {
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
            semaphore.release();
            return;
        }
        long[] W = vClockSend.clone();
        W[myRank-1] = lsn;
        lsn++;
        urb.broadcast(new Message(message, W));
    }

    @Override
    public void deliver(Message message) {
        //Not proud of that code, not optimized, not readable, spaghetti
        int originalSenderId = message.getOriginalSenderId();
        if (!canDeliver(message)) {
            pendingQueues.get(message.getOriginalSenderId()).add(message);
        }
        else {
            vClockDeliver[originalSenderId-1]++;
            receiver.deliver(message);
            boolean contGlobal;
            do {
                contGlobal = false;
                for (Host host : hosts) {
                    Iterator<Message> it = pendingQueues.get(host.getId()).iterator();
                    while (it.hasNext()) {
                        Message m = it.next();
                        if (canDeliver(m)) {
                            vClockDeliver[host.getId()-1]++;
                            if (causalities.get(myHost.getId()).contains(m.getOriginalSenderId())) {
                                vClockSend[host.getId()-1]++;
                            }
                            it.remove();
                            receiver.deliver(m);
                            if (m.getOriginalSenderId() == myHost.getId()){
                                semaphore.release();
                            }
                            contGlobal = true;
                        } else {
                            break;
                        }
                    }
                }
            } while (contGlobal);
        }
    }

    private boolean canDeliver(Message message) {
        for (int i = 0; i < vClockDeliver.length; i++) {
            if (vClockDeliver[i] < message.getClock()[i]) {
                return false;
            }
        }
        return true;
    }
}
