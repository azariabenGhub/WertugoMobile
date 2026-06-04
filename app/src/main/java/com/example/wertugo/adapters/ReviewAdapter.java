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
import com.example.wertugo.api.models.Review;
import com.example.wertugo.api.ApiClient;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private List<Review> reviewList;
    private SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    private SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
    private boolean isUmkm;
    private OnReplyClickListener replyClickListener;

    public interface OnReplyClickListener {
        void onReplyClick(Review review, int position);
    }

    public ReviewAdapter(List<Review> reviewList, boolean isUmkm, OnReplyClickListener listener) {
        this.reviewList = reviewList;
        this.isUmkm = isUmkm;
        this.replyClickListener = listener;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review_card, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviewList.get(position);

        holder.tvName.setText(review.getUser_name());
        holder.tvContent.setText(review.getComment());

        // Format tanggal
        String formattedDate = review.getCreated_at();
        try {
            Date date = inputFormat.parse(review.getCreated_at());
            if (date != null) formattedDate = outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        holder.tvDate.setText(formattedDate);

        // Rating stars
        setRatingStars(holder, review.getRating());

        // Avatar (gunakan Glide jika ada URL)
        String avatarUrl = review.getUser_avatar();
        if (avatarUrl != null && !avatarUrl.isEmpty() && !avatarUrl.equals("default-profile.png")) {
            String fullUrl = ApiClient.BASE_URL + "storage/" + avatarUrl;
            Glide.with(holder.itemView.getContext())
                    .load(fullUrl)
                    .placeholder(R.drawable.ic_default_profile)
                    .circleCrop()
                    .into(holder.ivAvatar);
        } else {
            holder.ivAvatar.setImageResource(R.drawable.ic_default_profile);
        }

        // Reply UI Logic
        if (review.getReply() != null && !review.getReply().trim().isEmpty()) {
            holder.layoutReply.setVisibility(View.VISIBLE);
            holder.tvReply.setText(review.getReply());
            holder.btnReply.setVisibility(View.GONE);
        } else {
            holder.layoutReply.setVisibility(View.GONE);
            if (isUmkm) {
                holder.btnReply.setVisibility(View.VISIBLE);
                holder.btnReply.setOnClickListener(v -> {
                    if (replyClickListener != null) {
                        replyClickListener.onReplyClick(review, position);
                    }
                });
            } else {
                holder.btnReply.setVisibility(View.GONE);
            }
        }
    }

    private void setRatingStars(ReviewViewHolder holder, float rating) {
        int fullStars = (int) rating;
        boolean hasHalfStar = (rating - fullStars) >= 0.5;

        int[] starIds = {R.id.star1, R.id.star2, R.id.star3, R.id.star4, R.id.star5};
        for (int i = 0; i < starIds.length; i++) {
            ImageView star = holder.itemView.findViewById(starIds[i]);
            if (i < fullStars) {
                star.setImageResource(android.R.drawable.btn_star_big_on);
                star.setColorFilter(0xFFF1C40F); // kuning
            } else if (i == fullStars && hasHalfStar) {
                star.setImageResource(android.R.drawable.btn_star_big_on); // setengah bintang butuh drawable khusus, untuk sementara full
                star.setColorFilter(0xFFF1C40F);
            } else {
                star.setImageResource(android.R.drawable.btn_star_big_off);
                star.setColorFilter(0xFFD3D3D3);
            }
        }
    }

    @Override
    public int getItemCount() {
        return reviewList == null ? 0 : reviewList.size();
    }

    public void updateList(List<Review> newList) {
        this.reviewList = newList;
        notifyDataSetChanged();
    }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvName, tvDate, tvContent;
        View layoutReply;
        TextView tvReply;
        TextView btnReply;
        // Star ImageViews perlu di-inisialisasi di item_review_card.xml dengan id star1..star5
        // Untuk sementara kita akses dengan findViewById di onBind

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.iv_reviewer_avatar);
            tvName = itemView.findViewById(R.id.tv_reviewer_name);
            tvDate = itemView.findViewById(R.id.tv_review_date);
            tvContent = itemView.findViewById(R.id.tv_review_content);
            layoutReply = itemView.findViewById(R.id.layout_reply);
            tvReply = itemView.findViewById(R.id.tv_review_reply);
            btnReply = itemView.findViewById(R.id.btn_reply_comment);
        }
    }
}