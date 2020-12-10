package cs451.broadcast;

import cs451.Receiver;
import cs451.Host;
import cs451.links.PerfectLink;
import cs451.links.PerfectLinkPriorityQueue;
import cs451.messages.Message;

import java.util.List;

/**
 * Implements a basic process for this project. Contains the main information (hosts, port...)
 */
public class BestEffortBroadcast implements Broadcaster, Receiver {
    private final List<Host> hosts;
    private final Host myHost;
    //private final PerfectLink perfectLink;
    private final PerfectLinkPriorityQueue perfectLink;
    private final Receiver receiver;

    public BestEffortBroadcast(Receiver receiver, List<Host> hosts, Host myHost) {
        this.hosts = hosts;
        this.myHost = myHost;
        //this.perfectLink = new PerfectLink(this, myHost);
        this.perfectLink = new PerfectLinkPriorityQueue(this, myHost, hosts);
        this.receiver = receiver;

        Thread linkThread = new Thread(perfectLink);
        linkThread.start();
    }

    public void broadcast(Message message) {
        hosts.forEach(destHost -> perfectLink.send(message, destHost));
    }

    @Override
    public void deliver(Message message) {
        receiver.deliver(message);
    }
}
