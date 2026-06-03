package com.example.wertugo.api;

import com.example.wertugo.api.models.ApiResponse;
import com.example.wertugo.api.models.LoginRequest;
import com.example.wertugo.api.models.LoginResponse;
import com.example.wertugo.api.models.RegisterRequest;

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
import retrofit2.http.Path;


public interface ApiService {

    // Endpoint untuk memanggil UmkmController@index
    // ==========================================
    // DATA BERANDA USER
    // ==========================================
    // Untuk klik detail tempat

    // ==========================================
    // DATA KOMENTAR DAN RATING
    // ==========================================
    @POST("places/{id}/comment")
    Call<Map<String, Object>> postComment(
            @Header("Authorization") String auth,
            @Path("id") String placeId,
            @Body Map<String, Object> body
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
    @POST("reports")
    Call<Map<String, Object>> postReport(
            @Header("Authorization") String auth,
            @Body Map<String, Object> body
    );
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

    @GET("places/{id}/reviews")
    Call<Map<String, Object>> getPlaceReviews(@Header("Authorization") String auth, @Path("id") String placeId);

    @GET("umkm/{umkm_id}/comments")
    Call<Map<String, Object>> getUmkmComments(@Header("Authorization") String auth, @Path("umkm_id") String umkmId);

    @PUT("umkm/place/status")
    Call<Map<String, Object>> updateUmkmStatus(@Header("Authorization") String auth, @Body Map<String, Boolean> body);

    @Multipart
    @POST("umkm/documents")
    Call<Map<String, Object>> uploadLegalitasDocuments(
            @Header("Authorization") String auth,
            @Part MultipartBody.Part nib_dokumen,
            @Part MultipartBody.Part npwp_dokumen,
            @Part MultipartBody.Part legalitas_bangunan_dokumen,
            @Part MultipartBody.Part sertifikat_halal_dokumen,
            @Part MultipartBody.Part sertifikat_usaha_pariwisata_dokumen
    );

    @GET("umkm/verification-status")
    Call<Map<String, Object>> getVerificationStatus(@Header("Authorization") String auth);

    @GET("profile")
    Call<Map<String, Object>> getProfile(@Header("Authorization") String auth);

    @Multipart
    @PUT("profile/update")
    Call<Map<String, Object>> updateProfileWithImage(
            @Header("Authorization") String auth,
            @PartMap Map<String, RequestBody> textFields,
            @Part MultipartBody.Part foto
    );

    @POST("logout")
    Call<Void> logout(@Header("Authorization") String auth);
}