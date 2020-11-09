package cs451.links;

import cs451.Receiver;
import cs451.messages.Message;
import java.io.IOException;
import java.net.*;
import java.util.Arrays;

/**
 * Implements FairLossLink with UDP.
 */
public class FairLossLink implements Runnable{
    private byte[] sendBuffer;
    private byte[] receiveBuffer;
    private DatagramSocket socket;
    private final int port;
    private static final int BUFFER_SIZE = 512;
    private final Receiver receiver;

    public FairLossLink(Receiver receiver, int port) {
        this.receiver = receiver;
        this.sendBuffer = new byte[BUFFER_SIZE];
        this.receiveBuffer = new byte[BUFFER_SIZE];
        this.port = port;
        try {
            this.socket = new DatagramSocket(port);
        } catch (SocketException e) {
            System.out.println("An error occurred when creating socket.");
            e.printStackTrace();
        }
    }

    public void send(Message message) {
        try {
            sendBuffer = (Arrays.copyOf(message.serialize(), BUFFER_SIZE));
        } catch (IOException e) {
            e.printStackTrace();
        }
        DatagramPacket myPacket;
        try {
            myPacket = new DatagramPacket(sendBuffer, sendBuffer.length, InetAddress.getByName(message.getDest().getIp()), message.getDest().getPort());
            socket.send(myPacket);
        } catch (UnknownHostException e){
            System.out.println("Unresolvable IP address.");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("An error occurred when sending packet.");
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("An error occurred");
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (true) {
            DatagramPacket packet_receive = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            try {
                socket.receive(packet_receive);
                receiver.deliver(Message.deserialize(receiveBuffer));
            } catch (IOException e) {
                System.out.println("An error occurred when receiving packet.");
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
