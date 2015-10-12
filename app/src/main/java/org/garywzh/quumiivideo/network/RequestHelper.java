package org.garywzh.quumiivideo.network;

import android.util.Log;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.jsoup.nodes.Document;
import org.garywzh.quumiivideo.util.LogUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.garywzh.quumiivideo.BuildConfig;
import org.garywzh.quumiivideo.common.exception.ConnectionException;
import org.garywzh.quumiivideo.common.exception.RemoteException;
import org.garywzh.quumiivideo.common.exception.RequestException;
import org.garywzh.quumiivideo.model.Comment;
import org.garywzh.quumiivideo.model.Item;
import org.garywzh.quumiivideo.network.interceptor.UserAgentInterceptor;
import org.garywzh.quumiivideo.parser.CommentListParser;
import org.garywzh.quumiivideo.parser.ItemListParser;
import org.garywzh.quumiivideo.parser.Parser;
import org.garywzh.quumiivideo.parser.VideoUrlParser;

public class RequestHelper {
    public static final String BASE_URL = "http://www.quumii.com";

    private static final String TAG = RequestHelper.class.getSimpleName();

    private static String VIDEO_URL = "http://www.quumii.com/videolist/fake.php?blogid=";

    private static final int SERVER_ERROR_CODE = 500;

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

    public static List<Item> getItems() throws ConnectionException, RemoteException {

        final List<Item> allItems = new ArrayList<>();

        for (int page = 1; page <= 3; page++) {
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
                Log.v(TAG, "received items, count: " + items.size());
            }
            allItems.addAll(items);
        }

        return allItems;
    }

    public static String getRedirectUrl(String url) throws ConnectionException, RemoteException {
        final Request request = new Request.Builder()
                .url(url)
                .build();

        final Response response = sendRequest(request);

        final String responseBody;
        try {
            responseBody = response.body().string();

            LogUtils.d(TAG, responseBody);
        } catch (IOException e) {
            throw new ConnectionException(e);
        }
        return response.header("Request URL");
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

        if (response.code() == HttpStatus.SC_MOVED_TEMPORARILY){
            LogUtils.d(TAG, "location : "+response.header("location"));

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

        if (code ==302){
            return;
        }
        if (code >= SERVER_ERROR_CODE) {
            throw new RemoteException(response);
        }

        if (code == 403 || code == 404) {
            try {
                final String body = response.body().string();
                LogUtils.d(TAG, "404 : "+body);
            } catch (IOException e) {
                throw new ConnectionException(e);
            }
        }
        throw new RequestException(response);
    }
}
