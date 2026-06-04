package com.example.wertugo.api;

import com.example.wertugo.api.models.ApiResponse;
import com.example.wertugo.api.models.LoginRequest;
import com.example.wertugo.api.models.LoginResponse;
import com.example.wertugo.api.models.RegisterRequest;

import java.util.List;
import java.util.Map;

import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Field;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Path;
import retrofit2.http.DELETE;
import retrofit2.http.Query;


public interface ApiService {

    // Endpoint untuk memanggil UmkmController@index
    // ==========================================
    // DATA BERANDA USER
    // ==========================================
    // Untuk klik detail tempat

    // ==========================================
    // DATA KOMENTAR DAN RATING
    // ==========================================
//    @POST("places/{id}/comment")
//    Call<Map<String, Object>> postComment(
//            @Header("Authorization") String auth,
//            @Path("id") String placeId,
//            @Body Map<String, Object> body
//    );


    // Endpoint untuk Upload Thumbnail & Banyak Banner sekaligus
    // TETAP GUNAKAN @POST DI SINI! (Ini bukan salah ketik)
    // Kita akan memanipulasinya dari dalam UploadGaleriActivity
    @Multipart
    @POST("umkm/place/gallery") // Pastikan URL sesuai dengan routes/api.php
    Call<Map<String, Object>> uploadGaleriUmkmMultiple(
            @Header("Authorization") String token,
            @Part List<MultipartBody.Part> files
    );

    @GET("places/search")
    Call<Map<String, Object>> searchPlaces(@Header("Authorization") String auth, @Query("q") String keyword);

    @DELETE("umkm/place/gallery/{id}")
    Call<Map<String, Object>> deleteGallery(
            @Header("Authorization") String token,
            @Path("id") String id
    );
    // ==========================================
    // TAMBAH TEMPAT (DENGAN GAMBAR)
    // ==========================================



    @Multipart
    @POST("bucketlist/{id}/places")
    Call<Map<String, Object>> addPlaceToBucketList(
            @Header("Authorization") String token,
            @Path("id") String bucketId,
            @Part("place_id") okhttp3.RequestBody placeId,
            @Part("nama_tempat") okhttp3.RequestBody namaTempat,
            @Part("lokasi") okhttp3.RequestBody lokasi,
            @Part("deskripsi") okhttp3.RequestBody deskripsi,
            @Part("kategori") okhttp3.RequestBody kategori,
            @Part okhttp3.MultipartBody.Part image // 👈 Ini untuk file gambarnya
    );

    // ==========================================
    // EDIT TEMPAT BUCKET LIST
    // ==========================================
    @FormUrlEncoded
    @PUT("bucketlist/{id}/places/{place_id}")
    Call<Map<String, Object>> updatePlace(
            @Header("Authorization") String token,
            @Path("id") String bucketId,
            @Path("place_id") String placeId,
            @Field("nama_tempat") String namaTempat,
            @Field("lokasi") String lokasi,
            @Field("deskripsi") String deskripsi,
            @Field("kategori") String kategori
    );

    // Mengambil detail bucket list beserta daftar tempatnya
    @GET("bucketlist/{id}")
    Call<Map<String, Object>> getBucketListDetail(
            @Header("Authorization") String token,
            @Path("id") String bucketId
    );



    // Menambah destinasi/tempat ke bucket list
    // Menambah destinasi/tempat ke bucket list (dengan data lengkap)
    @FormUrlEncoded
    @POST("bucketlist/{id}/places")
    Call<Map<String, Object>> addPlaceToBucketList(
            @Header("Authorization") String token,
            @Path("id") String bucketId,
            @Field("place_id") String placeId,
            @Field("nama_tempat") String namaTempat,
            @Field("lokasi") String lokasi,
            @Field("deskripsi") String deskripsi,
            @Field("kategori") String kategori
    );

    // Menghapus tempat dari bucket list
    @DELETE("bucketlist/{id}/places/{place_id}")
    Call<Map<String, Object>> removePlace(
            @Header("Authorization") String token,
            @Path("id") String bucketId,
            @Path("place_id") String placeId
    );

    // Mengedit tempat di bucket list
    // Mengambil daftar Bucket List milik user
    @GET("bucketlist")
    Call<Map<String, Object>> getBucketLists(@Header("Authorization") String token);

