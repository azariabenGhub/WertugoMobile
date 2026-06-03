package com.example.wertugo.api.models;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class BerandaResponse {
    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private List<UmkmItem> data;

    @SerializedName("meta")
    private com.example.wertugo.models.MetaPaginasi meta;

    // TODO: Generate Getter
    public String getMessage() { return message; }
    public List<UmkmItem> getData() { return data; }
    public com.example.wertugo.models.MetaPaginasi getMeta() { return meta; }
}