package com.ggg.rememo.feature.message.DataModule;

public class ItemSystemMsg extends ListItem {
    private int ImageResId;
    private String title;
    private String time;
    private String content;

    public ItemSystemMsg() {
    }

    public ItemSystemMsg(int ImageResId, String title, String time, String content) {
        this.ImageResId = ImageResId;
        this.title = title;
        this.time = time;
        this.content = content;
    }

    public int getImageResId() {
        return ImageResId;
    }

    public void setImageResId(int imageResId) {
        ImageResId = imageResId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public int getType() {
        return TYPE_SYSTEM;
    }
}
