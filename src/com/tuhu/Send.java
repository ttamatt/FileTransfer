package com.tuhu;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;

class Send {

     void sendFile(String fileLocation) {
        System.out.println("send file");
        RandomAccessFile randomAccessFile;
        try {
            randomAccessFile = new RandomAccessFile(fileLocation, "rw");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }
        FileChannel fileChannel = randomAccessFile.getChannel();
        InetSocketAddress inetSocketAddress = new InetSocketAddress("127.0.0.1", Config.port);
        SocketChannel socketChannel;
        try {
            socketChannel = SocketChannel.open();
            socketChannel.connect(inetSocketAddress);
            fileChannel.transferTo(0, fileChannel.size(), socketChannel);
            socketChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
