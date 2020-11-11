package cs451;

import cs451.broadcast.BestEffortBroadcast;
import cs451.broadcast.Broadcaster;
import cs451.broadcast.FifoBroadcast;
import cs451.broadcast.UniformReliableBroadcast;
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

    public Process(List<Host> hosts, String outputFile, Host myHost, Coordinator coordinator, int messages) {
        this.outputFile = outputFile;
        broadcaster = new FifoBroadcast(this, hosts, myHost);
        this.coordinator = coordinator;
        this.myHost = myHost;
        this.messages = messages;
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

    private void run(){
        coordinator.waitOnBarrier();
        for (int i = 1; i<=messages; i++) {
            broadcaster.broadcast(new Message(i, myHost.getId() + " " + i, myHost, myHost));
            writeToFile(outputFile, "b " + i);
        }
        coordinator.finishedBroadcasting();
    }

    @Override
    public void deliver(Message message) {
        writeToFile(outputFile, "d " + message.getContent());
    }
}
