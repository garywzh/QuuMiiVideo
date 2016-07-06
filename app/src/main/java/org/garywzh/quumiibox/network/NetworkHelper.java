package org.garywzh.quumiibox.network;

import org.garywzh.quumiibox.util.LogUtils;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by garywzh on 2016/7/3.
 */
public class NetworkHelper {
    public static String TAG = NetworkHelper.class.getSimpleName();
    public static String BASE_URL = "http://www.quumii.com/";
    public static int ONCE_LOAD_COUNT = 30;

    public static QuumiiApi quumiiApi = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(new OkHttpClient.Builder()
                    .addInterceptor(new HttpLoggingInterceptor(new SimpleLogger())
                            .setLevel(HttpLoggingInterceptor.Level.BASIC))
                    .build())
            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(QuumiiApi.class);

    public static QuumiiApi getApiService() {
        return quumiiApi;
    }

    static class SimpleLogger implements HttpLoggingInterceptor.Logger {
        @Override
        public void log(String message) {
            LogUtils.d(TAG, message);
        }
    }
}
