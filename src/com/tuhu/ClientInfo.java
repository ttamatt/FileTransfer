package com.tuhu;

import java.io.Serializable;

public class ClientInfo implements Serializable {

    private Long fileSize;

    private String fileName;

    private String fileMd5;

    public Long getFileSize() {
        return fileSize;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileMd5() {
        return fileMd5;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setFileMd5(String fileMd5) {
        this.fileMd5 = fileMd5;
    }
}
