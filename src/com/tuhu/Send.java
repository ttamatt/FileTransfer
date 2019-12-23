package com.tuhu;

import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class Send {
    void handleSend(String filePath, String targetIp) throws Exception {
        if ("quit".equals(filePath)) {
            System.exit(0);
        } else {
            RandomAccessFile randomAccessFile = new RandomAccessFile(filePath, "r");
            Long fileSize = randomAccessFile.getChannel().size();
            int threadNum = Runtime.getRuntime().availableProcessors() * 2;
            ExecutorService executor = Executors.newFixedThreadPool(threadNum);
            ServerInfo serverInfo = getFileRecord(filePath, targetIp, fileSize);
            List<RecordInfo.Block> blockList = serverInfo.getStartBlockList();
            if (blockList != null) {
                System.out.println("File had a record");
                for (RecordInfo.Block block : blockList) {
                    Long start = block.getStartPosition();
                    Long end = block.getEndPosition();
                    executor.submit(() -> sendFile(filePath, targetIp, start, end));
                }
            } else {
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

    }

    void sendFile(String fileLocation, String targetIp, Long startLocation, Long endLocation) {
        try {
            RandomAccessFile randomAccessFile = new RandomAccessFile(fileLocation, "r");
            FileChannel fileChannel = randomAccessFile.getChannel();
            InetSocketAddress inetSocketAddress = new InetSocketAddress(targetIp, Config.TRANSFER_PORT);
            SocketChannel socketChannel = SocketChannel.open();
            socketChannel.connect(inetSocketAddress);
            sendClientInfo(socketChannel, startLocation,endLocation);
            fileChannel.transferTo(startLocation, endLocation - startLocation, socketChannel);
            socketChannel.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendClientInfo(SocketChannel socketChannel, Long startPosition,Long endPosition) {
        try {
            ClientInfo clientInfo = new ClientInfo();
            clientInfo.setStartPosition(startPosition);
            clientInfo.setEndPosition(endPosition);
            HandleInfo.sendSerial(socketChannel, clientInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ServerInfo getFileRecord(String fileLocation, String targetIp, Long fileSize) throws Exception {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.update(Files.readAllBytes(Paths.get(fileLocation)));
        byte[] fileMd5Digest = md5.digest();
        String fileMd5 = Base64.getEncoder().encodeToString(fileMd5Digest);
        InetSocketAddress inetSocketAddress = new InetSocketAddress(targetIp, Config.FILE_INFO_PORT);
        FileInfo fileInfo = new FileInfo();
        fileInfo.setFileMd5(fileMd5);
        fileInfo.setFileName(Paths.get(fileLocation).getFileName().toString());
        fileInfo.setFileSize(fileSize);
        SocketChannel socketChannel = SocketChannel.open(inetSocketAddress);
        HandleInfo.sendSerial(socketChannel, fileInfo);
        ServerInfo serverInfo = (ServerInfo) HandleInfo.recvSerial(socketChannel);
        socketChannel.close();
        return serverInfo;
    }
}
