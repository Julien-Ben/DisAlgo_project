package cs451.broadcast;

import cs451.Host;
import cs451.Receiver;
import cs451.messages.Message;
import cs451.tools.Pair;

import java.util.*;

public class LocalizedCausalBroadcast implements Broadcaster, Receiver {
    private final UniformReliableBroadcast urb;
    private final Receiver receiver;
    private final List<Host> hosts;
    private final Host myHost;

    private final Map<Integer, HashSet<Integer>> causalities;
    //Map (OriginalSenderID, SeqNb) -> Message
    private final Map<Pair<Integer, Long>, Message> pending;
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
        if (causalities==null) {
            this.causalities = null;
        } else {
            this.causalities = Map.copyOf(causalities);
        }
        this.pending = new HashMap<>();
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
        long[] W = vClockSend.clone();
        W[myRank-1] = lsn;
        lsn++;
        urb.broadcast(new Message(message, W));
    }

    @Override
    public void deliver(Message message) {
        int originalSenderId = message.getOriginalSender().getId();
        if (!canDeliver(message)) {
            pending.put(new Pair<>(originalSenderId, message.getId()), message);
        }
        else {
            vClockDeliver[originalSenderId-1]++;
            receiver.deliver(message);
            boolean cont = false;
            do {
                cont = false;
                //for (Host host : hosts) {
                    Iterator<Map.Entry<Pair<Integer, Long>,Message>> it = pending.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry<Pair<Integer, Long>,Message> pairMessageEntry = it.next();
                        Message m = pairMessageEntry.getValue();
                        Pair<Integer, Long> p = pairMessageEntry.getKey();
                        if (canDeliver(m)) {
                            vClockDeliver[p.x-1]++;
                            if (causalities.get(myHost.getId()).contains(m.getOriginalSender().getId())) {
                                vClockSend[p.x-1]++;
                            }
                            pending.remove(p);
                            receiver.deliver(m);
                            cont = true;
                            break;
                        }
                    }
                //}
            } while (cont);
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
