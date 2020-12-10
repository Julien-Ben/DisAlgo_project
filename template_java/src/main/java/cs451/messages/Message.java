package cs451.messages;

import cs451.Host;

import java.io.*;
import java.util.Objects;

public class Message implements Serializable {
    private long id;
    private String content;
    private int senderId;
    private int originalSenderId;
    private final long[] clock;

    public Message(long id, String content, int senderId, int originalSenderId, long[] clock) {
        this.id = id;
        this.content = content;
        this.senderId = senderId;
        this.originalSenderId = originalSenderId;
        this.clock = clock;
    }

    public Message(Message m, int senderId) {
        this(m.getId(), m.getContent(), senderId, m.getOriginalSenderId(), m.getClock());
    }

    public Message(Message m, long[] clock) {
        this(m.getId(), m.getContent(), m.getSenderId(), m.getOriginalSenderId(), clock);
    }

    //StackOverflow https://stackoverflow.com/questions/3736058/java-object-to-byte-and-byte-to-object-converter-for-tokyo-cabinet/3736091
    public byte[] serialize() throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ObjectOutputStream os = new ObjectOutputStream(out);
            os.writeObject(this);
            return out.toByteArray();
        }
    }


    public static Message deserialize(byte[] data) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream in = new ByteArrayInputStream(data)) {
            ObjectInputStream is = new ObjectInputStream(in);
            return (Message)is.readObject();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return id == message.id &&
                //Objects.equals(content, message.content) &&
                Objects.equals(senderId, message.senderId) &&
                Objects.equals(originalSenderId, message.originalSenderId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, content, senderId, originalSenderId);
    }

    @Override
    public String toString() {
        return "{"+id+", "+content+", "+originalSenderId+", "+senderId+"}";
        //return "ID : " + id + " Content : " + content + "Original sender : "+originalSender + " Sender : " + sender;
    }

    public long getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getSenderId() {
        return senderId;
    }

    public int getOriginalSenderId() {
        return originalSenderId;
    }

    public long[] getClock() {
        return clock;
    }
}
