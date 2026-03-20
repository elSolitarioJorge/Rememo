package com.ggg.rememo.core.data.local.type;

import androidx.room.TypeConverter;

import com.ggg.rememo.core.data.model.entity.MemoryPhoto;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class Converters {
    private static final Gson gson = new Gson();

    @TypeConverter
    public static List<MemoryPhoto> fromPhotoString(String value) {
        if (value == null) return null;
        Type listType = new TypeToken<List<MemoryPhoto>>() {}.getType();
        return gson.fromJson(value, listType);
    }

    @TypeConverter
    public static String fromPhotoList(List<MemoryPhoto> list) {
        if (list == null) return null;
        return gson.toJson(list);
    }
}
