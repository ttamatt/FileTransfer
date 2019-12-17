package com.tuhu;

import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

import java.io.IOException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        Send send = new Send();
        Receive receive = new Receive();
        receive.setDaemon(true);
        receive.start();
        System.out.println("Server listening at:"+Config.port);
        send.handleSend();
    }

}
