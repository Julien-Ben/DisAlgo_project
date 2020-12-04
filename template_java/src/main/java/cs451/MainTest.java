package cs451;

import cs451.parser.Parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

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

        int messages = 8; //Default number of messages

        // if config is defined; always check before parser.config()
        if (parser.hasConfig()) {
            System.out.println("Config: " + parser.config());
            Scanner scanner = null;
            try {
                scanner = new Scanner(new File(parser.config()));
            } catch (FileNotFoundException e) {
                System.out.println("Impossible to open config file");
                e.printStackTrace();
                return;
            }
            while(scanner.hasNextInt()) {
                messages = scanner.nextInt();
            }
        }
        int processNbr = 4;

        for (Host host: parser.hosts()) {
            Process process = new Process(parser.hosts(), parser.output()+host.getId(), host, coordinators.get(host.getId()-1), messages);
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
