package com.ggg.rememo.feature.message.data;

public abstract class ListItem {
    public static final int TYPE_SYSTEM = 0;
    public static final int TYPE_TIME_HEADER = 1;
    public static final int TYPE_COMMENT = 2;
    public static final int TYPE_LIKE = 3;

    public abstract int getType();
}
