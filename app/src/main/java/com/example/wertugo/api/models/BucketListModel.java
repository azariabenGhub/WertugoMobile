package com.example.wertugo.api.models;

import com.google.gson.annotations.SerializedName;

public class BucketListModel {

    @SerializedName("_id") // MongoDB menggunakan _id
    private String id;

    @SerializedName("title") // Menyesuaikan dengan kolom di Laravel
    private String namaBucket;

    @SerializedName("total_member")
    private int totalMember;

    @SerializedName("total_tempat")
    private int totalTempat;

    public BucketListModel(String id, String namaBucket, int totalMember, int totalTempat) {
        this.id = id;
        this.namaBucket = namaBucket;
        this.totalMember = totalMember;
        this.totalTempat = totalTempat;
    }

    public String getId() { return id; }
    public String getNamaBucket() { return namaBucket; }
    public int getTotalMember() { return totalMember; }
    public int getTotalTempat() { return totalTempat; }
}