    // Membuat Bucket List baru
    @FormUrlEncoded
    @POST("bucketlist")
    Call<Map<String, Object>> createBucketList(
            @Header("Authorization") String token,
            @Field("title") String title // <-- Diubah menjadi title
    );
    // ==========================================
    // KIRIM LAPORAN
    // ==========================================
    @GET("beranda/{id}")
    Call<Map<String, Object>> getDetailTempat(@Header("Authorization") String auth, @Path("id") String placeId);
    @GET("beranda")
    Call<Map<String, Object>> getBerandaUser(@Header("Authorization") String auth);
    @POST("register")
    Call<ApiResponse> register(@Body RegisterRequest request);

    @POST("login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("umkm/place")
    Call<Map<String, Object>> registerUmkm(@Header("Authorization") String auth, @Body Map<String, String> body);

    @GET("umkm/place")
    Call<Map<String, Object>> getUmkmPlace(@Header("Authorization") String auth);

    @PUT("umkm/place")
    Call<Map<String, Object>> updateUmkmPlace(@Header("Authorization") String auth, @Body Map<String, Object> body);

    @GET("places/{id}/reviews")
    Call<Map<String, Object>> getPlaceReviews(@Header("Authorization") String auth, @Path("id") String placeId);

    @POST("places/{id}/comment")
    Call<Map<String, Object>> postComment(@Header("Authorization") String auth, @Path("id") String placeId, @Body Map<String, Object> body);

    @POST("reports")
    Call<Map<String, Object>> postReport(@Header("Authorization") String auth, @Body Map<String, Object> body);

    @GET("umkm/{umkm_id}/comments")
    Call<Map<String, Object>> getUmkmComments(@Header("Authorization") String auth, @Path("umkm_id") String umkmId);
    
    @FormUrlEncoded
    @POST("comments/{id}/reply")
    Call<Map<String, Object>> replyComment(
            @Header("Authorization") String token,
            @Path("id") String commentId,
            @Field("reply") String reply
    );

    // ==========================================
    // INVITATION & COLLABORATION BUCKET LIST
    // ==========================================
    @POST("bucketlist/{id}/invite")
    Call<Map<String, Object>> inviteUser(@Header("Authorization") String auth, @Path("id") String id, @Body Map<String, String> body);

    @GET("bucketlist/invitations")
    Call<Map<String, Object>> getInvitations(@Header("Authorization") String auth);

    @POST("bucketlist/{id}/join")
    Call<Map<String, Object>> joinSharedList(@Header("Authorization") String auth, @Path("id") String id, @Body Map<String, Boolean> body);

    @PUT("umkm/place/status")
    Call<Map<String, Object>> updateUmkmStatus(@Header("Authorization") String auth, @Body Map<String, Boolean> body);

    @PUT("umkm/place/schedule")
    Call<Map<String, Object>> updateSchedule(@Header("Authorization") String auth, @Body Map<String, Object> body);

    @Multipart
    @POST("umkm/documents")
    Call<Map<String, Object>> uploadLegalitasDocuments(
            @Header("Authorization") String auth,
            @Part java.util.List<okhttp3.MultipartBody.Part> documents
    );

    @GET("umkm/verification-status")
    Call<Map<String, Object>> checkVerificationStatus(@Header("Authorization") String auth);

    // Mengirim rating dan komentar personal untuk suatu tempat di dalam bucket list
    @FormUrlEncoded
    @POST("bucketlist/{id}/rating")
    Call<Map<String, Object>> ratePlace(
            @Header("Authorization") String token,
            @Path("id") String bucketId,
            @Field("place_id") String placeId,
            @Field("rating") int rating,
            @Field("comment") String comment
    );
    @GET("umkm/verification-status")
    Call<Map<String, Object>> getVerificationStatus(@Header("Authorization") String auth);

    @GET("profile")
    Call<Map<String, Object>> getProfile(@Header("Authorization") String auth);

    @Multipart
    @POST("profile/update")
    Call<Map<String, Object>> updateProfileWithImage(
            @Header("Authorization") String auth,
            @PartMap Map<String, RequestBody> textFields,
            @Part MultipartBody.Part foto
    );

    @POST("logout")
    Call<Void> logout(@Header("Authorization") String auth);
}