package cs451.messages;

import cs451.Host;

import java.io.*;
import java.util.Objects;

public class Message implements Serializable {
    private long id;
    private String content;
    private Host sender;
    private Host originalSender;

    public Message(long id, String content, Host sender, Host originalSender) {
        this.id = id;
        this.content = content;
        this.sender = sender;
        this.originalSender = originalSender;
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
                Objects.equals(content, message.content) &&
                Objects.equals(sender, message.sender); /*&&
                Objects.equals(originalSender, message.originalSender);*/
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, content, sender, originalSender);
    }

    @Override
    public String toString() {
        return "{"+id+", "+content+", "+originalSender+", "+sender+"}";
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

    public Host getSender() {
        return sender;
    }

    public void setSender(Host sender) {
        this.sender = sender;
    }

    public Host getOriginalSender() {
        return originalSender;
    }

    public void setOriginalSender(Host dest) {
        this.originalSender = dest;
    }
}
