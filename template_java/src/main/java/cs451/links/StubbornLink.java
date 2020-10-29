package cs451.links;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

public class StubbornLink extends Link{
    FairLossLink fairLossLink;

    public StubbornLink(int port) {
        super(port);
        fairLossLink = new FairLossLink(port);
    }

    public void stubbornSend(String message, String destIp, int destPort) {
        while(true) {
            fairLossLink.fairLossSend(message, destIp, destPort);
        }
    }

    public byte[] stubbornDeliver() {
        return fairLossLink.fairLossDeliver();
    }

}
