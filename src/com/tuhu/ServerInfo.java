package com.tuhu;

import java.io.Serializable;
import java.util.List;

public class ServerInfo implements Serializable {
    private List<RecordInfo.Block> startBlockList;

    public List<RecordInfo.Block> getStartBlockList() {
        return startBlockList;
    }

    public void setStartBlockList(List<RecordInfo.Block> startBlockList) {
        this.startBlockList = startBlockList;
    }
}
