package com.tuhu.info;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class RecordInfo implements Serializable {

    private volatile Map<String, List<Block>> breakPointMap;

    public Map<String, List<Block>> getBreakPointMap() {
        return breakPointMap;
    }

    public void setBreakPointMap(Map<String, List<Block>> breakPointMap) {
        this.breakPointMap = breakPointMap;
    }


    public static class Block implements Serializable {

        private Long startPosition;

        private Long endPosition;

        public Long getStartPosition() {
            return startPosition;
        }

        public void setStartPosition(Long startPosition) {
            this.startPosition = startPosition;
        }

        public Long getEndPosition() {
            return endPosition;
        }

        public void setEndPosition(Long endPosition) {
            this.endPosition = endPosition;
        }
    }
}
