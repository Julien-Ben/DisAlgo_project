package cs451.messages;

import cs451.Host;

import java.io.*;
import java.util.Objects;

public class MessageVC extends Message implements Serializable {
    long[] clock;
    public MessageVC(long id, String content, cs451.Host sender, Host originalSender, long[] clock){
        super(id, content, sender, originalSender);
        this.clock = clock;
    }

    public MessageVC(Message message, long[] clock) {
        super(message.getId(), message.getContent(), message.getSender(), message.getOriginalSender());
        this.clock = clock;
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
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
