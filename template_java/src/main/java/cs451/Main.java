package cs451;

import cs451.parser.Parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;

public class Main {
    static Process process;

    private static void handleSignal() {
        //immediately stop network packet processing
        System.out.println("Immediately stopping network packet processing.");
        //write/flush output file if necessary
        System.out.println("Writing output.");
        process.flushStrBuilder();
        process.closeFileWriter();

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

        Coordinator coordinator = new Coordinator(parser.myId(), parser.barrierIp(), parser.barrierPort(),
                parser.signalIp(), parser.signalPort());

        // example
        long pid = ProcessHandle.current().pid();
        System.out.println("List of hosts is:");
        int myPort = 0;
        Host myHost = null;
        for (Host host: parser.hosts()) {
            System.out.println(host.getId() + ", " + host.getIp() + ", " + host.getPort());
            if (host.getId() == parser.myId()) {
                myPort = host.getPort();
                myHost = host;
            }
        }

        int messages = 20; //Default number of messages
        Map<Integer, HashSet<Integer>> causalities = null;
        // if config is defined; always check before parser.config()
        if (parser.hasConfig()) {
            messages = parser.getMessages();
            causalities = parser.getCausalities();
        } else {
            System.out.println("No config defined");
        }
        System.out.println("My PID is " + pid + ".");
        System.out.println("Use 'kill -SIGINT " + pid + " ' or 'kill -SIGTERM " + pid + " ' to stop processing packets.");
        System.out.println("My id is " + parser.myId() + ".");

        process = new Process(parser.hosts(), parser.output(), myHost, coordinator, messages,
                causalities);
        process.run();

        while (true) {
            // Sleep for 1 hour
            try {
                Thread.sleep(60 * 60 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
