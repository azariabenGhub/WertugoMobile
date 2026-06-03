package com.example.wertugo.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wertugo.R;
import com.example.wertugo.activities.BucketListDetailActivity;
import com.example.wertugo.api.models.BucketListModel;

import java.util.List;

public class BucketListAdapter extends RecyclerView.Adapter<BucketListAdapter.ViewHolder> {

    private List<BucketListModel> bucketList;

    public BucketListAdapter(List<BucketListModel> bucketList) {
        this.bucketList = bucketList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Menggunakan layout item_bucket_list.xml yang baru
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bucket_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BucketListModel bucket = bucketList.get(position);

        // Pasang data ke UI
        holder.tvName.setText(bucket.getNamaBucket());
        holder.tvInfo.setText(bucket.getTotalMember() + " Member • " + bucket.getTotalTempat() + " Tempat");

        // Event saat kartu diklik, pindah ke halaman Detail
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), BucketListDetailActivity.class);
            intent.putExtra("BUCKET_ID", bucket.getId());
            intent.putExtra("BUCKET_NAME", bucket.getNamaBucket());
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return bucketList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvInfo;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // ID di sini sekarang sudah 100% sama dengan yang ada di item_bucket_list.xml
            tvName = itemView.findViewById(R.id.tv_bucket_name);
            tvInfo = itemView.findViewById(R.id.tv_bucket_info);
        }
    }
}