package com.example.wertugo.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wertugo.R;
import com.example.wertugo.activities.DetailTempatActivity; // Pastikan Activity ini sudah kamu buat agar tidak merah!

import java.util.List;
import java.util.Map;

public class UmkmAdapter extends RecyclerView.Adapter<UmkmAdapter.ViewHolder> {

    // List untuk menampung array "rekomendasi" dari JSON
    private final List<Map<String, Object>> umkmList;

    public UmkmAdapter(List<Map<String, Object>> umkmList) {
        this.umkmList = umkmList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Menyambungkan dengan file XML item_umkm
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_umkm, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> umkm = umkmList.get(position);

        // 1. Ekstrak Nama Usaha
        Object rawNama = umkm.get("nama_usaha");
        holder.tvName.setText(rawNama != null ? String.valueOf(rawNama) : "Nama Tidak Diketahui");

        // 2. Ekstrak Lokasi
        Object rawLokasi = umkm.get("lokasi");
        holder.tvLocation.setText(rawLokasi != null ? String.valueOf(rawLokasi) : "Lokasi Tidak Diketahui");

        // 3. Ekstrak Deskripsi
        Object rawDeskripsi = umkm.get("deskripsi");
        holder.tvDesc.setText(rawDeskripsi != null ? String.valueOf(rawDeskripsi) : "Tidak ada deskripsi");

        // 4. Ekstrak ID Tempat (Antisipasi '_id' dari MongoDB atau 'id' bawaan Laravel)
        Object rawId = umkm.containsKey("_id") ? umkm.get("_id") : umkm.get("id");
        String placeId = rawId != null ? String.valueOf(rawId) : "";

        // 5. Buat seluruh kotak item (itemView) bisa diklik
        holder.itemView.setOnClickListener(v -> {
            // Arahkan ke halaman DetailTempatActivity
            Intent intent = new Intent(v.getContext(), DetailTempatActivity.class);

            // Bawa ID tempat ini layaknya koper
            intent.putExtra("PLACE_ID", placeId);

            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return umkmList.size();
    }

    // Class untuk mengenali ID TextView di dalam item_umkm.xml
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvLocation, tvDesc;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_item_name);
            tvLocation = itemView.findViewById(R.id.tv_item_location);
            tvDesc = itemView.findViewById(R.id.tv_item_desc);
        }
    }
}