package com.tuhu;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        Send send = new Send();
        Scanner scanner = new Scanner(System.in);
        System.out.println("Input 1 receive, 2 send:");
        switch (scanner.nextLine()) {
            //server mode
            case "1":
                for(;;){
                    System.out.println("Input output directory path:");
                    String outputPath = scanner.nextLine();
                    try{
                        Path path = Paths.get(outputPath);
                        if(!Files.isDirectory(path)){
                            System.out.println("Path can't found!");
                            continue;
                        }
                    }catch (Exception e){
                        System.out.println("Path format wrong!");
                        continue;
                    }
                    Receive receive = new Receive();
                    receive.run(outputPath);
                }
            case "2":
                //client mode
                for (; ; ) {
                    System.out.println("Input targetIp:");
                    String targetIp = scanner.nextLine();
                    if(!targetIp.matches(Config.IP_PATTERN)){
                        System.out.println("Ip format wrong!");
                        continue;
                    }
                    System.out.println("Input file path('quit' to exit):");
                    String filePath = scanner.nextLine();
                    try {
                        send.handleSend(filePath, targetIp);
                    } catch (Exception e) {
                        if(e instanceof FileNotFoundException){
                            System.out.println("Can't find file!");
                            continue;
                        }
                    }
                }

        }
    }

}
