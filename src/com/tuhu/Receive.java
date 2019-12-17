package com.tuhu;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class Receive extends Thread {

    @Override
    public void run() {
        handleReceive();
    }

    synchronized private void handleReceive() {
        try {
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.socket().bind(new InetSocketAddress(Config.port));
            serverSocketChannel.configureBlocking(false);
            for (; ; ) {
                SocketChannel socketChannel = serverSocketChannel.accept();
                if (socketChannel != null) {
                    System.out.println("File receiving......");
                    FileChannel fileChannel = new RandomAccessFile(Config.outPutLocation, "rw").getChannel();
                    fileChannel.transferFrom(socketChannel, 0, 1000000000);
                    fileChannel.close();
                    serverSocketChannel.close();
                    System.out.println("File received");
                    return;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
