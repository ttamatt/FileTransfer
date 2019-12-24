package com.tuhu;

import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
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
            ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
            Long startTime = System.currentTimeMillis();
            handleReceive(executorService, serverSocketChannel, outputPath, fileInfo);
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.MINUTES);
            RecordInfo recordInfo = new RecordInfo();
            recordInfo.setBreakPointMap(FileRecord.recordMap);
            FileRecord.setRecordMap(recordInfo, fileInfo.getFileMd5());
            Long endTime = System.currentTimeMillis();
            double elapsedTime = (double) (endTime - startTime) / 1000;
            System.out.println("Elapsed time:" + elapsedTime + "s");
            System.out.println((fileInfo.getFileSize() / 1024 / 1024) / elapsedTime + "M/S");
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
        Long start = System.currentTimeMillis();
        while (serverSocketChannel.isOpen()) {
            Long now = System.currentTimeMillis();
            if (now - start > 5000) {
                executorService.shutdown();
                return;
            }
            SocketChannel socketChannel = serverSocketChannel.accept();
            if (socketChannel != null) {
                executorService.submit(() -> {
                    try {
                        ClientInfo clientInfo = (ClientInfo) HandleInfo.recvSerial(socketChannel);
                        RandomAccessFile randomAccessFile = new RandomAccessFile(output + '/' + fileInfo.getFileName(), "rw");
                        randomAccessFile.setLength(fileInfo.getFileSize());
                        FileChannel fileChannel = randomAccessFile.getChannel();
                        Long receiveIndex = 0L;
                        System.out.println("Receiving......");
                        while (receiveIndex < clientInfo.getEndPosition() - clientInfo.getStartPosition()) {
                            receiveIndex += fileChannel.transferFrom(socketChannel, clientInfo.getStartPosition(), 2147483648L);
                        }
//                        System.out.println("start:" + clientInfo.getStartPosition() + "end:" + clientInfo.getEndPosition() + "%:" + receiveIndex
//                        / (clientInfo.getEndPosition() - clientInfo.getStartPosition()));
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
}
