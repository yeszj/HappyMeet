package cn.happy.beautyface.bean;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;


public class FilterItem implements LinkageEntity {

    public String name;
    public Bitmap icon;
    public String iconPath;
    public String model;
    public boolean selected;

    public FilterItem(String name, Bitmap icon, String modelName) {
        this.name = name;
        this.icon = icon;
        this.model = modelName;
    }

    public FilterItem(String name, String iconPath, String modelName) {
        this.name = name;
        this.iconPath = iconPath;
        this.model = modelName;
    }

    @NotNull
    @Override
    public StickerState getState() {
        return StickerState.DONE_STATE;
    }

    @Override
    public void setState(@NotNull StickerState state) {

    }

    @Override
    public void setPath(@NotNull String path) {
        this.model = path;
    }

    @NonNull
    @Override
    public String getPkgUrl() {
        return "";
    }
}
