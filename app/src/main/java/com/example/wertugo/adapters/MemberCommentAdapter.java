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

public class MemberCommentAdapter extends RecyclerView.Adapter<MemberCommentAdapter.CommentViewHolder> {

    private List<MemberCommentModel> commentList;

    public MemberCommentAdapter(List<MemberCommentModel> commentList) {
        this.commentList = commentList;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_member_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        MemberCommentModel comment = commentList.get(position);
        holder.tvName.setText(comment.getUserName());
        holder.ratingBar.setRating(comment.getRating());
        holder.tvText.setText(comment.getCommentText());
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvText;
        RatingBar ratingBar;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_commenter_name);
            tvText = itemView.findViewById(R.id.tv_commenter_text);
            ratingBar = itemView.findViewById(R.id.rating_commenter_stars);
        }
    }
}