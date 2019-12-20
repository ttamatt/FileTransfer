package com.tuhu;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class HandleInfo {

    public static void sendSerial(SocketChannel socket, Serializable serializable) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(serializable);
        oos.close();
        ByteBuffer wrap = ByteBuffer.allocate(1024);
        wrap.put(baos.toByteArray());
        System.out.println(wrap);
        wrap.flip();
        socket.write(wrap);
    }

    public static Serializable recvSerial(SocketChannel socket) throws IOException, ClassNotFoundException {
        ByteBuffer dataByteBuffer = ByteBuffer.allocate(1024);
        socket.read(dataByteBuffer);
        System.out.println(dataByteBuffer);
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(dataByteBuffer.array()));
        return (Serializable) ois.readObject();
    }
}
