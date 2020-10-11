package cs451;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class Main {

    private static void handleSignal() {
        //immediately stop network packet processing
        System.out.println("Immediately stopping network packet processing.");

        //write/flush output file if necessary
        System.out.println("Writing output.");
    }

    private static void initSignalHandlers() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                handleSignal();
            }
        });
    }

    public static void main(String[] args) {
        Parser parser = new Parser(args);
        parser.parse();

        initSignalHandlers();

        // example
        long pid = ProcessHandle.current().pid();
        System.out.println("My PID is " + pid + ".");
        System.out.println("Use 'kill -SIGINT " + pid + " ' or 'kill -SIGTERM " + pid + " ' to stop processing packets.");

        System.out.println("My id is " + parser.myId() + ".");
        System.out.println("List of hosts is:");
        int myPort = 0;
        for (Host host: parser.hosts()) {
            System.out.println(host.getId() + ", " + host.getIp() + ", " + host.getPort());
            if (host.getId() == parser.myId()) {
                myPort = host.getPort();
            }
        }

        System.out.println("Barrier: " + parser.barrierIp() + ":" + parser.barrierPort());
        System.out.println("Output: " + parser.output());
        // if config is defined; always check before parser.config()
        if (parser.hasConfig()) {
            System.out.println("Config: " + parser.config());
        }

        BarrierParser.Barrier.waitOnBarrier();

        String message = "Hello, i'm speaking My PID is " + pid + " and my Id is " + parser.myId();
        byte[] buf_send;
        buf_send = message.getBytes();
        try {
            DatagramSocket mySocket = new DatagramSocket(myPort);
            DatagramPacket myPacket;
            // Trying things with UDP
            for (Host host: parser.hosts()) {
                myPacket = new DatagramPacket(buf_send, buf_send.length, InetAddress.getByName(host.getIp()), host.getPort());
                mySocket.send(myPacket);
            }
            byte[] buf_receive = new byte[256];
            DatagramPacket packet_receive = new DatagramPacket(buf_receive, buf_receive.length);
            while (true) {
                mySocket.receive(packet_receive);
                System.out.println(parser.myId() + " : " + new String(buf_receive, StandardCharsets.UTF_8));
            }

        } catch (Exception e) {
            System.out.println("Something gone wrong  : "+e.getMessage());
        }

    }
}
