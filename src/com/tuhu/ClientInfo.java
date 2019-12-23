package com.tuhu;

import java.io.Serializable;

public class ClientInfo implements Serializable {

    private Long startPosition;

    public Long getEndPosition() {
        return endPosition;
    }

    public void setEndPosition(Long endPosition) {
        this.endPosition = endPosition;
    }

    private Long endPosition;


    public Long getStartPosition() {
        return startPosition;
    }

    public void setStartPosition(Long startPosition) {
        this.startPosition = startPosition;
    }

}
