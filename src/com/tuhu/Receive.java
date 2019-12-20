package com.tuhu;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.Scanner;

public class Receive extends Thread {

    private ClientInfo clientInfo;

    @Override
    public void run() {
        try {
            System.out.println("Input output file path:");
            Scanner scanner = new Scanner(System.in);
            String outputPath = scanner.nextLine();
            System.out.println("Server listening at:" + Config.TRANSFER_PORT);
//            clientInfo = getClientInfo();
//            Long startPosition = getStartPosition(clientInfo.getFileMd5());
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.socket().bind(new InetSocketAddress(Config.TRANSFER_PORT));
            serverSocketChannel.configureBlocking(false);
//            handleReceive(serverSocketChannel, startPosition, outputPath);
            handleReceive(serverSocketChannel, outputPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void handleReceive(ServerSocketChannel serverSocketChannel, String output) throws IOException, ClassNotFoundException {
        while (true) {

            SocketChannel socketChannel = serverSocketChannel.accept();

            if (socketChannel != null) {
                ClientInfo clientInfo = (ClientInfo) HandleInfo.recvSerial(socketChannel);
                ServerInfo serverInfo = new ServerInfo();
                serverInfo.setAcceptedLocation(1000000L);
                HandleInfo.sendSerial(socketChannel, serverInfo);
                RandomAccessFile randomAccessFile = new RandomAccessFile(output + '/' + clientInfo.getFileName(), "rw");
                randomAccessFile.setLength(clientInfo.getFileSize());
                FileChannel fileChannel = randomAccessFile.getChannel();
                System.out.println(clientInfo.getStartPosition());
                Long receIndex = fileChannel.transferFrom(socketChannel, clientInfo.getStartPosition(), 2147483648L);
                socketChannel.close();
                System.out.println(receIndex);
            }

//                    key.interestOps(SelectionKey.OP_WRITE);


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

    private Long getStartPosition(String fileMd5) {
        FileRecord fileRecord = new FileRecord();
        Map<String, String> fileMap = fileRecord.getFileRecord();
        if (fileMap.get(fileMd5) == null) {
            System.out.println("Start a new transferring");
            return 0L;
        } else {
            Long startPosition = Long.parseLong(fileMap.get(fileMd5));
            if (startPosition.equals(clientInfo.getFileSize())) {
                System.out.println("This file has been sent before, resending the file.....");
                return 0L;
            }
//             System.out.println("Transferring resume at:" + startPosition);
            System.out.println("File has been sent" + startPosition / clientInfo.getFileSize());
            return startPosition;
        }
    }

    private ClientInfo getClientInfo() {
        try {
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.socket().bind(new InetSocketAddress(Config.MSG_PORT));
            SocketChannel socketChannel = serverSocketChannel.accept();
            ClientInfo clientInfo = (ClientInfo) HandleInfo.recvSerial(socketChannel);
//            System.out.println("File MD5 checksum:" + clientInfo.getFileMd5());
            //prepare serverInfo
            ServerInfo serverInfo = new ServerInfo();
            FileRecord fileRecord = new FileRecord();
            Map<String, String> map = fileRecord.getFileRecord();
            if (map.get(clientInfo.getFileMd5()) != null) {
                serverInfo.setAcceptedLocation(Long.parseLong(map.get(clientInfo.getFileMd5())));
            } else {
                serverInfo.setAcceptedLocation(0L);
            }
            HandleInfo.sendSerial(socketChannel, serverInfo);
            socketChannel.close();
            serverSocketChannel.close();
            return clientInfo;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
