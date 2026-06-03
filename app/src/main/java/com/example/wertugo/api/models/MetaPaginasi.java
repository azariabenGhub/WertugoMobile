package com.example.wertugo.models;
import com.google.gson.annotations.SerializedName;

public class MetaPaginasi {
    @SerializedName("current_page")
    private int currentPage;

    @SerializedName("last_page")
    private int lastPage;

    @SerializedName("has_more_pages")
    private boolean hasMorePages;

    // TODO: Generate Getter
    public int getCurrentPage() { return currentPage; }
    public int getLastPage() { return lastPage; }
    public boolean isHasMorePages() { return hasMorePages; }
}