package com.tuhu;

import java.io.IOException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        Send send = new Send();
        Scanner scanner = new Scanner(System.in);
        System.out.println("Input 1 receive, 2 send:");
        switch (scanner.nextLine()) {
            case "1":
                Receive receive = new Receive();
                receive.start();
                receive.join();
                break;
            case "2":
                for (; ; ) {
                    System.out.println("Input targetIp:");
                    String targetIp = scanner.nextLine();
                    System.out.println("Input file path('quit' to exit):");
                    String filePath = scanner.nextLine();
                    try {
                        send.handleSend(filePath, targetIp);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

        }
    }

}
