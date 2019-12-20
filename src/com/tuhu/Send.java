package com.tuhu;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class Send {
    void handleSend(String filePath, String targetIp) throws IOException {
        if ("quit".equals(filePath)) {
            System.exit(0);
        } else {
            RandomAccessFile randomAccessFile = new RandomAccessFile(filePath, "r");
            Long fileSize = randomAccessFile.getChannel().size();
            int threadNum = Runtime.getRuntime().availableProcessors() * 2;
            ExecutorService executor = Executors.newFixedThreadPool(threadNum);
            Long blockSize = fileSize / threadNum;
            for (long i = 0L; i < threadNum - 1; i++) {
                Long index = i;
                executor.submit(
                        () -> sendFile(filePath, targetIp, index * blockSize, (index + 1) * blockSize)
                );
            }
            executor.submit(() -> sendFile(filePath, targetIp, (threadNum - 1) * blockSize, fileSize));
        }

    }

    void sendFile(String fileLocation, String targetIp, Long startLocation, Long endLocation) {
        try {
            RandomAccessFile randomAccessFile = new RandomAccessFile(fileLocation, "r");
            FileChannel fileChannel = randomAccessFile.getChannel();
            InetSocketAddress inetSocketAddress = new InetSocketAddress(targetIp, Config.TRANSFER_PORT);
            SocketChannel socketChannel = SocketChannel.open();
            socketChannel.connect(inetSocketAddress);
            beforeSend(socketChannel, targetIp, fileLocation, fileChannel.size(),startLocation);
            System.out.println(startLocation);
            System.out.println(endLocation - startLocation);
            System.out.println();
            Long sentIndx= fileChannel.transferTo(startLocation, endLocation - startLocation, socketChannel);
            System.out.println("Block has been sent:"+sentIndx);
            socketChannel.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
//    void sendFile(String fileLocation, String targetIp, Long StartLocation, Long EndLocation) {
//        RandomAccessFile randomAccessFile;
//        try {
//            randomAccessFile = new RandomAccessFile(fileLocation, "r");
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//            return;
//        }
//        FileChannel fileChannel = randomAccessFile.getChannel();
//        SocketChannel socketChannel;
//        InetSocketAddress inetSocketAddress = new InetSocketAddress(targetIp, Config.TRANSFER_PORT);
//        try {
//            Long startPosition = beforeSend(targetIp, fileLocation, fileChannel.size());
//            System.out.println("Sending file......");
//            if (startPosition.equals(fileChannel.size())) {
//                System.out.println("This file has been sent before, resending the file.....");
//                startPosition = 0L;
//            }
//            socketChannel = SocketChannel.open();
//            socketChannel.connect(inetSocketAddress);
//            fileChannel.transferTo(startPosition, fileChannel.size(), socketChannel);
//            System.out.println("File has been sent");
//            socketChannel.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    private void beforeSend(SocketChannel socketChannel, String targetIp, String fileLocation, Long fileSize , Long startPosition) {
        try {
//            MessageDigest md5 = MessageDigest.getInstance("MD5");
//            md5.update(Files.readAllBytes(Paths.get(fileLocation)));
//            byte[] fileMd5Digest = md5.digest();
//            String fileMd5 = Base64.getEncoder().encodeToString(fileMd5Digest);
            ClientInfo clientInfo = new ClientInfo();
//            clientInfo.setFileMd5(fileMd5);
            clientInfo.setFileName(Paths.get(fileLocation).getFileName().toString());
            clientInfo.setFileSize(fileSize);
            clientInfo.setStartPosition(startPosition);
            HandleInfo.sendSerial(socketChannel, clientInfo);
            ServerInfo serverInfo = (ServerInfo) HandleInfo.recvSerial(socketChannel);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
