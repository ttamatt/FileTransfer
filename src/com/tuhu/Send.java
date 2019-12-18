package com.tuhu;

import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
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
        InetSocketAddress inetSocketAddress = new InetSocketAddress(Config.sendIp, Config.port);
        Long startPosition = getStartPositionFromServer(inetSocketAddress, fileLocation);
        System.out.println("Sending file......");
        RandomAccessFile randomAccessFile;
        try {
            randomAccessFile = new RandomAccessFile(fileLocation, "r");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }
        FileChannel fileChannel = randomAccessFile.getChannel();
        SocketChannel socketChannel;
        try {
            if(startPosition.equals(fileChannel.size())){
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

    private Long getStartPositionFromServer(InetSocketAddress inetSocketAddress, String fileLocation) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(Files.readAllBytes(Paths.get(fileLocation)));
            byte[] fileMd5Digest = md5.digest();
            SocketChannel socketChannel = SocketChannel.open();
            socketChannel.connect(inetSocketAddress);
            socketChannel.write(ByteBuffer.wrap(fileMd5Digest));
            ByteBuffer indexBuffer = ByteBuffer.allocate(1024);
            socketChannel.read(indexBuffer);
            byte[] indexBytes = new byte[indexBuffer.position()];
            indexBuffer.rewind();
            indexBuffer.get(indexBytes);
            socketChannel.close();
            Thread.sleep(1000);
            return Long.parseLong(new String(indexBytes, StandardCharsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0L;
    }
}
