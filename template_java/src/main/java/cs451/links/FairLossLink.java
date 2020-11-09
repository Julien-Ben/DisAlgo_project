package cs451.links;

import cs451.Deliverer;
import cs451.messages.Message;
import java.io.IOException;
import java.net.*;
import java.util.Arrays;
import java.util.Optional;

/**
 * Implements FairLossLink with UDP.
 */
public class FairLossLink implements Link, Runnable{
    private byte[] sendBuffer;
    private byte[] receiveBuffer;
    private DatagramSocket socket;
    private final int port;
    private static final int BUFFER_SIZE = 512;
    private static final int UDP_RECEIVE_TIMEOUT = 100;
    private final Deliverer deliverer;

    public FairLossLink(Deliverer deliverer, int port) {
        this.deliverer = deliverer;
        this.sendBuffer = new byte[BUFFER_SIZE];
        this.receiveBuffer = new byte[BUFFER_SIZE];
        this.port = port;
        try {
            this.socket = new DatagramSocket(port);
            socket.setSoTimeout(UDP_RECEIVE_TIMEOUT);
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

    public Optional<Message> deliver() {
        return Optional.empty();
    }

    @Override
    public void run() {
        while (true) {
            DatagramPacket packet_receive = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            try {
                socket.receive(packet_receive);
                deliverer.deliver(Message.deserialize(receiveBuffer));
            } catch (SocketTimeoutException e){
                return;
            } catch (IOException e) {
                System.out.println("An error occurred when receiving packet.");
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
