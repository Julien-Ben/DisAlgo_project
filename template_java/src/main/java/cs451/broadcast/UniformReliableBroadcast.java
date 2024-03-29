package cs451.broadcast;

import cs451.Host;
import cs451.Receiver;
import cs451.messages.Message;
import cs451.tools.Pair;

import java.util.*;

public class UniformReliableBroadcast implements Broadcaster, Receiver {
    private final List<Host> hosts;
    private final Host myHost;
    private final Receiver receiver;
    private final BestEffortBroadcast beb;

    //MessageId, OriginalSenderId
    private final Set<Pair<Long, Integer>> delivered;
    //MessageId, OriginalSenderId
    private final Set<Pair<Long, Integer>> pending;
    //MessageId, OriginalSenderId
    private final Map<Pair<Long, Integer>, Set<Integer>> ack;

    public UniformReliableBroadcast(Receiver receiver, List<Host> hosts, Host myHost) {
        this.hosts = hosts;
        this.myHost = myHost;
        this.receiver = receiver;
        beb = new BestEffortBroadcast(this, hosts, myHost);

        this.delivered = new HashSet<>();
        this.pending = new HashSet<>();
        this.ack = new HashMap<>();
    }

    public void broadcast(Message message) {
        pending.add(new Pair<>(message.getId(), myHost.getId()));
        beb.broadcast(message);
    }

    @Override
    public void deliver(Message message) {
        Pair<Long, Integer> pair = new Pair<Long, Integer>(message.getId(), message.getOriginalSenderId());
        int senderId = message.getSenderId();
        //ComputeIfAbsent to avoid NullPointerException for new messages
        ack.computeIfAbsent(pair, x -> new HashSet<>()).add(senderId);
        if (!pending.contains(pair)) {
            pending.add(pair);
            beb.broadcast(new Message(message, myHost.getId()));
        }
        deliverIfYouCan(message);
    }

    private boolean canDeliver(Message m) {
        return ack.getOrDefault( new Pair(m.getId(), m.getOriginalSenderId()),
                new HashSet<>()).size() > (double)hosts.size()/2.0;
    }

    private void deliverIfYouCan(Message message) {
        Pair<Long, Integer> pair = new Pair(message.getId(), message.getOriginalSenderId());
        if (pending.contains(pair) && canDeliver(message) && !delivered.contains(pair)) {
            delivered.add(pair);
            pending.remove(pair);
            receiver.deliver(message);
        }
    }
}
