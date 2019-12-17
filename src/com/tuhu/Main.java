package com.tuhu;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        Receive receive = new Receive();
        receive.start();
        new Send().sendFile("/Users/matt/Downloads/123.jpg");
        receive.join();
    }

}
