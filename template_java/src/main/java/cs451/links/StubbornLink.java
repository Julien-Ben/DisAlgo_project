package cs451.links;

public class StubbornLink implements Link{
    FairLossLink fairLossLink;

    public StubbornLink(int port) {
        fairLossLink = new FairLossLink(port);
    }

    public void send(String message, String destIp, int destPort) {
        while(true) {
            fairLossLink.send(message, destIp, destPort);
        }
    }

    public byte[] deliver() {
        return fairLossLink.deliver();
    }

}
