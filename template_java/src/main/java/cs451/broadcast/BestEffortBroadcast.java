package cs451.broadcast;

import cs451.Coordinator;
import cs451.Deliverer;
import cs451.Host;
import cs451.links.PerfectLink;
import cs451.messages.Message;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Implements a basic process for this project. Contains the main information (hosts, port...)
 */
public class BestEffortBroadcast implements Deliverer {
    private final List<Host> hosts;
    private final int id;
    private final String outputFile;
    private final String ip;
    private final int port;
    private final Long pid;
    private final Host myHost;
    private Long messageId;
    PerfectLink myLink;
    Coordinator coordinator;

    //TODO remove barrierIp, barrierPort...
    public BestEffortBroadcast(List<Host> hosts, int id, String outputFile, String ip, int port, Long pid,
                                Host myHost, Coordinator coordinator) {
        this.hosts = hosts;
        this.id = id;
        this.outputFile = outputFile;
        this.ip = ip;
        this.port = port;
        this.pid = pid;
        this.myHost = myHost;
        this.myLink = new PerfectLink(this, port);
        this.coordinator = coordinator;

        System.out.println("My PID is " + pid + ".");
        System.out.println("Use 'kill -SIGINT " + pid + " ' or 'kill -SIGTERM " + pid + " ' to stop processing packets.");
        System.out.println("My id is " + id+ ".");

        System.out.println("Output: " + outputFile);
        run();
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
        hosts.parallelStream().forEach(destHost -> myLink.send(new Message(id, myHost.getId()+" "+id, myHost, destHost)));
    }

    private void run() {
        coordinator.waitOnBarrier();
        Thread linkThread = new Thread(myLink);
        linkThread.start();

        messageId = 0L;
        broadcast(messageId);
        try {
            Thread.sleep(8000);
        } catch (InterruptedException e) {
            System.out.print("Thread interrupted");
            e.printStackTrace();
        }
        coordinator.finishedBroadcasting();
    }

    @Override
    public void deliver(Message message) {
        writeToFile(outputFile, message.getContent());
    }
}
