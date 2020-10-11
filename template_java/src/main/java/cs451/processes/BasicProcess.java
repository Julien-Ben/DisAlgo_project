package cs451.processes;

import cs451.BarrierParser;
import cs451.Host;

import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
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
            FileWriter myWriter = new FileWriter(filename, true);
            myWriter.write(message);
            myWriter.write(System.lineSeparator());
            myWriter.close();
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {


        }
    }

    private void run() {
        barrier.waitOnBarrier();

        String message = "Hello, i'm speaking My PID is " + pid + " and my Id is " + id;
        byte[] buf_send;
        buf_send = message.getBytes();
        try {
            DatagramSocket mySocket = new DatagramSocket(port);
            DatagramPacket myPacket;
            // Trying things with UDP
            for (Host host: hosts) {
                myPacket = new DatagramPacket(buf_send, buf_send.length, InetAddress.getByName(host.getIp()), host.getPort());
                mySocket.send(myPacket);
            }
            byte[] buf_receive = new byte[256];
            DatagramPacket packet_receive = new DatagramPacket(buf_receive, buf_receive.length);
            while (true) {
                mySocket.receive(packet_receive);
                writeToFile(outputFile, id + " : " + new String(buf_receive, StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
            System.out.println("Something gone wrong  : "+e.getMessage());
        }
    }
}
