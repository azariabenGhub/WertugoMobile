package com.example.wertugo.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.wertugo.R;
import com.example.wertugo.api.models.UmkmItem;

import java.util.ArrayList;
import java.util.List;

public class UmkmAdapter extends RecyclerView.Adapter<UmkmAdapter.UmkmViewHolder> {

    // List untuk menampung data UMKM dari API
    private List<UmkmItem> umkmList = new ArrayList<>();

    // Method ini dipanggil dari HomeUserActivity saat data API berhasil diambil
    public void setUmkmList(List<UmkmItem> umkmList) {
        this.umkmList = umkmList;
        notifyDataSetChanged(); // Memberi tahu RecyclerView untuk me-refresh tampilan
    }

    @NonNull
    @Override
    public UmkmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Menghubungkan adapter dengan layout item_umkm.xml
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_umkm, parent, false);
        return new UmkmViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UmkmViewHolder holder, int position) {
        // Mengambil data spesifik berdasarkan posisinya di list
        UmkmItem umkm = umkmList.get(position);

        // 1. Set Teks Nama dan Lokasi
        holder.tvNamaUsaha.setText(umkm.getNamaUsaha());
        holder.tvLokasi.setText(umkm.getLokasi());

        // 2. Logika Badge Verified
        if (umkm.isVerified()) {
            holder.ivVerified.setVisibility(View.VISIBLE);
        } else {
            holder.ivVerified.setVisibility(View.GONE);
        }

        // 3. Load Gambar menggunakan Glide
        // CATATAN: Sesuaikan base URL ini dengan URL penyimpanan Laravel-mu
        // Jika endpoint API kamu sudah mengembalikan URL gambar secara utuh (http://...),
        // kamu cukup menggunakan umkm.getThumbnail() saja.
        String imageUrl = umkm.getThumbnail();
        if (imageUrl != null && !imageUrl.startsWith("http")) {
            imageUrl = "http://10.0.2.2:8000/storage/" + imageUrl;
        }

        Glide.with(holder.itemView.getContext())
                .load(imageUrl)
                .placeholder(android.R.color.darker_gray) // Warna abu-abu saat gambar sedang loading
                .error(android.R.color.holo_red_light) // Warna merah jika gambar gagal diload
                .centerCrop()
                .into(holder.ivThumbnail);
    }

    @Override
    public int getItemCount() {
        // Mengembalikan jumlah total data di dalam list
        return umkmList != null ? umkmList.size() : 0;
    }

    // Inner class untuk inisialisasi komponen UI dari item_umkm.xml
    public static class UmkmViewHolder extends RecyclerView.ViewHolder {
        TextView tvNamaUsaha, tvLokasi;
        ImageView ivThumbnail, ivVerified;

        public UmkmViewHolder(@NonNull View itemView) {
            super(itemView);
            // Sesuaikan ID ini dengan ID yang ada di file item_umkm.xml
            tvNamaUsaha = itemView.findViewById(R.id.tvNamaUsaha);
            tvLokasi = itemView.findViewById(R.id.tvLokasi);
            ivThumbnail = itemView.findViewById(R.id.ivThumbnail);
            ivVerified = itemView.findViewById(R.id.ivVerified);
        }
    }
}