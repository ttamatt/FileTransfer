package com.tuhu;

import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

class Send {


    void handleSend() {
        for (; ; ) {
            System.out.println("Input file path('quit' to exit):");
            Scanner scanner = new Scanner(System.in);
            String filePath = scanner.nextLine();
            if ("quit".equals(filePath)) {
                return;
            } else {
                new Send().sendFile(filePath);
            }
        }
    }

    synchronized void sendFile(String fileLocation) {
        System.out.println("Sending file......");
        RandomAccessFile randomAccessFile;
        try {
            randomAccessFile = new RandomAccessFile(fileLocation, "rw");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }
        FileChannel fileChannel = randomAccessFile.getChannel();
        InetSocketAddress inetSocketAddress = new InetSocketAddress(Config.sendIp, Config.port);
        SocketChannel socketChannel;
        try {
            socketChannel = SocketChannel.open();
            socketChannel.connect(inetSocketAddress);
            fileChannel.transferTo(0, fileChannel.size(), socketChannel);
            System.out.println("File has been sent");
            socketChannel.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
