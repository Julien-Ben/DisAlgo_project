package cs451;

import cs451.broadcast.Broadcaster;
import cs451.broadcast.FifoBroadcast;
import cs451.broadcast.LocalizedCausalBroadcast;
import cs451.messages.Message;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Implements a basic process for this project. Contains the main information (hosts, port...)
 */
public class Process implements Receiver, Runnable {
    private final String outputFile;
    private final Broadcaster broadcaster;
    private final Coordinator coordinator;
    private final Host myHost;
    private final List<Host> hosts;
    private final int messages;
    private StringBuilder stringBuilder;
    private static final int STR_BUILDER_BATCH = 16384;
    private FileWriter fileWriter;
    private Set<Integer> finishedHosts;
    private long timeStamp = 0;

    public Process(List<Host> hosts, String outputFile, Host myHost, Coordinator coordinator, int messages,
                   Map<Integer, HashSet<Integer>> causalities) {
        this.outputFile = outputFile;
        broadcaster = new LocalizedCausalBroadcast(this, hosts, myHost, causalities);
        this.coordinator = coordinator;
        this.myHost = myHost;
        this.hosts = hosts;
        this.messages = messages;
        this.stringBuilder = new StringBuilder(STR_BUILDER_BATCH);
        finishedHosts = new HashSet<>();
        try {
            this.fileWriter = new FileWriter(outputFile, true);
        } catch (Exception e) {
            System.out.println("An error occurred when opening file.");
            e.printStackTrace();
        }
    }

    //TODO : move into a FileHelper
    public void writeToFile(String message) {
        try {
            //TODO : try with resource
            fileWriter.write(message);
        } catch (Exception e) {
            System.out.println("An error occurred when writing file.");
            e.printStackTrace();
        }
    }

    @Override
    public void run(){
        coordinator.waitOnBarrier();
        timeStamp = System.currentTimeMillis();
        for (int i = 1; i<=messages; i++) {
            broadcaster.broadcast(new Message(i, myHost.getId() + " " + i, myHost.getId(), myHost.getId(), null));
            strBuilderAppend( "b " + i);
        }
        coordinator.finishedBroadcasting();
    }

    @Override
    public void deliver(Message message) {
        strBuilderAppend("d " + message.getContent());
        if (message.getId() % 2000 == 0 && message.getOriginalSenderId() == myHost.getId()) {
            System.out.println(myHost.getId() + " " + message.getId());
            System.out.println("Time : "+(System.currentTimeMillis() - timeStamp));
        }
        if (message.getId() == messages) {
            finishedHosts.add(message.getOriginalSenderId());
            if (finishedHosts.size() == hosts.size()) {
                System.out.println("Flushing");
                flushStrBuilder();
                System.out.println("Finished in " + (System.currentTimeMillis() - timeStamp));
            }
        }
    }

    public void strBuilderAppend(String s) {
        stringBuilder.append(s);
        stringBuilder.append(System.lineSeparator());
        if (stringBuilder.length() > STR_BUILDER_BATCH - 15) {
            flushStrBuilder();
        }
    }

    public void flushStrBuilder() {
        writeToFile(stringBuilder.toString());
        stringBuilder = new StringBuilder(STR_BUILDER_BATCH);
    }

    public void closeFileWriter() {
        try {
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
