package org.garywzh.quumiibox.network;

import android.view.MenuItem;

import com.google.gson.Gson;

import org.garywzh.quumiibox.AppContext;
import org.garywzh.quumiibox.R;
import org.garywzh.quumiibox.common.UserState;
import org.garywzh.quumiibox.eventbus.UserOperationResponseEvent;
import org.garywzh.quumiibox.eventbus.UserReplyResponseEvent;
import org.garywzh.quumiibox.model.Item;
import org.garywzh.quumiibox.model.UserOperation;
import org.garywzh.quumiibox.model.UserReply;
import org.garywzh.quumiibox.util.LogUtils;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RequestHelper {
    private static final String TAG = RequestHelper.class.getSimpleName();

    public static final String BASE_URL = "http://www.quumii.com";
    private static final String VIDEO_INFO_URL_PREFIX = BASE_URL + "/app/api.php?method=getv&vid=";
    private static final String USER_OPRATION_URL = BASE_URL + "/app/api.php?method=click";
    private static final String USER_REPLY_URL = BASE_URL + "/app/api.php?method=addcomment";
    public static final int ONCE_LOAD_ITEM_COUNT = 30;

    private static final OkHttpClient CLIENT;
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final Gson GSON;

    static {
        CLIENT = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .followRedirects(true)
                .build();

        GSON = new Gson();
    }

    private static Gson getGson() {
        return GSON;
    }

    public static void userOperation(Item item, MenuItem menuItem) throws IOException {
        UserOperation userOperation;
        final String uid = UserState.getInstance().getId();
        final String blogid = item.blogid;

        switch (menuItem.getItemId()) {
            case R.id.action_up:
                userOperation = new UserOperation(uid, blogid, UserOperation.TYPE_LIKE);
                break;
            case R.id.action_down:
                userOperation = new UserOperation(uid, blogid, UserOperation.TYPE_UNLIKE);
                break;
            case R.id.action_fav:
                userOperation = new UserOperation(uid, blogid, UserOperation.TYPE_FAV);
                break;
            default:
                userOperation = new UserOperation();
        }

        String json = getGson().toJson(userOperation);
        final RequestBody body = RequestBody.create(JSON, json);
        final Request request = new Request.Builder().url(USER_OPRATION_URL).post(body).build();
        final Response response = sendRequest(request);
        json = response.body().string();
        AppContext.getEventBus().post(new UserOperationResponseEvent(userOperation.type, json));
    }

    public static void sentReply(String blogid, String reply) throws IOException {
        final UserReply userReply = new UserReply(UserState.getInstance().getId(), blogid, "", reply);

        String json = getGson().toJson(userReply);
        final RequestBody body = RequestBody.create(JSON, json);
        final Request request = new Request.Builder().url(USER_REPLY_URL).post(body).build();
        final Response response = sendRequest(request);
        json = response.body().string();
        AppContext.getEventBus().post(new UserReplyResponseEvent(json));
    }

    static Response sendRequest(Request request) throws IOException {
        Response response;
        LogUtils.d(TAG, request.toString());
        response = CLIENT.newCall(request).execute();
        LogUtils.d(TAG, response.toString());

        return response;
    }
}