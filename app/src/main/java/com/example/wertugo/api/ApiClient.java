package com.example.wertugo.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;

public class ApiClient {
    // Gunakan IP ini untuk emulator Android
    private static final String BASE_URL = "http://10.0.2.2:8000/api/";
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            // Gson dengan mode lenient untuk menerima JSON yang sedikit tidak baku
            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();

            // Logging interceptor untuk debugging
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            // Interceptor untuk menambahkan header Accept: application/json
            Interceptor headerInterceptor = new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request original = chain.request();
                    Request request = original.newBuilder()
                            .header("Accept", "application/json")
                            .method(original.method(), original.body())
                            .build();
                    return chain.proceed(request);
                }
            };

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(headerInterceptor)
                    .addInterceptor(logging)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(client)
                    .build();
        }
        return retrofit;
    }

    public static ApiService getApiService() {
        return getClient().create(ApiService.class);
    }
}
