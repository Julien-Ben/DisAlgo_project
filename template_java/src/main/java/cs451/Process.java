package cs451;

import cs451.broadcast.BestEffortBroadcast;
import cs451.messages.Message;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Implements a basic process for this project. Contains the main information (hosts, port...)
 */
public class Process implements Receiver {
    private final String outputFile;
    private final BestEffortBroadcast broadcaster;
    private final Coordinator coordinator;
    public Process(List<Host> hosts, int id, String outputFile, int port, Host myHost, Coordinator coordinator) {
        this.outputFile = outputFile;
        broadcaster = new BestEffortBroadcast(this, hosts, port, myHost, coordinator);
        this.coordinator = coordinator;
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
        int NBR_MESSAGES = 30;
        for (int i = 0; i<NBR_MESSAGES; i++) {
            broadcaster.broadcast(i);
        }
        coordinator.finishedBroadcasting();
    }

    @Override
    public void deliver(Message message) {
        writeToFile(outputFile, message.getContent());
    }
}
