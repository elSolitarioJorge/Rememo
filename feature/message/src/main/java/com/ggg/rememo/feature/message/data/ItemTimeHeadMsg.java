package com.ggg.rememo.feature.message.data;

public class ItemTimeHeadMsg extends ListItem {
    private String time;

    public ItemTimeHeadMsg() {
    }

    public ItemTimeHeadMsg(String time) {
        this.time = time;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Override
    public int getType() {
        return ListItem.TYPE_TIME_HEADER;
    }
}
