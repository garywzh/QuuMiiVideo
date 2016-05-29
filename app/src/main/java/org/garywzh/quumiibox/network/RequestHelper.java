package org.garywzh.quumiibox.network;

import android.util.Log;
import android.view.MenuItem;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.garywzh.quumiibox.AppContext;
import org.garywzh.quumiibox.BuildConfig;
import org.garywzh.quumiibox.R;
import org.garywzh.quumiibox.common.UserState;
import org.garywzh.quumiibox.common.exception.ConnectionException;
import org.garywzh.quumiibox.common.exception.RemoteException;
import org.garywzh.quumiibox.common.exception.RequestException;
import org.garywzh.quumiibox.eventbus.UserOperationResponseEvent;
import org.garywzh.quumiibox.eventbus.UserReplyResponseEvent;
import org.garywzh.quumiibox.eventbus.VideoInfoResponseEvent;
import org.garywzh.quumiibox.model.Comment;
import org.garywzh.quumiibox.model.Item;
import org.garywzh.quumiibox.model.ItemList;
import org.garywzh.quumiibox.model.LoginResult;
import org.garywzh.quumiibox.model.OperatInfo;
import org.garywzh.quumiibox.model.UserOperation;
import org.garywzh.quumiibox.model.UserReply;
import org.garywzh.quumiibox.model.VideoInfo;
import org.garywzh.quumiibox.ui.fragment.ItemListFragment;
import org.garywzh.quumiibox.util.LogUtils;
import org.garywzh.quumiibox.util.UTF8EncoderUtil;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RequestHelper {
    private static final String TAG = RequestHelper.class.getSimpleName();

    public static final String BASE_URL = "http://www.quumii.com";
    private static final String LOGIN_URL = BASE_URL + "/app/api.php?method=registerlogin";
    private static final String HOME_URL = BASE_URL + "/app/api.php?end=50&start=0&method=getlist&ftime=1";
    private static final String SEARCH_URL_PREFIX = BASE_URL + "/api.php?end=50&start=0&method=getlist&search=";
    private static final String COMMENT_LIST_URL_PREFIX = BASE_URL + "/app/api.php?method=getcomment&blogid=";
    private static final String VIDEO_INFO_URL_PREFIX = BASE_URL + "/app/api.php?method=getv&vid=";
    private static final String USER_OPRATION_URL = BASE_URL + "/app/api.php?method=click";
    private static final String USER_REPLY_URL = BASE_URL + "/app/api.php?method=addcomment";
    private static final int SERVER_ERROR_CODE = 500;
    public static final int ONCE_LOAD_ITEM_COUNT = 30;

    private static final OkHttpClient CLIENT;
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final Gson GSON;

    static {
        CLIENT = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .followRedirects(true)
                .build();

        GSON = new Gson();
    }

    private static Gson getGson() {
        return GSON;
    }

    public static OkHttpClient getClient() {
        return CLIENT;
    }

    public static LoginResult login(String username, String password) throws ConnectionException, RemoteException {
        LogUtils.v(TAG, "login user: " + username);

        final RequestBody requestBody = new FormBody.Builder()
                .add("username", username)
                .add("password", password)
                .build();
        Request request = new Request.Builder()
                .url(LOGIN_URL)
                .post(requestBody)
                .build();
        Response response = sendRequest(request);

        final LoginResult loginResult;
        try {
            final String json = response.body().string();
            loginResult = getGson().fromJson(json, new TypeToken<LoginResult>() {
            }.getType());
        } catch (IOException e) {
            throw new ConnectionException(e);
        }
        if (loginResult.operatinfo.status == OperatInfo.STATUS_LOGIN_SUCCESS) {
            LogUtils.d(TAG, "login succeed");
            return loginResult;
        } else {
            LogUtils.d(TAG, "login failed");
            return null;
        }
    }

    public static List<Item> getItemsByTypeByPage(int type, String queryString, int page) throws ConnectionException, RemoteException {
        final int start = (page - 1) * ONCE_LOAD_ITEM_COUNT;
        final int end = start + ONCE_LOAD_ITEM_COUNT;
        String itemListUrl;
        switch (type) {
            case ItemListFragment.TYPE_ALL:
                itemListUrl = BASE_URL + "/app/api.php?end=" + end + "&start=" + start + "&method=getlist&ftime=1";
                break;
            case ItemListFragment.TYPE_SEARCH:
                itemListUrl = BASE_URL + "/app/api.php?end=" + end + "&start=" + start + "&method=getlist&search=" + UTF8EncoderUtil.encode(queryString);
                break;
            default:
                throw new RuntimeException("error type");
        }
        LogUtils.d(TAG, "start loading items");

        final Request request = new Request.Builder()
                .url(itemListUrl)
                .build();
        final Response response = sendRequest(request);
        final ItemList itemList;
        try {
            final String json = response.body().string();
            itemList = getGson().fromJson(json, new TypeToken<ItemList>() {
            }.getType());
        } catch (IOException e) {
            throw new ConnectionException(e);
        }
        if (BuildConfig.DEBUG && itemList.content != null) {
            Log.v(TAG, "page " + page + " received items, count: " + itemList.content.size());
        }
        return itemList.content;
    }

    public static void fectchVideoInfo(String id) {
        final Request request = new Request.Builder()
                .url(VIDEO_INFO_URL_PREFIX + id)
                .build();

        CLIENT.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                AppContext.getEventBus().post(new VideoInfoResponseEvent(null, e));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String json = response.body().string();

                VideoInfo videoInfo = getGson().fromJson(json, new TypeToken<VideoInfo>() {
                }.getType());

                AppContext.getEventBus().post(new VideoInfoResponseEvent(videoInfo, null));
            }
        });
    }

    public static List<Comment> getComments(String id) throws ConnectionException, RemoteException {
        final String commentListUrl = COMMENT_LIST_URL_PREFIX + id;

        LogUtils.d(TAG, "start loading comments");
        final Request request = new Request.Builder()
                .url(commentListUrl)
                .build();
        Response response = sendRequest(request);
        LogUtils.d(TAG, "comments got");

        final List<Comment> comments;
        try {
            final String json = response.body().string();
            comments = getGson().fromJson(json, new TypeToken<List<Comment>>() {
            }.getType());
        } catch (IOException e) {
            throw new ConnectionException(e);
        }
        if (BuildConfig.DEBUG) {
            if (comments != null) {
                Log.v(TAG, "received comments, count: " + comments.size());
            }
        }
        return comments;
    }

    public static void userOperation(Item item, MenuItem menuItem) throws ConnectionException, RemoteException {
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
        try {
            json = response.body().string();
        } catch (IOException e) {
            throw new ConnectionException(e);
        }
        AppContext.getEventBus().post(new UserOperationResponseEvent(userOperation.type, json));
    }

    public static void sentReply(String blogid, String reply) throws ConnectionException, RemoteException {
        final UserReply userReply = new UserReply(UserState.getInstance().getId(), blogid, "", reply);

        String json = getGson().toJson(userReply);
        final RequestBody body = RequestBody.create(JSON, json);
        final Request request = new Request.Builder().url(USER_REPLY_URL).post(body).build();
        final Response response = sendRequest(request);
        try {
            json = response.body().string();
        } catch (IOException e) {
            throw new ConnectionException(e);
        }
        AppContext.getEventBus().post(new UserReplyResponseEvent(json));
    }

    static Response sendRequest(Request request) throws ConnectionException, RemoteException {
        final Response response;
        try {
            LogUtils.d(TAG, request.toString());
            response = CLIENT.newCall(request).execute();
            LogUtils.d(TAG, response.toString());
        } catch (IOException e) {
            throw new ConnectionException(e);
        }
        return response;
    }

    private static void checkResponse(Response response) throws RemoteException, RequestException, ConnectionException {
        if (response.isSuccessful()) {
            return;
        }
        final int code = response.code();
        if (code == 302) {
            return;
        }
        if (code >= SERVER_ERROR_CODE) {
            throw new RemoteException(response);
        }
        if (code == 403 || code == 404) {
            try {
                final String body = response.body().string();
                LogUtils.d(TAG, "404 : " + body);
            } catch (IOException e) {
                throw new ConnectionException(e);
            }
        }
        throw new RequestException(response);
    }
}