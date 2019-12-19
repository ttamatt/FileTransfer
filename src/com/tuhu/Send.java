package com.tuhu;

import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.Base64;
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

    void sendFile(String fileLocation) {
        RandomAccessFile randomAccessFile;
        try {
            randomAccessFile = new RandomAccessFile(fileLocation, "r");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }
        FileChannel fileChannel = randomAccessFile.getChannel();
        SocketChannel socketChannel;
        InetSocketAddress inetSocketAddress = new InetSocketAddress(Config.sendIp, Config.TRANSFER_PORT);
        try {
            Long startPosition = beforeSend(fileLocation,fileChannel.size());
            System.out.println("Sending file......");
            if (startPosition.equals(fileChannel.size())) {
                System.out.println("This file has been sent before, resending the file.....");
                startPosition = 0L;
            }
            socketChannel = SocketChannel.open();
            socketChannel.connect(inetSocketAddress);
            fileChannel.transferTo(startPosition, fileChannel.size(), socketChannel);
            System.out.println("File has been sent");
            socketChannel.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Long beforeSend(String fileLocation,Long fileSize) {
        try {
            InetSocketAddress inetSocketAddress = new InetSocketAddress(Config.sendIp, Config.MSG_PORT);
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(Files.readAllBytes(Paths.get(fileLocation)));
            byte[] fileMd5Digest = md5.digest();
            String fileMd5 = Base64.getEncoder().encodeToString(fileMd5Digest);
            ClientInfo clientInfo = new ClientInfo();
            clientInfo.setFileMd5(fileMd5);
            clientInfo.setFileSize(fileSize);
            SocketChannel socketChannel = SocketChannel.open();
            socketChannel.connect(inetSocketAddress);
            HandleInfo.sendSerial(socketChannel, clientInfo);
            ServerInfo serverInfo = (ServerInfo) HandleInfo.recvSerial(socketChannel);
            return serverInfo.getAcceptedLocation();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0L;
    }
}
