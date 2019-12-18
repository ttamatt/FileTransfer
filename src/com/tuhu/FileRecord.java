package com.tuhu;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class FileRecord {

    public void recordFile(String md5, String index) throws IOException {
        FileChannel fileChannel;
        try {
            RandomAccessFile randomAccessFile = new RandomAccessFile("./FileRecordText", "rw");
            fileChannel = randomAccessFile.getChannel();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }
        Map<String, String> map = getFileRecord();
        map.put(md5, index);
        ByteBuffer byteBuffer = ByteBuffer.wrap(map.toString().getBytes());
        fileChannel.write(byteBuffer);
    }

    public Map<String, String> getFileRecord() {
        try {
            RandomAccessFile randomAccessFile = new RandomAccessFile("./FileRecordText", "rw");
            FileChannel fileChannel = randomAccessFile.getChannel();
            ByteBuffer byteBuffer = ByteBuffer.allocate((int) fileChannel.size());
            fileChannel.read(byteBuffer);
            String fileContent = new String(byteBuffer.array(), StandardCharsets.UTF_8);
            return stringToMap(fileContent);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    private Map<String, String> stringToMap(String value) {
        value = value.substring(1, value.length() - 2);
        if(value.equals("")){
            return new HashMap<>();
        }
        String[] keyValuePairs = value.split(",", 2);
        Map<String, String> map = new HashMap<>();
        for (String pair : keyValuePairs) {
            String key = pair.substring(0, pair.lastIndexOf("="));
            String index = pair.substring(pair.lastIndexOf("=") + 1);
            map.put(key, index);
        }
        return map;
    }

}
