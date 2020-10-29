package cs451.links;

public interface Link {
    void send(String message, String destIp, int destPort);
    byte[] deliver();

}
