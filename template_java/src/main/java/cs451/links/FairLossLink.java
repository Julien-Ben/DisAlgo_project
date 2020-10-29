package cs451.links;

import java.io.IOException;
import java.net.*;
import java.util.Arrays;

/**
 * Implements FairLossLink with UDP.
 */
public class FairLossLink extends Link {

    public FairLossLink(int port) {
        super(port);
    }

    public void fairLossSend(String message, String destIp, int destPort) {
        setSendBuffer(Arrays.copyOf(message.getBytes(), getBufferSize()));
        DatagramPacket myPacket;
        try {
            myPacket = new DatagramPacket(getSendBuffer(), getSendBuffer().length, InetAddress.getByName(destIp), destPort);
            getSocket().send(myPacket);
        } catch (UnknownHostException e){
            System.out.println("Unresolvable IP address.");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("An error occurred when sending packet.");
            e.printStackTrace();
        }

    }

    public byte[] fairLossDeliver() {
        DatagramPacket packet_receive = new DatagramPacket(getReceiveBuffer(), getReceiveBuffer().length);
        while (true) {
            try {
                getSocket().receive(packet_receive);
                return getReceiveBuffer();
            } catch (IOException e) {
                System.out.println("An error occurred when receiving packet.");
                e.printStackTrace();
            }

        }
    }

}
