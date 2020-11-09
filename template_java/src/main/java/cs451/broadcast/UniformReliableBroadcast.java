package cs451.broadcast;

import cs451.Coordinator;
import cs451.Host;
import cs451.Receiver;
import cs451.links.PerfectLink;
import cs451.messages.Message;

import java.util.List;

public class UniformReliableBroadcast implements Receiver {
    private final List<Host> hosts;
    private final Host myHost;
    private final PerfectLink myLink;
    private final Coordinator coordinator;
    private final Receiver receiver;
    private final BestEffortBroadcast beb;

    public UniformReliableBroadcast(Receiver receiver, List<Host> hosts, int port,
                               Host myHost, Coordinator coordinator) {
        this.hosts = hosts;
        this.myHost = myHost;
        this.myLink = new PerfectLink(this, port);
        this.coordinator = coordinator;
        this.receiver = receiver;
        beb = new BestEffortBroadcast(this, hosts, port, myHost, coordinator);

        Thread linkThread = new Thread(myLink);
        linkThread.start();
    }

    public void broadcast(long id) {
        hosts.forEach(destHost -> myLink.send(new Message(id, myHost.getId()+" "+id, myHost, destHost)));
    }

    @Override
    public void deliver(Message message) {
        receiver.deliver(message);
    }
}
