package cs451.broadcast;

import cs451.Coordinator;
import cs451.Receiver;
import cs451.Host;
import cs451.messages.Message;
import cs451.links.PerfectLink;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Implements a basic process for this project. Contains the main information (hosts, port...)
 */
public class ReliableBroadcast implements Receiver {
    private final List<Host> hosts;
    private final String outputFile;
    private final Host myHost;
    private List<Host> neighbourHosts;
    private static final int DEFAULT_HALF_WINDOW_SIZE = 1;
    private Long messageId;
    PerfectLink myLink;
    Coordinator coordinator;

    //TODO remove barrierIp, barrierPort...
    public ReliableBroadcast(List<Host> hosts, String outputFile, Host myHost, Coordinator coordinator) {
        this.hosts = hosts;
        this.outputFile = outputFile;
        this.myHost = myHost;
        this.myLink = new PerfectLink(this, myHost);
        this.coordinator = coordinator;

        this.neighbourHosts = new ArrayList<>();
        updateNeighbours(hosts.size()); //hosts.size()/2 + 1

        run();
    }

    public void updateNeighbours(int halfWindowSize) {
        int myIndex = hosts.indexOf(myHost);
        neighbourHosts = new ArrayList<>(hosts.subList(Math.max(0, myIndex-halfWindowSize), Math.min(hosts.size(),
                myIndex+halfWindowSize+1)));
        neighbourHosts.remove(myHost);
        System.out.println("My neighbours are : ");
        for (Host neighbour : neighbourHosts) {
            System.out.println(neighbour.getId() + "");
        }
    }

    //TODO : move into a FileHelper
    public void writeToFile(String filename, String message) {
        try {
            //TODO : try with resource
            FileWriter myWriter = new FileWriter(filename, true);
            myWriter.write(message);
            myWriter.write(System.lineSeparator());
            myWriter.close();
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred when writing file.");
            e.printStackTrace();
        }
    }

    private void broadcast(long id) {
        for (Host destHost: hosts) {
            myLink.send(new Message(id, myHost.getId()+" "+id, myHost, myHost), destHost);
        }
    }

    private void run() {
        coordinator.waitOnBarrier();
        Thread linkThread = new Thread(myLink);
        linkThread.start();

        messageId = 0L;
        broadcast(messageId);
        coordinator.finishedBroadcasting();

    }

    private void relayToNeighbours(Message m) {
        for (Host neighbour: neighbourHosts) {
            myLink.send(new Message(m.getId(), m.getContent(), myHost, m.getOriginalSender()), neighbour);
        }
    }

    @Override
    public void deliver(Message message) {
        if (message.getSender().equals(message.getOriginalSender())) {
            relayToNeighbours(message);
        }
        writeToFile(outputFile, message.getContent());
    }
}
