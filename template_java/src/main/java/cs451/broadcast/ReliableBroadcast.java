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
    private final int id;
    private final String outputFile;
    private final String ip;
    private final int port;
    private final String barrierIp;
    private final int barrierPort;
    private final Long pid;
    private final Host myHost;
    private List<Host> neighbourHosts;
    private static final int DEFAULT_HALF_WINDOW_SIZE = 1;
    private Long messageId;
    PerfectLink myLink;
    Coordinator coordinator;

    //TODO remove barrierIp, barrierPort...
    public ReliableBroadcast(List<Host> hosts, int id, String outputFile, String ip, int port, Long pid,
                             String barrierIp, int barrierPort, Host myHost, Coordinator coordinator) {
        this.hosts = hosts;
        this.id = id;
        this.outputFile = outputFile;
        this.ip = ip;
        this.port = port;
        this.pid = pid;
        this.barrierIp = barrierIp;
        this.barrierPort = barrierPort;
        this.myHost = myHost;
        this.myLink = new PerfectLink(this, port);
        this.coordinator = coordinator;

        this.neighbourHosts = new ArrayList<>();
        updateNeighbours(hosts.size()); //hosts.size()/2 + 1

        System.out.println("My PID is " + pid + ".");
        System.out.println("Use 'kill -SIGINT " + pid + " ' or 'kill -SIGTERM " + pid + " ' to stop processing packets.");
        System.out.println("My id is " + id+ ".");

        System.out.println("Barrier: " + barrierIp + ":" + barrierPort);
        System.out.println("Output: " + outputFile);
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
            myLink.send(new Message(id, myHost.getId()+" "+id, myHost, destHost));
        }
    }

    private void run() {
        coordinator.waitOnBarrier();
        String message = "";
        Thread linkThread = new Thread(myLink);
        linkThread.start();

        messageId = 0L;
        broadcast(messageId);
        coordinator.finishedBroadcasting();

    }

    private void relayToNeighbours(Message m) {
        for (Host neighbour: neighbourHosts) {
            myLink.send(new Message(m.getId(), makeRelay(m.getContent()), myHost, neighbour));
        }
    }

    private String makeRelay(String content) {
        return "relay" + content;
    }

    private boolean isRelay(String content) {
        return content.startsWith("relay");
    }

    @Override
    public void deliver(Message message) {
        if (!isRelay(message.getContent())) {
            relayToNeighbours(message);
            writeToFile(outputFile, message.getContent());
        }
    }
}
