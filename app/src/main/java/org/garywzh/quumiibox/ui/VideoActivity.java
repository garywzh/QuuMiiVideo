package org.garywzh.quumiibox.ui;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;

import org.garywzh.quumiibox.common.exception.ConnectionException;
import org.garywzh.quumiibox.model.Member;
import org.garywzh.quumiibox.ui.loader.AsyncTaskLoader;
import org.garywzh.quumiibox.ui.loader.CommentListLoader;
import org.garywzh.quumiibox.common.exception.RemoteException;
import org.garywzh.quumiibox.model.Comment;
import org.garywzh.quumiibox.network.RequestHelper;
import org.garywzh.quumiibox.ui.adapter.CommentAdapter;
import org.garywzh.quumiibox.util.LogUtils;

import java.util.List;

import org.garywzh.quumiibox.R;

public class VideoActivity extends AppCompatActivity implements CommentAdapter.OnCommentActionListener, LoaderManager.LoaderCallbacks<AsyncTaskLoader.LoaderResult<List<Comment>>>{

    private static final String TAG = VideoActivity.class.getSimpleName();

    private WebView mWebView;
    private TextView mTiTleView;
    private String mId;
    private int widthPixels;
    private int heightPixels;

    private CommentAdapter mCommentAdapter;
    private List<Comment> mComments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        final String mTitle = getIntent().getStringExtra("title");
        mId = getIntent().getStringExtra("id");

        mTiTleView = (TextView) findViewById(R.id.title_tv);
        mTiTleView.setText(mTitle);

        mWebView = (WebView) findViewById(R.id.webview);

        mWebView.setBackgroundColor(Color.parseColor("#000000"));

        widthPixels = getResources().getDisplayMetrics().widthPixels;
        heightPixels = Math.round(widthPixels / 1.7777f);
        ViewGroup.LayoutParams lp = mWebView.getLayoutParams();
        lp.height = heightPixels;
        LogUtils.d(TAG, "height : " + lp.height);
        mWebView.setLayoutParams(lp);

        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setUseWideViewPort(false);
        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });

        ListView mCommentsView = (ListView) findViewById(R.id.list_view);
        mCommentAdapter = new CommentAdapter(this);
        mCommentAdapter.setDataSource(mComments);
        mCommentsView.setAdapter(mCommentAdapter);

        VideoLinkTask mVideoLinkTask = new VideoLinkTask(mId);
        mVideoLinkTask.execute((Void) null);

        getSupportLoaderManager().initLoader(0, null, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mWebView.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mWebView.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onDestroy() {
        mWebView.loadUrl("");
        mWebView.destroy();
        mWebView = null;
        super.onDestroy();
    }

    @Override
    public Loader<AsyncTaskLoader.LoaderResult<List<Comment>>> onCreateLoader(int id, Bundle args) {
        return new CommentListLoader(this, Integer.parseInt(mId));
    }

    @Override
    public void onLoadFinished(Loader<AsyncTaskLoader.LoaderResult<List<Comment>>> loader, AsyncTaskLoader.LoaderResult<List<Comment>> result) {
        if (result.hasException()) {
            Toast.makeText(this, "评论加载失败 - 网络错误", Toast.LENGTH_SHORT).show();
            return;
        }

        mCommentAdapter.setDataSource(result.mResult);
    }

    @Override
    public void onLoaderReset(Loader<AsyncTaskLoader.LoaderResult<List<Comment>>> loader) {
        mCommentAdapter.setDataSource(null);
        LogUtils.d(TAG, "onLoaderReset called");
    }

    @Override
    public void onMemberClick(Member member) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(Member.buildUrlFromId(member.getId())));
        startActivity(i);
    }

    public class VideoLinkTask extends AsyncTask<Void, Void, String> {
        private final String mId;

        VideoLinkTask(String id) {
            mId = id;
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                final String result = RequestHelper.getWebViewLinkById(mId);
                if (result != null) {
                    return result;
                }
            } catch (ConnectionException | RemoteException e) {
                e.printStackTrace();
            }
            return "error";
        }

        @Override
        protected void onPostExecute( String result) {

            if (result.equals("error")) {
                mWebView.loadUrl("http://baidu.com");
                LogUtils.d(TAG, "error link : "+result);
            } else if (result.contains("youku")) {
                mWebView.loadUrl(result);
                LogUtils.d(TAG, "video link : "+result);
            }else if (result.contains("letv")){
                mWebView.getSettings().setUseWideViewPort(true);
                result = result.replace("670", String.valueOf(widthPixels));
                result = result.replace("490", String.valueOf(heightPixels));
                mWebView.loadUrl(result);
                LogUtils.d(TAG, "video link : "+result);
            }
            else {
//                mWebView.getSettings().setUseWideViewPort(true);
//                mWebView.loadUrl(result);
//                LogUtils.d(TAG, "video link : " + result);

                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(result));
                startActivity(i);
            }
        }

        @Override
        protected void onCancelled() {
        }
    }
}
