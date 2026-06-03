package com.example.wertugo.api.models;

public class BucketListPlaceModel {
    private String id;
    private String namaTempat;
    private String lokasi;
    private String kategori;

    public BucketListPlaceModel(String id, String namaTempat, String lokasi, String kategori) {
        this.id = id;
        this.namaTempat = namaTempat;
        this.lokasi = lokasi;
        this.kategori = kategori;
    }

    public String getId() { return id; }
    public String getNamaTempat() { return namaTempat; }
    public String getLokasi() { return lokasi; }
    public String getKategori() { return kategori; }
}