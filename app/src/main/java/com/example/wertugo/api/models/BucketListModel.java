package com.example.wertugo.api.models;

import com.google.gson.annotations.SerializedName;

public class BucketListModel {

    @SerializedName(value = "_id", alternate = {"id"})
    private String id;

    @SerializedName("title")
    private String namaBucket;

    @SerializedName("member_count")
    private int totalMember;

    @SerializedName("tempat_count")
    private int totalTempat;

    // FIX: Penulisan Java yang benar (private String)
    @SerializedName("gambar")
    private String gambar;

    // FIX: Tambahkan 'gambar' ke dalam konstruktor
    public BucketListModel(String id, String namaBucket, int totalMember, int totalTempat, String gambar) {
        this.id = id;
        this.namaBucket = namaBucket;
        this.totalMember = totalMember;
        this.totalTempat = totalTempat;
        this.gambar = gambar;
    }

    // Getter
    public String getId() {
        return id;
    }

    public String getNamaBucket() {
        return namaBucket;
    }

    public int getTotalMember() {
        return totalMember;
    }

    public int getTotalTempat() {
        return totalTempat;
    }

    // FIX: Tambahkan Getter untuk gambar
    public String getGambar() {
        return gambar;
    }
}