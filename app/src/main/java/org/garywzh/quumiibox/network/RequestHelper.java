package org.garywzh.quumiibox.network;

import android.util.Log;
import android.view.MenuItem;

import com.google.common.collect.Lists;
import com.google.common.net.HttpHeaders;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.garywzh.quumiibox.AppContext;
import org.garywzh.quumiibox.BuildConfig;
import org.garywzh.quumiibox.R;
import org.garywzh.quumiibox.common.UserState;
import org.garywzh.quumiibox.common.exception.ConnectionException;
import org.garywzh.quumiibox.common.exception.RemoteException;
import org.garywzh.quumiibox.common.exception.RequestException;
import org.garywzh.quumiibox.eventbus.UserOptionEvent;
import org.garywzh.quumiibox.model.Comment;
import org.garywzh.quumiibox.model.Item;
import org.garywzh.quumiibox.model.LoginResult;
import org.garywzh.quumiibox.model.Member;
import org.garywzh.quumiibox.network.interceptor.UserAgentInterceptor;
import org.garywzh.quumiibox.parser.CommentListParser;
import org.garywzh.quumiibox.parser.ItemListParser;
import org.garywzh.quumiibox.parser.MemberParser;
import org.garywzh.quumiibox.parser.MyselfParser;
import org.garywzh.quumiibox.parser.Parser;
import org.garywzh.quumiibox.parser.VideoUrlParser;
import org.garywzh.quumiibox.ui.fragment.ItemListFragment;
import org.garywzh.quumiibox.util.LogUtils;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.CookieManager;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RequestHelper {
    public static final String BASE_URL = "http://www.quumii.com";

    private static final String TAG = RequestHelper.class.getSimpleName();

    private static final String HOME_PAGE_URL_PREFIX = BASE_URL + "/itemlist.php?type=all.0.0.0.hot.0.0.&page=";
    private static final String IMAGE_LIST_URL_PREFIX = BASE_URL + "/itemlist.php?type=all.0.0.0.dateline.0.1.2&page=";
    private static final String VIDEO_LIST_URL_PREFIX = BASE_URL + "/itemlist.php?type=all.0.0.0.dateline.0.1.3&page=";
    private static final String ITEM_LIST_BY_TAG_URL_PREFIX = BASE_URL + "/itemlist-do-tag-id-104-page-4.html";

    private static final String VIDEO_URL_PREFIX = "http://www.quumii.com/videolist/fake.php?blogid=";

    private static final String URL_SIGN_IN = "http://www.quumii.com/do.php?ac=943c400772ea74e9ed9335e02dc786a3&&ref";

    private static final int SERVER_ERROR_CODE = 500;

    public static final int ONCE_LOAD_PAGE_COUNT = 2;

    private static final OkHttpClient CLIENT;

    private static MyCookieStore mCookies;

    private static final UserAgentInterceptor USER_AGENT_INTERCEPTOR;

    static {
        CLIENT = new OkHttpClient();
        CLIENT.setConnectTimeout(10, TimeUnit.SECONDS);
        CLIENT.setWriteTimeout(10, TimeUnit.SECONDS);
        CLIENT.setReadTimeout(30, TimeUnit.SECONDS);
//        CLIENT.networkInterceptors().add(new UserAgentInterceptor());
        CLIENT.setFollowRedirects(false);

        mCookies = new MyCookieStore(AppContext.getInstance());
        CLIENT.setCookieHandler(new CookieManager(mCookies, null));
        USER_AGENT_INTERCEPTOR = new UserAgentInterceptor();
    }

    public static OkHttpClient getClient() {
        return CLIENT;
    }

    public static LoginResult login(String username, String password) throws ConnectionException, RemoteException {
        clearCookies();

        LogUtils.v(TAG, "login user: " + username);

        final RequestBody requestBody = new FormEncodingBuilder()
                .add("username", username)
                .add("password", password)
                .add("cookietime", "315360000")
                .add("refer", "http://www.quumii.com/index.php")
                .add("loginsubmit", "登录")
                .add("formhash", "66a2b842")
                .build();
        Request request = new Request.Builder().url(URL_SIGN_IN)
                .header(HttpHeaders.CACHE_CONTROL, "max-age=0")
                .header(HttpHeaders.CONTENT_LENGTH, "157")
                .header(HttpHeaders.COOKIE, "uchome_version=0")
                .header(HttpHeaders.ORIGIN, "http://www.quumii.com")
                .header(HttpHeaders.REFERER, "http://www.quumii.com/do.php?ac=943c400772ea74e9ed9335e02dc786a3")
                .post(requestBody).build();
        Response response = sendRequest(request);

        // 登陆成功的话，返回的response的header里的Set-Cookie字段有4个
        if (response.headers(HttpHeaders.SET_COOKIE).size() != 4) {
            LogUtils.d(TAG, "login failed");
            return null;
        }

        LogUtils.d(TAG, "login succeed");

        final Request homeRequest = new Request.Builder()
                .url("http://www.quumii.com/itemlist.php")
                .build();
        response = sendRequest(homeRequest);

        final String myselfUrl;
        final int myCredit;
        final int myselfId;
        try {
            final String html = response.body().string();
            final Document document = Parser.toDoc(html);
            myselfId = MyselfParser.parseId(document);

            myCredit = MyselfParser.parseCredit(document);
            myselfUrl = Member.buildUrlFromId(myselfId);
        } catch (IOException e) {
            throw new ConnectionException(e);
        }

        final Request MyselfInfoRequest = new Request.Builder()
                .url(myselfUrl)
                .build();
        response = sendRequest(MyselfInfoRequest);

        try {
            final String html = response.body().string();
            final Document document = Parser.toDoc(html);
            final Member.Builder builder = MemberParser.parseDocForMemberNameAndAvatar(document);

            builder.setId(myselfId);
            return new LoginResult(builder.createMember(), myCredit);
        } catch (IOException e) {
            throw new ConnectionException(e);
        }
    }

    //    一次获取多页视频，减少网络获取频率，避免loading标志出现频率过高
    public static List<Item> getMutiPageItemsByCount(int type, int tagId, int count) throws ConnectionException, RemoteException {
        final List<Item> result = Lists.newArrayListWithCapacity(ONCE_LOAD_PAGE_COUNT * 20);
        for (int i = 1; i <= ONCE_LOAD_PAGE_COUNT; i++) {
            result.addAll(getItemsByTypeByPage(type, tagId, (count - 1) * ONCE_LOAD_PAGE_COUNT + i));
        }
        return result;
    }

    public static List<Item> getItemsByTypeByPage(int type, int tagId, int page) throws ConnectionException, RemoteException {
        String itemListUrl;

        switch (type) {
            case ItemListFragment.TYPE_HOME:
                itemListUrl = HOME_PAGE_URL_PREFIX + page;
                break;
            case ItemListFragment.TYPE_VIDEO:
                itemListUrl = VIDEO_LIST_URL_PREFIX + page;
                break;
            case ItemListFragment.TYPE_IMAGE:
                itemListUrl = IMAGE_LIST_URL_PREFIX + page;
                break;
            case ItemListFragment.TYPE_TAG:
                itemListUrl = BASE_URL + "/itemlist-do-tag-id-" + tagId + "-page-" + page + ".html";
                break;
            case ItemListFragment.TYPE_FAV:
                itemListUrl = BASE_URL + "/itemlist.php?type=all.0.0." + UserState.getInstance().getId() + ".hot.0.0.0&page=" + page;
                break;
            default:
                throw new RuntimeException("error type");
        }

        LogUtils.d(TAG, "start loading items");

        final Request request = new Request.Builder()
                .url(itemListUrl)
                .build();

        final Response response = sendRequest(request);

        final Document doc;
        final List<Item> items;
        try {
            doc = Parser.toDoc(response.body().string());
            items = ItemListParser.parseDocForItemList(doc, tagId > 0, type == ItemListFragment.TYPE_FAV);
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
                .url(VIDEO_URL_PREFIX + id)
                .build();

//        通过手机 User-Agent 才能获得 html5 视频链接
        CLIENT.networkInterceptors().add(USER_AGENT_INTERCEPTOR);
        final Response response = sendRequest(request);
        CLIENT.networkInterceptors().remove(USER_AGENT_INTERCEPTOR);

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
        LogUtils.d(TAG, "comments got");

        final Document doc;
        final List<Comment> comments;
        try {
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

    public static void userOperation(Item item, MenuItem menuItem) throws ConnectionException, RemoteException {
        String operationUrl;
        boolean isFav = false;
        switch (menuItem.getItemId()) {
            case R.id.action_up:
                operationUrl = item.getUpUrl();
                break;
            case R.id.action_down:
                operationUrl = item.getDowmUrl();
                break;
            case R.id.action_fav:
                operationUrl = item.getFavUrl();
                isFav = true;
                break;
            default:
                operationUrl = BASE_URL;
        }

        final Request request = new Request.Builder().url(operationUrl).build();
        Response response = sendRequest(request);

        if (isFav) {
            try {
                if (response.body().string().contains("收藏成功")) {
                    AppContext.getEventBus().post(new UserOptionEvent(true));
                } else {
                    final Request unFavRequest = new Request.Builder().url(item.getUnFavUrl()).build();
                    response = sendRequest(unFavRequest);
                    if (response.body().string().contains("已删除")) {
                        AppContext.getEventBus().post(new UserOptionEvent(false));
                    }
                }
            } catch (IOException e) {
                throw new ConnectionException(e);
            }
        }
    }

    public static void clearCookies() {
        mCookies.removeAll();
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
