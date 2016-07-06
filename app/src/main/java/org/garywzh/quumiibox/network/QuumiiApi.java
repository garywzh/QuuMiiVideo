package org.garywzh.quumiibox.network;

import org.garywzh.quumiibox.model.Comment;
import org.garywzh.quumiibox.model.ItemList;
import org.garywzh.quumiibox.model.LoginResult;
import org.garywzh.quumiibox.model.VideoInfo;

import java.util.List;

import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by garywzh on 2016/7/3.
 */
public interface QuumiiApi {

    @GET("app/api.php?method=getv")
    Observable<VideoInfo> fectchVideoInfo(@Query("vid") String id);

    @GET("app/api.php?method=getcomment")
    Observable<List<Comment>> getComments(@Query("blogid") String id);

    @GET("app/api.php?method=getlist&ftime=1")
    Observable<ItemList> getItems(@Query("start") int start, @Query("end") int end);

    @GET("app/api.php?method=getlist")
    Observable<ItemList> search(@Query("start") int start, @Query("end") int end, @Query("search") String search);

    @FormUrlEncoded
    @POST("app/api.php?method=registerlogin")
    Observable<LoginResult> login(@Field("username") String username, @Field("password") String password);
}
