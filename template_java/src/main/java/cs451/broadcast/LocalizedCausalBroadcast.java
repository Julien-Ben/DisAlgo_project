package cs451.broadcast;

import cs451.Host;
import cs451.Receiver;
import cs451.messages.Message;
import cs451.messages.MessageVC;
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
        lsn = 0;
    }

    @Override
    public void broadcast(Message message) {
        long[] W = V.clone();
        W[myRank] = lsn;
        lsn++;
        urb.broadcast(new MessageVC(message, W));
    }

    @Override
    public void deliver(Message message) {
        pending.put(new Pair<>(message.getOriginalSender().getId(), message.getId()), message);
        boolean cont = true;
        while (cont) {
            cont = false;
            for (Host host : hosts) {
                long vectorClock = V[host.getId()];
                //Under this line : to be continued
                Pair pair = new Pair<>(host.getId(), vectorClock);
                if (pending.containsKey(pair)) {
                    next.put(host.getId(), nextValue+1);
                    Message m = pending.get(pair);
                    pending.remove(pair);
                    receiver.deliver(m);
                    cont = true;
                }
            }
        }
    }
}
