package com.tuhu;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileRecord {

    static Map<String, List<RecordInfo.Block>> recordMap;

    static void setRecordMap(Serializable object) throws IOException {
        try {
            File file = new File("./FileRecordText");
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(file));
            objectOutputStream.writeObject(object);
            objectOutputStream.flush();
            objectOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    static void addBlock(String md5, RecordInfo.Block block) {
        if (recordMap.get(md5) == null) {
            List<RecordInfo.Block> list = new ArrayList<>();
            list.add(block);
            recordMap.put(md5, list);
        } else {
            recordMap.get(md5).add(block);
        }
    }

    static List<RecordInfo.Block> getBlockList(String md5) {
        return recordMap.get(md5);
    }

    static void getRecordMap() {
        try {
            File file = new File("./FileRecordText");
            ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(file));
            RecordInfo recordInfo = (RecordInfo) objectInputStream.readObject();
            objectInputStream.close();
            recordMap = recordInfo.getBreakPointMap();
        } catch (Exception e) {
            FileRecord.recordMap = new HashMap<>();
            return;
        }
    }
}
