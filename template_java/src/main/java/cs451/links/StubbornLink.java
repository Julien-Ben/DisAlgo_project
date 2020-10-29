package cs451.links;

public class StubbornLink{
    FairLossLink fairLossLink;

    public StubbornLink(int port) {
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
