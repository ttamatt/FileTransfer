package com.tuhu.tool;

import com.tuhu.info.RecordInfo;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FileRecordTool {

    public static Map<String, List<RecordInfo.Block>> recordMap;

    public static void setRecordMap(RecordInfo recordInfo,String md5) throws IOException {
        try {
            File file = new File("./FileRecordText");

            if(!file.exists()) {
                file.createNewFile();
            }
            FileWriter fileWriter =new FileWriter(file);
            fileWriter.write("");
            fileWriter.flush();
            fileWriter.close();

            List<RecordInfo.Block> blockList = recordInfo.getBreakPointMap().get(md5).stream().filter(i->!i.getStartPosition().equals(i.getEndPosition())).collect(Collectors.toList());
            recordInfo.getBreakPointMap().remove(md5);
            recordInfo.getBreakPointMap().put(md5,blockList);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(file));
            objectOutputStream.writeObject(recordInfo);
            objectOutputStream.flush();
            objectOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void addBlock(String md5, RecordInfo.Block block) {
        if (recordMap.get(md5) == null) {
            List<RecordInfo.Block> list = new ArrayList<>();
            list.add(block);
            recordMap.put(md5, list);
        } else {
            recordMap.get(md5).add(block);
        }
    }

    public static List<RecordInfo.Block> getBlockList(String md5) {
        return recordMap.get(md5);
    }

    public static void getRecordMap() {
        try {
            File file = new File("./FileRecordText");
            ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(file));
            RecordInfo recordInfo = (RecordInfo) objectInputStream.readObject();
            objectInputStream.close();
            recordMap = recordInfo.getBreakPointMap();
        } catch (Exception e) {
            FileRecordTool.recordMap = new HashMap<>();
        }
    }
}
