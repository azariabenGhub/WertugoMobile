package com.example.wertugo.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wertugo.R;
import com.example.wertugo.api.models.BucketListPlaceModel;

import java.util.List;

public class BucketListPlaceAdapter extends RecyclerView.Adapter<BucketListPlaceAdapter.PlaceViewHolder> {

    private List<BucketListPlaceModel> placeList;

    public BucketListPlaceAdapter(List<BucketListPlaceModel> placeList) {
        this.placeList = placeList;
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

        holder.btnDelete.setOnClickListener(v -> {
            // Nanti kita buat fungsi hapus ke API
            Toast.makeText(v.getContext(), "Fitur hapus menyusul!", Toast.LENGTH_SHORT).show();
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