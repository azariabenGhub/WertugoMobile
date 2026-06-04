package com.example.wertugo.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide; // PASTIKAN GLIDE TER-IMPORT
import com.example.wertugo.R;
import com.example.wertugo.activities.DetailTempatActivity;
import com.example.wertugo.api.ApiClient; // UNTUK MENGAMBIL IMAGE_BASE_URL

import java.util.List;
import java.util.Map;

public class UmkmAdapter extends RecyclerView.Adapter<UmkmAdapter.ViewHolder> {

    private final List<Map<String, Object>> umkmList;

    public UmkmAdapter(List<Map<String, Object>> umkmList) {
        this.umkmList = umkmList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_umkm, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> umkm = umkmList.get(position);

        // 1. Ekstrak Nama, Lokasi, Deskripsi
        Object rawNama = umkm.get("nama_usaha");
        holder.tvName.setText(rawNama != null ? String.valueOf(rawNama) : "Nama Tidak Diketahui");

        Object rawLokasi = umkm.get("lokasi");
        holder.tvLocation.setText(rawLokasi != null ? String.valueOf(rawLokasi) : "Lokasi Tidak Diketahui");

        Object rawDeskripsi = umkm.get("deskripsi");
        holder.tvDesc.setText(rawDeskripsi != null ? String.valueOf(rawDeskripsi) : "Tidak ada deskripsi");

        Object rawId = umkm.containsKey("_id") ? umkm.get("_id") : umkm.get("id");
        String placeId = rawId != null ? String.valueOf(rawId) : "";

        // ==========================================
        // 2. FITUR GAMBAR DENGAN GLIDE
        // ==========================================
        Object rawGambar = umkm.get("gambar");
        if (rawGambar != null && !String.valueOf(rawGambar).isEmpty() && !String.valueOf(rawGambar).equals("null")) {
            String imgStr = String.valueOf(rawGambar);
            // Tambahkan "storage/" secara dinamis
            String imageUrl = ApiClient.IMG_BASE_URL + imgStr;

            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .centerCrop()
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.stat_notify_error)
                    .into(holder.ivImage);
        } else {
            // Kalau gambar belum ada di database, beri ikon standar
            holder.ivImage.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        // ==========================================
        // 3. FITUR BADGE VERIFIKASI (CENTANG HIJAU)
        // ==========================================
        Object rawVerification = umkm.get("verification_status");
        if (rawVerification != null && rawVerification.equals("verified")) {
            holder.ivVerifiedBadge.setVisibility(View.VISIBLE);
        } else {
            holder.ivVerifiedBadge.setVisibility(View.GONE);
        }

        // ==========================================
        // 3.5 FITUR STATUS BUKA/TUTUP
        // ==========================================
        Object rawIsOpen = umkm.get("is_open");
        if (rawIsOpen != null) {
            String openStr = String.valueOf(rawIsOpen).toLowerCase();
            boolean isOpen = openStr.equals("true") || openStr.equals("1") || openStr.equals("1.0");
            holder.tvStatus.setVisibility(View.VISIBLE);
            if (isOpen) {
                holder.tvStatus.setText("BUKA");
                holder.tvStatus.setBackgroundResource(R.drawable.bg_green_rounded_filled);
            } else {
                holder.tvStatus.setText("TUTUP");
                holder.tvStatus.setBackgroundColor(android.graphics.Color.parseColor("#D32F2F"));
            }
        } else {
            holder.tvStatus.setVisibility(View.GONE);
        }

        // 4. Aksi Klik
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), DetailTempatActivity.class);
            intent.putExtra("PLACE_ID", placeId);
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return umkmList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvLocation, tvDesc, tvStatus;
        ImageView ivImage, ivVerifiedBadge; // Deklarasikan ImageView-nya!

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_item_name);
            tvLocation = itemView.findViewById(R.id.tv_item_location);
            tvDesc = itemView.findViewById(R.id.tv_item_desc);
            tvStatus = itemView.findViewById(R.id.tv_item_status);

            // Sambungkan dengan ID di XML
            ivImage = itemView.findViewById(R.id.iv_item_image);
            ivVerifiedBadge = itemView.findViewById(R.id.iv_verified_badge);
        }
    }
}