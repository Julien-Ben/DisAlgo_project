package cs451.links;

import java.net.DatagramSocket;
import java.net.SocketException;

public abstract class Link {
    private byte[] sendBuffer;
    private byte[] receiveBuffer;
    private DatagramSocket socket;
    private final int port;
    private static final int BUFFER_SIZE = 256;

    public Link(int port) {
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
    public byte[] getSendBuffer() {
        return sendBuffer;
    }

    public void setSendBuffer(byte[] sendBuffer) {
        this.sendBuffer = sendBuffer;
    }

    public byte[] getReceiveBuffer() {
        return receiveBuffer;
    }

    public void setReceiveBuffer(byte[] receiveBuffer) {
        this.receiveBuffer = receiveBuffer;
    }

    public DatagramSocket getSocket() {
        return socket;
    }

    public void setSocket(DatagramSocket socket) {
        this.socket = socket;
    }

    public int getPort() {
        return port;
    }

    public static int getBufferSize() {
        return BUFFER_SIZE;
    }
}
