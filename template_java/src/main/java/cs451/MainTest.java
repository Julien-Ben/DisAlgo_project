package cs451;

import cs451.parser.Parser;

import java.util.*;

public class MainTest {
    static List<Process> processes = new ArrayList<>();

    private static void handleSignal() {
        //immediately stop network packet processing
        System.out.println("Immediately stopping network packet processing.");
        for (Process process : processes) {
            process.flushStrBuilder();
            process.closeFileWriter();
        }
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
        System.out.println("List of hosts is:");
        List<Coordinator> coordinators = new ArrayList<>();
        for (Host host: parser.hosts()) {
            coordinators.add(new Coordinator(host.getId(), parser.barrierIp(), parser.barrierPort(),
                    parser.signalIp(), parser.signalPort()));
            System.out.println(host.getId() + ", " + host.getIp() + ", " + host.getPort());

        }

        int messages = 10000; //Default number of messages
        Map<Integer, HashSet<Integer>> causalities = null;
        // if config is defined; always check before parser.config()
        if (parser.hasConfig()) {
            messages = parser.getMessages();
            causalities = parser.getCausalities();
        } else {
            System.out.println("No config defined");
        }

        int processNbr = 8;

        for (Host host: parser.hosts()) {
            Process process = new Process(parser.hosts(), parser.output()+host.getId()+".txt", host, coordinators.get(host.getId()-1), messages, causalities);
            Thread procThread = new Thread(process);
            processes.add(process);
            procThread.start();
        }

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
