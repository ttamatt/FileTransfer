package com.tuhu;

import com.sun.scenario.Settings;

import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Receive extends Thread {

    @Override
    public void run() {
        try {
            FileRecord.getRecordMap();
            System.out.println("Input output file path:");
            Scanner scanner = new Scanner(System.in);
            String outputPath = scanner.nextLine();
            System.out.println("Server listening at:" + Config.TRANSFER_PORT);
            FileInfo fileInfo = getFileInfo();
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.socket().bind(new InetSocketAddress(Config.TRANSFER_PORT));
            ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
            handleReceive(executorService, serverSocketChannel, outputPath, fileInfo);
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.MINUTES);
            RecordInfo recordInfo = new RecordInfo();
            recordInfo.setBreakPointMap(FileRecord.recordMap);
            FileRecord.setRecordMap(recordInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private FileInfo getFileInfo() throws Exception {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress(Config.FILE_INFO_PORT));
        SocketChannel socketChannel = serverSocketChannel.accept();
        FileInfo fileInfo = (FileInfo) HandleInfo.recvSerial(socketChannel);
        ServerInfo serverInfo = new ServerInfo();
        serverInfo.setStartBlockList(FileRecord.getBlockList(fileInfo.getFileMd5()));
        HandleInfo.sendSerial(socketChannel, serverInfo);
        socketChannel.close();
        serverSocketChannel.close();
        return fileInfo;
    }

    private void handleReceive(ExecutorService executorService, ServerSocketChannel serverSocketChannel, String output, FileInfo fileInfo) throws Exception {
        Long start = System.currentTimeMillis();;
        while (serverSocketChannel.isOpen()) {
            Long now = System.currentTimeMillis();
            if(now-start> 5000) {
                executorService.shutdown();
                return;
            }
            System.out.println(now-start);
            SocketChannel socketChannel = serverSocketChannel.accept();
            if (socketChannel != null) {
                executorService.submit(() -> {
                    try {
                        ClientInfo clientInfo = (ClientInfo) HandleInfo.recvSerial(socketChannel);
                        RandomAccessFile randomAccessFile = new RandomAccessFile(output + '/' + fileInfo.getFileName(), "rw");
                        randomAccessFile.setLength(fileInfo.getFileSize());
                        FileChannel fileChannel = randomAccessFile.getChannel();
                        Long receiveIndex = fileChannel.transferFrom(socketChannel, clientInfo.getStartPosition(), 2147483648L);
                        RecordInfo.Block block = new RecordInfo.Block();
                        block.setStartPosition(clientInfo.getStartPosition() + receiveIndex);
                        block.setEndPosition(clientInfo.getEndPosition());
                        FileRecord.addBlock(fileInfo.getFileMd5(), block);
                        if (fileInfo.getFileSize().equals(clientInfo.getStartPosition() + receiveIndex)) {
                            System.out.println("END!!!!!!!!!!!!!!!!!");
                        }
                        socketChannel.close();
                        fileChannel.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                start = System.currentTimeMillis();
            }
        }

    }


//    private void handleReceive(ServerSocketChannel serverSocketChannel, Long startPosition, String outputPath) {
//        try {
//            FileRecord fileRecord = new FileRecord();
//            for (; ; ) {
//                SocketChannel socketChannel = serverSocketChannel.accept();
//                if (socketChannel != null) {
//                    System.out.println("File receiving......");
//                    RandomAccessFile randomAccessFile = new RandomAccessFile(outputPath + '/' + clientInfo.getFileName(), "rw");
//                    FileChannel fileChannel = randomAccessFile.getChannel();
//                    Long receiveIndex = fileChannel.transferFrom(socketChannel, startPosition, 2147483648L);
//                    //delete completed file
//                    if (clientInfo.getFileSize().equals(receiveIndex)) {
//                        Map<String, String> fileMap = fileRecord.getFileRecord();
//                        fileMap.remove(clientInfo.getFileMd5());
//                        fileRecord.recordMap(fileMap);
//                    }else{
//                        fileRecord.addRecord(clientInfo.getFileMd5(), String.valueOf(receiveIndex));
//                    }
//                    fileChannel.close();
//                    serverSocketChannel.close();
//                    System.out.println("File received");
//                    return;
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//
//        }
//    }

//    private Long getStartPosition(String fileMd5) {
//        FileRecord fileRecord = new FileRecord();
//        Map<String, String> fileMap = fileRecord.getFileRecord();
//        if (fileMap.get(fileMd5) == null) {
//            System.out.println("Start a new transferring");
//            return 0L;
//        } else {
//            Long startPosition = Long.parseLong(fileMap.get(fileMd5));
//            if (startPosition.equals(clientInfo.getFileSize())) {
//                System.out.println("This file has been sent before, resending the file.....");
//                return 0L;
//            }
////             System.out.println("Transferring resume at:" + startPosition);
//            System.out.println("File has been sent" + startPosition / clientInfo.getFileSize());
//            return startPosition;
//        }
//    }

//    private ClientInfo getClientInfo() {
//        try {
//            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
//            serverSocketChannel.socket().bind(new InetSocketAddress(Config.MSG_PORT));
//            SocketChannel socketChannel = serverSocketChannel.accept();
//            ClientInfo clientInfo = (ClientInfo) HandleInfo.recvSerial(socketChannel);
////            System.out.println("File MD5 checksum:" + clientInfo.getFileMd5());
//            //prepare serverInfo
//            ServerInfo serverInfo = new ServerInfo();
//            FileRecord fileRecord = new FileRecord();
//            Map<String, String> map = fileRecord.getFileRecord();
//            if (map.get(clientInfo.getFileMd5()) != null) {
//                serverInfo.setAcceptedLocation(Long.parseLong(map.get(clientInfo.getFileMd5())));
//            } else {
//                serverInfo.setAcceptedLocation(0L);
//            }
//            HandleInfo.sendSerial(socketChannel, serverInfo);
//            socketChannel.close();
//            serverSocketChannel.close();
//            return clientInfo;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//    }
}
