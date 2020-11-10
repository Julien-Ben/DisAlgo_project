package cs451.broadcast;

import cs451.Coordinator;
import cs451.Host;
import cs451.Receiver;
import cs451.links.PerfectLink;
import cs451.messages.Message;
import cs451.tools.Pair;

import java.util.*;

public class UniformReliableBroadcast implements Receiver {
    private final List<Host> hosts;
    private final Host myHost;
    private final Receiver receiver;
    private final BestEffortBroadcast beb;

    //MessageId, OriginalSenderId
    private final Set<Pair<Long, Long>> delivered;
    //MessageId, OriginalSenderId
    private final Set<Pair<Long, Long>> pending;

    private final Map<Message, Set<Long>> ack;

    public UniformReliableBroadcast(Receiver receiver, List<Host> hosts, int port,
                               Host myHost) {
        this.hosts = hosts;
        this.myHost = myHost;
        this.receiver = receiver;
        beb = new BestEffortBroadcast(this, hosts, port, myHost);

        this.delivered = new HashSet<>();
        this.pending = new HashSet<>();
        this.ack = new HashMap<>();
    }

    public void broadcast(long id) {
        pending.add(new Pair<Long, Long>(id, (long) myHost.getId()));
        beb.broadcast(id);
    }

    @Override
    public void deliver(Message message) {
        long originalSenderId = getOriginalSenderId(message);
        ack.getOrDefault(message, new HashSet<Long>());
        ack.get(message).add(originalSenderId);
        Pair<Long, Long> pair = new Pair<>(originalSenderId, originalSenderId);
        if (!pending.contains(pair)) {
            pending.add(pair);
            deliverIfYouCan(message, getOriginalSenderId(message));
            beb.broadcast(message.getId(), originalSenderId + " " + message.getId());
        }
    }

    private boolean canDeliver(Message m) {
        return ack.get(m).size() > (double)hosts.size()/2.0;
    }

    private void deliverIfYouCan(Message message, long originalSenderId) {
        Pair<Long, Long> pair = new Pair(message.getId(), originalSenderId);
        if (pending.contains(pair) && canDeliver(message) && !delivered.contains(pair)) {
            delivered.add(pair);
            receiver.deliver(message);
        }
    }

    private long getOriginalSenderId(Message message) {
        return Long.parseLong(message.getContent().split(" ")[0]);
    }
}
