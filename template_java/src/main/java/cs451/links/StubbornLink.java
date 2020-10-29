package cs451.links;

import cs451.Message;

import java.util.ArrayList;
import java.util.Optional;

public class StubbornLink implements Link, Runnable{
    FairLossLink fairLossLink;
    ArrayList<Message> buffer;

    public StubbornLink(int port) {
        fairLossLink = new FairLossLink(port);
        buffer = new ArrayList<>();
    }

    public void run() {
        while (true) {
            deliver();
            for (Message m : buffer) {
                fairLossLink.send(m);
            }
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                System.out.print("Thread interrupted");
                e.printStackTrace();
            }
        }
    }

    public void send(Message message) {
        buffer.add(message);
    }

    public Optional<Message> deliver() {
        return fairLossLink.deliver();

    }
}
