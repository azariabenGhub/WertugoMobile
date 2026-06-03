package com.example.wertugo.api.models;
import com.google.gson.annotations.SerializedName;

public class UmkmItem {
    @SerializedName("id")
    private String id;

    @SerializedName("nama_usaha")
    private String namaUsaha;

    @SerializedName("lokasi")
    private String lokasi;

    @SerializedName("is_open")
    private boolean isOpen;

    @SerializedName("is_verified")
    private boolean isVerified;

    @SerializedName("thumbnail")
    private String thumbnail;

    // TODO: Generate Getter dan Setter (Alt+Insert di Android Studio)
    public String getId() { return id; }
    public String getNamaUsaha() { return namaUsaha; }
    public String getLokasi() { return lokasi; }
    public boolean isOpen() { return isOpen; }
    public boolean isVerified() { return isVerified; }
    public String getThumbnail() { return thumbnail; }
}