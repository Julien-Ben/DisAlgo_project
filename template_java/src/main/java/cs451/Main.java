package cs451;

import cs451.broadcast.BestEffortBroadcast;
import cs451.parser.Parser;

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

        Coordinator coordinator = new Coordinator(parser.myId(), parser.barrierIp(), parser.barrierPort(), parser.signalIp(), parser.signalPort());

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

        // if config is defined; always check before parser.config()
        if (parser.hasConfig()) {
            System.out.println("Config: " + parser.config());
        }

        //BasicProcess process = new BasicProcess(parser.hosts(), parser.myId(), parser.output(), "", myPort, pid, parser.barrierIp(), parser.barrierPort(), myHost);
        //ReliableBroadcast process = new ReliableBroadcast(parser.hosts(), parser.myId(), parser.output(), "", myPort, pid, parser.barrierIp(), parser.barrierPort(), myHost, coordinator);
        BestEffortBroadcast process = new BestEffortBroadcast(parser.hosts(), parser.myId(), parser.output(), "", myPort, pid, myHost, coordinator);

    }
}
