package cs451.links;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 * Implements FairLossLink with UDP.
 */
public class FairLossLink {
    private final byte[] sendBuffer;
    private final byte[] receiveBuffer;
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
}
