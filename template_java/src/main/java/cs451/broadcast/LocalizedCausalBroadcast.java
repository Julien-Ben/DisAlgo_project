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

    //Map (OriginalSenderID, SeqNb) -> Message
    private final Map<Pair<Integer, Long>, Message> pending;
    private final long[] V;
    private long lsn;
    private final int myRank;

    public LocalizedCausalBroadcast(Receiver receiver, List<Host> hosts, Host myHost) {
        this.urb = new UniformReliableBroadcast(this, hosts, myHost);
        this.receiver = receiver;
        this.hosts = hosts;

        this.pending = new HashMap<>();
        V = new long[hosts.size()];
        myRank = myHost.getId();
        lsn = 1;
        for (int i = 0; i < V.length; i++) {
            V[i]=1;
        }
    }

    @Override
    public void broadcast(Message message) {
        long[] W = V.clone();
        W[myRank-1] = lsn;
        lsn++;
        urb.broadcast(new Message(message, W));
    }

    @Override
    public void deliver(Message message) {
        int originalSenderId = message.getOriginalSender().getId();
        pending.put(new Pair<>(originalSenderId, message.getId()), message);
        boolean cont = true;
        long clockSender = message.getClock()[originalSenderId-1];
        for (long myClock = V[originalSenderId-1]; myClock <= clockSender && cont ; myClock++) {
            Pair pair = new Pair<>(message.getOriginalSender().getId(), myClock);
            if (pending.containsKey(pair)) {
                V[originalSenderId-1]++;
                receiver.deliver(pending.get(pair));
                pending.remove(pair);
            } else {
                cont = false;
            }
        }
    }
}
