package com.tuhu;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Base64;
import java.util.Map;

public class Receive extends Thread {

    boolean started = false;

    private String fileMd5;

    private Long startPosition;

    @Override
    public void run() {
        try {
            fileMd5 = getFileMd5FromClient();
            startPosition = getStartPosition(fileMd5);
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.socket().bind(new InetSocketAddress(Config.port));
            System.out.println("Server listening at:" + Config.port);
            started = true;
            serverSocketChannel.configureBlocking(false);
            handleReceive(serverSocketChannel, startPosition);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleReceive(ServerSocketChannel serverSocketChannel, Long startPosition) {
        try {
            FileRecord fileRecord = new FileRecord();
            for (; ; ) {
                SocketChannel socketChannel = serverSocketChannel.accept();
                if (socketChannel != null) {
                    System.out.println("File receiving......");
                    RandomAccessFile randomAccessFile = new RandomAccessFile(Config.outPutLocation, "rw");
                    FileChannel fileChannel = randomAccessFile.getChannel();
                    Long receiveIndex = fileChannel.transferFrom(socketChannel, startPosition, 2147483648L);
                    System.out.println("Receive startPosition:" + String.valueOf(receiveIndex));
                    fileRecord.recordFile(fileMd5, String.valueOf(receiveIndex));
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

    private Long getStartPosition(String fileMd5) {
        FileRecord fileRecord = new FileRecord();
        Map<String, String> fileMap = fileRecord.getFileRecord();
        if (fileMap.get(fileMd5) == null) {
            System.out.println("Start a new transferring");
            return 0L;
        } else {
            Long startPosition = Long.parseLong(fileMap.get(fileMd5));
            System.out.println("Transferring resume at:" + startPosition);
            return startPosition;
        }
    }

    private String getFileMd5FromClient() {
        try {
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.socket().bind(new InetSocketAddress(Config.port));
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
            System.out.println("Waiting for file MD5......");
            SocketChannel socketChannel = serverSocketChannel.accept();
            socketChannel.read(byteBuffer);
            FileRecord fileRecord = new FileRecord();
            Map<String, String> map = fileRecord.getFileRecord();
            byte[] md5Bytes = new byte[byteBuffer.position()];
            byteBuffer.rewind();
            byteBuffer.get(md5Bytes);
            String fileMd5 = Base64.getEncoder().encodeToString(md5Bytes);
            ByteBuffer startPositionBuffer;
            if (map.get(fileMd5) != null) {
                startPositionBuffer = ByteBuffer.wrap(map.get(fileMd5).getBytes());
            } else {
                startPositionBuffer = ByteBuffer.wrap("0".getBytes());
            }
            socketChannel.write(startPositionBuffer);
            socketChannel.close();
            serverSocketChannel.close();
            System.out.println("File MD5 checksum:" + fileMd5);
            return fileMd5;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
