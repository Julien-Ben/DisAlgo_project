package cs451.links;

import java.io.IOException;
import java.net.*;
import java.util.Arrays;

/**
 * Implements FairLossLink with UDP.
 */
public class FairLossLink {
    private byte[] sendBuffer;
    private byte[] receiveBuffer;
    private DatagramSocket socket;
    private final int port;
    private static final int BUFFER_SIZE = 256;

    public FairLossLink(int port) {
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

    public void fairLossSend(String message, String destIp, int destPort) {
        sendBuffer = (Arrays.copyOf(message.getBytes(), BUFFER_SIZE));
        DatagramPacket myPacket;
        try {
            myPacket = new DatagramPacket(sendBuffer, sendBuffer.length, InetAddress.getByName(destIp), destPort);
            socket.send(myPacket);
        } catch (UnknownHostException e){
            System.out.println("Unresolvable IP address.");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("An error occurred when sending packet.");
            e.printStackTrace();
        }

    }

    public byte[] fairLossDeliver() {
        DatagramPacket packet_receive = new DatagramPacket(receiveBuffer, receiveBuffer.length);
        while (true) {
            try {
                socket.receive(packet_receive);
                return receiveBuffer;
            } catch (IOException e) {
                System.out.println("An error occurred when receiving packet.");
                e.printStackTrace();
            }

        }
    }

}
