package com.example.wertugo.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wertugo.R;
import com.example.wertugo.activities.BucketPlaceDetailActivity;
import com.example.wertugo.api.models.BucketListPlaceModel;

import java.util.List;

public class BucketListPlaceAdapter extends RecyclerView.Adapter<BucketListPlaceAdapter.PlaceViewHolder> {

    private List<BucketListPlaceModel> placeList;
    private String bucketId; // Tambahan untuk menyimpan ID Room Bucket List
    private OnPlaceDeleteListener deleteListener;

    public interface OnPlaceDeleteListener {
        void onDeleteClick(BucketListPlaceModel place, int position);
    }

    // Konstruktor sekarang menerima bucketId juga
    public BucketListPlaceAdapter(List<BucketListPlaceModel> placeList, String bucketId, OnPlaceDeleteListener deleteListener) {
        this.placeList = placeList;
        this.bucketId = bucketId;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public PlaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bucket_place, parent, false);
        return new PlaceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceViewHolder holder, int position) {
        BucketListPlaceModel place = placeList.get(position);

        holder.tvName.setText(place.getNamaTempat());
        holder.tvLocation.setText(place.getLokasi() + " • " + place.getKategori());

        // Event saat tombol hapus (ikon tong sampah) diklik
        holder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDeleteClick(place, position);
            }
        });

        // Event saat KARTU TEMPAT diklik -> Pindah ke Halaman Detail & Rating
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), BucketPlaceDetailActivity.class);
            intent.putExtra("BUCKET_ID", bucketId);
            intent.putExtra("PLACE_ID", place.getId());
            intent.putExtra("PLACE_NAME", place.getNamaTempat());
            intent.putExtra("PLACE_LOCATION", place.getLokasi());
            intent.putExtra("PLACE_CATEGORY", place.getKategori());

            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return placeList.size();
    }

    public static class PlaceViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvLocation;
        ImageView btnDelete;

        public PlaceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_place_name);
            tvLocation = itemView.findViewById(R.id.tv_place_location);
            btnDelete = itemView.findViewById(R.id.btn_delete_place);
        }
    }
}