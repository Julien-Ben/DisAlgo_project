package cs451.processes;

import cs451.BarrierParser;
import cs451.Host;
import cs451.links.FairLossLink;
import cs451.links.StubbornLink;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Implements a basic process for this project. Contains the main information (hosts, port...)
 */
public class BasicProcess{
    private final List<Host> hosts;
    private final int id;
    private final String outputFile;
    private final String ip;
    private final int port;
    private final String barrierIp;
    private final int barrierPort;
    private final Long pid;
    BarrierParser.Barrier barrier;

    public BasicProcess(List<Host> hosts, int id, String outputFile, String ip, int port, Long pid, String barrierIp, int barrierPort) {
        this.hosts = hosts;
        this.id = id;
        this.outputFile = outputFile;
        this.ip = ip;
        this.port = port;
        this.pid = pid;
        this.barrierIp = barrierIp;
        this.barrierPort = barrierPort;

        System.out.println("My PID is " + pid + ".");
        System.out.println("Use 'kill -SIGINT " + pid + " ' or 'kill -SIGTERM " + pid + " ' to stop processing packets.");
        System.out.println("My id is " + id+ ".");

        System.out.println("Barrier: " + barrierIp + ":" + barrierPort);
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

    private void run() {
        barrier.waitOnBarrier();
        String message = "Hello, i'm speaking My PID is " + pid + " and my Id is " + id;
        StubbornLink myLink = new StubbornLink(port);
        for (Host host: hosts) {
            myLink.send(message, host.getIp(), host.getPort());
        }
        while (true) {
            byte[] buf_receive = myLink.deliver();
            writeToFile(outputFile, id + " : " + new String(buf_receive, StandardCharsets.UTF_8));
        }

    }
}
