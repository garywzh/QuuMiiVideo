package org.garywzh.quumiibox.network;

import android.util.Log;

import com.google.common.collect.Lists;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.garywzh.quumiibox.common.exception.ConnectionException;
import org.garywzh.quumiibox.common.exception.RemoteException;
import org.garywzh.quumiibox.common.exception.RequestException;
import org.garywzh.quumiibox.network.interceptor.UserAgentInterceptor;
import org.garywzh.quumiibox.parser.VideoUrlParser;
import org.garywzh.quumiibox.util.LogUtils;
import org.garywzh.quumiibox.BuildConfig;
import org.garywzh.quumiibox.model.Comment;
import org.garywzh.quumiibox.model.Item;
import org.garywzh.quumiibox.parser.CommentListParser;
import org.garywzh.quumiibox.parser.ItemListParser;
import org.garywzh.quumiibox.parser.Parser;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RequestHelper {
    public static final String BASE_URL = "http://www.quumii.com";

    private static final String TAG = RequestHelper.class.getSimpleName();

    private static String VIDEO_URL = "http://www.quumii.com/videolist/fake.php?blogid=";

    private static final int SERVER_ERROR_CODE = 500;

    private static final int ONCE_LOAD_PAGE_COUNT = 3;

    private static final OkHttpClient CLIENT;

    static {
        CLIENT = new OkHttpClient();
        CLIENT.setConnectTimeout(10, TimeUnit.SECONDS);
        CLIENT.setWriteTimeout(10, TimeUnit.SECONDS);
        CLIENT.setReadTimeout(30, TimeUnit.SECONDS);
        CLIENT.networkInterceptors().add(new UserAgentInterceptor());
        CLIENT.setFollowRedirects(false);
    }

    public static OkHttpClient getClient() {
        return CLIENT;
    }

    //    一次获取多页视频，减少网络获取频率，避免一直loading标志出现频率过高
    public static List<Item> getMutiPageItemsByCount(int count) throws ConnectionException, RemoteException {
        final List<Item> result = Lists.newArrayListWithCapacity(ONCE_LOAD_PAGE_COUNT * 12);
        for (int i = 1; i <= ONCE_LOAD_PAGE_COUNT; i++) {
            result.addAll(getItemsByPage((count - 1) * ONCE_LOAD_PAGE_COUNT + i));
        }
        return result;
    }

    public static List<Item> getItemsByPage(int page) throws ConnectionException, RemoteException {

        final String videoListUrl = "http://www.quumii.com/videolist.php?type=all.0.0.0.dateline.0.0&page=" + page + "&ajaxdiv=main_content&inajax=1&ajaxtarget=main_content&inajax=1";
        LogUtils.d(TAG, "start loading items");

        final Request request = new Request.Builder()
                .url(videoListUrl)
                .build();

        final Response response = sendRequest(request);

        final Document doc;
        final List<Item> items;
        try {
            doc = Parser.xmlToDoc(response.body().string());
            items = ItemListParser.parseDocForItemList(doc);
        } catch (IOException e) {
            throw new ConnectionException(e);
        }

        if (BuildConfig.DEBUG) {
            Log.v(TAG, "page " + page + " received items, count: " + items.size());
        }

        return items;
    }

    public static String getWebViewLinkById(String id) throws ConnectionException, RemoteException {
        final Request request = new Request.Builder()
                .url(VIDEO_URL + id)
                .build();
        final Response response = sendRequest(request);

        final String link;
        try {
            final Document doc = Parser.toDoc(response.body().string());

            link = VideoUrlParser.parseDocForVideoUrl(doc);
        } catch (IOException e) {
            throw new ConnectionException(e);
        }
        return link;

    }

    public static List<Comment> getComments(int id) throws ConnectionException, RemoteException {

        final String itemPageUrl = Item.buildUrlFromId(id);

        LogUtils.d(TAG, "start loading comments");
        final Request request = new Request.Builder()
                .url(itemPageUrl)
                .build();

        Response response = sendRequest(request);

        if (response.code() == HttpStatus.SC_MOVED_TEMPORARILY) {
            LogUtils.d(TAG, "location : " + response.header("location"));

            final Request finalRequest = new Request.Builder()
                    .url(response.header("location"))
                    .build();
            response = sendRequest(finalRequest);
        }

        LogUtils.d(TAG, "response got");

        final Document doc;
        final List<Comment> comments;
        try {
//            System.out.println("response : " + response.body().string());

            doc = Parser.toDoc(response.body().string());
            LogUtils.d(TAG, "toDoc done");
            comments = CommentListParser.parseDocForCommentList(doc);
        } catch (IOException e) {
            throw new ConnectionException(e);
        }

        if (BuildConfig.DEBUG) {
            Log.v(TAG, "received comments, count: " + comments.size());
        }

        return comments;
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

        checkResponse(response);
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
