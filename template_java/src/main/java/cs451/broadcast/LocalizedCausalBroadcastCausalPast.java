package cs451.broadcast;

import cs451.Host;
import cs451.Receiver;
import cs451.messages.Message;
import cs451.tools.Pair;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LocalizedCausalBroadcastCausalPast implements Broadcaster, Receiver {
    private final FifoBroadcast fifoBroadcast;
    //MessageId, OriginalSenderId
    private final Set<Pair<Long, Integer>> delivered;
    private final Receiver receiver;

    public LocalizedCausalBroadcastCausalPast(Receiver receiver, List<Host> hosts, Host myHost) {
        this.fifoBroadcast = new FifoBroadcast(this, hosts, myHost);
        this.delivered = new HashSet<>();
        this.receiver = receiver;
    }

    @Override
    public void broadcast(Message message) {
        fifoBroadcast.broadcast(message);
//        past.add(message);
    }

    @Override
    public void deliver(Message message) {
        if (!delivered.contains(message)) {
//            for (Message n : past) {
//                if (!delivered.contains(n)) {
//                    receiver.deliver(n);
//                    delivered.add(n);
//                    if (!past.contains(n)) {
//                        past.add(n);
//                    }
//                }
//            }
//            receiver.deliver(m);
//            delivered.add(m);
//            if (!past.contains(m)) {
//                past.add(m);
//            }
        }
    }
}
