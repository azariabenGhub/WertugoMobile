package com.example.wertugo.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wertugo.R;
import com.example.wertugo.api.models.MemberCommentModel;

import java.util.List;

public class MemberCommentAdapter extends RecyclerView.Adapter<MemberCommentAdapter.ViewHolder> {

    private List<MemberCommentModel> commentList;

    public MemberCommentAdapter(List<MemberCommentModel> commentList) {
        this.commentList = commentList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_member_comment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MemberCommentModel comment = commentList.get(position);

        String name = comment.getUserName();
        if (name == null || name.trim().isEmpty() || name.equals("null")) {
            holder.tvName.setText("Anggota (Tanpa Nama)");
        } else {
            holder.tvName.setText(name);
        }

        // ==========================================
        // PAKSA BINTANG MENYALA KUNING
        // ==========================================
        holder.ratingBar.setRating(comment.getRating());

        // Cek dan paksakan warna kuning dari Java
        if (holder.ratingBar.getProgressDrawable() != null) {
            holder.ratingBar.getProgressDrawable().setColorFilter(
                    android.graphics.Color.parseColor("#FFC107"),
                    android.graphics.PorterDuff.Mode.SRC_ATOP
            );
        }

        holder.tvComment.setText(comment.getComment());
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvComment;
        RatingBar ratingBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_comment_user_name);
            tvComment = itemView.findViewById(R.id.tv_comment_text);
            ratingBar = itemView.findViewById(R.id.rb_comment_rating);
        }
    }
}