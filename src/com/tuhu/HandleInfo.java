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
        ByteBuffer wrap = ByteBuffer.allocate(2048);
        wrap.put(baos.toByteArray());
        byte[] bytes= new byte[wrap.limit()-wrap.position()];
        wrap.put(bytes);
//        System.out.println("SP:"+ wrap.position());
        wrap.flip();
        socket.write(wrap);
    }

    public static Serializable recvSerial(SocketChannel socket) throws IOException, ClassNotFoundException {
        ByteBuffer dataByteBuffer = ByteBuffer.allocate(2048);
        socket.read(dataByteBuffer);
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(dataByteBuffer.array()));
//        System.out.println("RP:"+dataByteBuffer.position());
        return (Serializable) ois.readObject();
    }
}
