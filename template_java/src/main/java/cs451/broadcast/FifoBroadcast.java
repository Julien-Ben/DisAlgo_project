package cs451.broadcast;

import cs451.Host;
import cs451.Receiver;
import cs451.messages.Message;
import cs451.tools.Pair;

import java.util.*;

public class FifoBroadcast implements Broadcaster, Receiver {

    private final List<Host> hosts;
    private final Host myHost;
    private final Receiver receiver;
    private final UniformReliableBroadcast urb;

    private long seqNb;
    //Map (OriginalSenderID, SeqNb) -> Message
    private final Map<Pair<Integer, Long>, Message> pending;

    //Map OriginalSenderId to seqNb
    private final Map<Integer, Long> next;


    public FifoBroadcast(Receiver receiver, List<Host> hosts, Host myHost) {
        this.hosts = hosts;
        this.myHost = myHost;
        this.receiver = receiver;
        urb = new UniformReliableBroadcast(this, hosts, myHost);
        this.pending = new HashMap<>();
        this.next = new HashMap<>();
        for (Host host : hosts) {
            next.put(host.getId(), 1l);
        }
    }

    @Override
    public void broadcast(Message message) {
        seqNb++;
        urb.broadcast(new Message(seqNb, message.getContent(), message.getSenderId(), message.getOriginalSenderId(), message.getClock()));
    }

    @Override
    public void deliver(Message message) {
        pending.put(new Pair<>(message.getOriginalSenderId(), message.getId()), message);
        boolean cont = true;
        while (cont) {
            cont = false;
            for (Host host : hosts) {
                long nextValue = next.get(host.getId());
                Pair pair = new Pair<>(host.getId(), nextValue);
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
