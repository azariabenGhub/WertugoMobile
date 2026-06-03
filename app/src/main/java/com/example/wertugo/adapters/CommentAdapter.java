package com.example.wertugo.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wertugo.R;

import java.util.List;
import java.util.Map;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {

    private final List<Map<String, Object>> commentList;
    private final String currentUserId; // Untuk mengecek apakah ini komentar sendiri
    private final OnReportClickListener reportClickListener;

    // Interface untuk memicu Pop-up di Activity
    public interface OnReportClickListener {
        void onReportClick(String commentId, String reportedUserId, String username);
    }

    public CommentAdapter(List<Map<String, Object>> commentList, String currentUserId, OnReportClickListener listener) {
        this.commentList = commentList;
        this.currentUserId = currentUserId;
        this.reportClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> comment = commentList.get(position);

        Object rawText = comment.get("content");
        Object rawRating = comment.get("rating");

        // Ambil ID Komentar dan ID Pemilik Komentarnya
        Object rawCommentId = comment.containsKey("_id") ? comment.get("_id") : comment.get("id");
        Object rawReportedUserId = comment.get("user_id");

        String commentId = rawCommentId != null ? String.valueOf(rawCommentId) : "";
        String reportedUserId = rawReportedUserId != null ? String.valueOf(rawReportedUserId) : "";

        holder.tvText.setText(rawText != null ? String.valueOf(rawText) : "");
        holder.tvRating.setText(rawRating != null ? rawRating + "/5" : "0/5");

        String displayUsername = "Anonim";
        if (comment.containsKey("user") && comment.get("user") instanceof Map) {
            Map<String, Object> userObj = (Map<String, Object>) comment.get("user");
            Object rawUsername = userObj.get("username");
            displayUsername = rawUsername != null ? String.valueOf(rawUsername) : "Anonim";
        }
        holder.tvUsername.setText(displayUsername);

        // LOGIKA: Sembunyikan tombol Report jika ini adalah komentar miliknya sendiri
        if (currentUserId != null && currentUserId.equals(reportedUserId)) {
            holder.btnReport.setVisibility(View.GONE);
        } else {
            holder.btnReport.setVisibility(View.VISIBLE);
        }

        // Aksi Klik Tombol Report
        String finalDisplayUsername = displayUsername;
        holder.btnReport.setOnClickListener(v -> {
            if (reportClickListener != null) {
                reportClickListener.onReportClick(commentId, reportedUserId, finalDisplayUsername);
            }
        });
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUsername, tvText, tvRating;
        ImageView btnReport;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tv_comment_username);
            tvText = itemView.findViewById(R.id.tv_comment_text);
            tvRating = itemView.findViewById(R.id.tv_comment_rating);
            btnReport = itemView.findViewById(R.id.btn_report_comment);
        }
    }
}