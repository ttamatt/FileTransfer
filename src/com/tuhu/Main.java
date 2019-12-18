package com.tuhu;

import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

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
//                for (;;) {
//                    if (receive.started) {
//                        System.out.println("Input 'quit' to exit");
//                        if ("quit".equals(scanner.nextLine())) {
//                            return;
//                        }
//                    }else {
//                        Thread.yield();
//                    }
//                }
                receive.join();
            case "2":
                send.handleSend();
        }
    }

}
