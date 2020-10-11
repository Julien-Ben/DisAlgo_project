package cs451;

import cs451.processes.BasicProcess;

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

        System.out.println("List of hosts is:");
        int myPort = 0;
        for (Host host: parser.hosts()) {
            System.out.println(host.getId() + ", " + host.getIp() + ", " + host.getPort());
            if (host.getId() == parser.myId()) {
                myPort = host.getPort();
            }
        }

        // if config is defined; always check before parser.config()
        if (parser.hasConfig()) {
            System.out.println("Config: " + parser.config());
        }

        BasicProcess process = new BasicProcess(parser.hosts(), parser.myId(), parser.output(), "", myPort, pid, parser.barrierIp(), parser.barrierPort());

    }
}
