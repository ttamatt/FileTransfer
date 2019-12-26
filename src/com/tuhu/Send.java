package com.tuhu;

import com.tuhu.info.ClientInfo;
import com.tuhu.info.FileInfo;
import com.tuhu.info.RecordInfo;
import com.tuhu.info.ServerInfo;
import com.tuhu.tool.CompressTool;
import com.tuhu.tool.HandleSerialTool;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

class Send {
    void handleSend(String filePath, String targetIp) throws Exception {
        if ("quit".equals(filePath)) {
            System.exit(0);
        } else {
            CompressTool.zipFile(filePath, filePath + ".zip");
            filePath = filePath + ".zip";
            RandomAccessFile randomAccessFile = new RandomAccessFile(filePath, "r");
            Long fileSize = randomAccessFile.getChannel().size();
            ServerInfo serverInfo = getFileRecord(filePath, targetIp, fileSize);
            List<RecordInfo.Block> blockList = serverInfo.getStartBlockList();
            //prepare thread pool
            int threadNum = Runtime.getRuntime().availableProcessors();
            ExecutorService executor = Executors.newFixedThreadPool(threadNum);
            //file resume from record
            if (blockList != null && !blockList.isEmpty()) {
                System.out.println("File transferred from breakpoint....");
                for (RecordInfo.Block block : blockList) {
                    Long start = block.getStartPosition();
                    Long end = block.getEndPosition();
                    String finalFilePath = filePath;
                    executor.submit(() -> sendFile(finalFilePath, targetIp, start, end));
                }
            } else {
                //start a new file
                String finalFilePath = filePath;
                Long blockSize = fileSize / threadNum;
                for (long i = 0L; i < threadNum - 1; i++) {
                    Long index = i;
                    executor.submit(
                            () -> sendFile(finalFilePath, targetIp, index * blockSize, (index + 1) * blockSize)
                    );
                }
                executor.submit(() -> sendFile(finalFilePath, targetIp, (threadNum - 1) * blockSize, fileSize));
            }
            executor.shutdown();
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MINUTES);
            new File(filePath).delete();
            System.out.println("transferring finished");
        }

    }

    void sendFile(String fileLocation, String targetIp, Long startLocation, Long endLocation) {
        try {
            RandomAccessFile randomAccessFile = new RandomAccessFile(fileLocation, "r");
            FileChannel fileChannel = randomAccessFile.getChannel();
            InetSocketAddress inetSocketAddress = new InetSocketAddress(targetIp, Config.TRANSFER_PORT);
            SocketChannel socketChannel = SocketChannel.open();
            socketChannel.connect(inetSocketAddress);
            sendClientInfo(socketChannel, startLocation, endLocation);
            fileChannel.transferTo(startLocation, endLocation - startLocation, socketChannel);
            socketChannel.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendClientInfo(SocketChannel socketChannel, Long startPosition, Long endPosition) {
        try {
            ClientInfo clientInfo = new ClientInfo();
            clientInfo.setStartPosition(startPosition);
            clientInfo.setEndPosition(endPosition);
            HandleSerialTool.sendSerial(socketChannel, clientInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ServerInfo getFileRecord(String fileLocation, String targetIp, Long fileSize) throws Exception {
        //generate md5
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        System.out.println("Generating file MD5.....");
        RandomAccessFile randomAccessFile = new RandomAccessFile(fileLocation, "r");
        Integer md5Size = 10485760;
        MappedByteBuffer mappedByteBuffer;
        if (fileSize > md5Size) {
            mappedByteBuffer = randomAccessFile.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, md5Size);
        } else {
            mappedByteBuffer = randomAccessFile.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, fileSize);
        }
        md5.update(mappedByteBuffer);
        byte[] fileMd5Digest = md5.digest();
        String fileMd5 = Base64.getEncoder().encodeToString(fileMd5Digest);
        InetSocketAddress inetSocketAddress = new InetSocketAddress(targetIp, Config.FILE_INFO_PORT);
        //send file info
        FileInfo fileInfo = new FileInfo();
        fileInfo.setFileMd5(fileMd5);
        fileInfo.setFileName(Paths.get(fileLocation).getFileName().toString());
        fileInfo.setFileSize(fileSize);
        SocketChannel socketChannel = SocketChannel.open(inetSocketAddress);
        HandleSerialTool.sendSerial(socketChannel, fileInfo);
        ServerInfo serverInfo = (ServerInfo) HandleSerialTool.recvSerial(socketChannel);
        socketChannel.close();
        return serverInfo;
    }
}
