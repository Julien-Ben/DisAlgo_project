package cs451;

import cs451.broadcast.Broadcaster;
import cs451.broadcast.FifoBroadcast;
import cs451.messages.Message;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Implements a basic process for this project. Contains the main information (hosts, port...)
 */
public class Process implements Receiver {
    private final String outputFile;
    private final Broadcaster broadcaster;
    private final Coordinator coordinator;
    private final Host myHost;
    private final int messages;
    private StringBuilder stringBuilder;
    private static final int STR_BUILDER_BATCH = 16384;
    private FileWriter fileWriter;

    public Process(List<Host> hosts, String outputFile, Host myHost, Coordinator coordinator, int messages) {
        this.outputFile = outputFile;
        broadcaster = new FifoBroadcast(this, hosts, myHost);
        this.coordinator = coordinator;
        this.myHost = myHost;
        this.messages = messages;
        this.stringBuilder = new StringBuilder(STR_BUILDER_BATCH);
        try {
            this.fileWriter = new FileWriter(outputFile, true);
        } catch (IOException e) {
            System.out.println("An error occurred when opening file.");
            e.printStackTrace();
        }
        run();
    }

    //TODO : move into a FileHelper
    public void writeToFile(String message) {
        try {
            //TODO : try with resource
            fileWriter.write(message);
        } catch (IOException e) {
            System.out.println("An error occurred when writing file.");
            e.printStackTrace();
        }
    }

    private void run(){
        coordinator.waitOnBarrier();
        for (int i = 1; i<=messages; i++) {
            broadcaster.broadcast(new Message(i, myHost.getId() + " " + i, myHost, myHost));
            strBuilderAppend( "b " + i);
        }
        coordinator.finishedBroadcasting();
    }

    @Override
    public void deliver(Message message) {
        strBuilderAppend("d " + message.getContent());
    }

    public void strBuilderAppend(String s) {
        stringBuilder.append(s);
        stringBuilder.append(System.lineSeparator());
        if (stringBuilder.length() > STR_BUILDER_BATCH - 1) {
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
