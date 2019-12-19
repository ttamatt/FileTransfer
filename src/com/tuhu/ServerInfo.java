package com.tuhu;

import java.io.Serializable;

public class ServerInfo implements Serializable {
    private Long acceptedLocation;

    public void setAcceptedLocation(Long acceptedLocation) {
        this.acceptedLocation = acceptedLocation;
    }

    public Long getAcceptedLocation() {
        return acceptedLocation;
    }
}
