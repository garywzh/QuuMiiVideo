package org.garywzh.quumiibox.ui;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;

import org.garywzh.quumiibox.R;
import org.garywzh.quumiibox.common.exception.ConnectionException;
import org.garywzh.quumiibox.common.exception.RemoteException;
import org.garywzh.quumiibox.model.Item;
import org.garywzh.quumiibox.model.Member;
import org.garywzh.quumiibox.network.RequestHelper;
import org.garywzh.quumiibox.ui.adapter.CommentAdapter;
import org.garywzh.quumiibox.ui.fragment.CommentListFragment;
import org.garywzh.quumiibox.ui.fragment.ItemHeaderFragment;
import org.garywzh.quumiibox.util.LogUtils;

public class VideoActivity extends AppCompatActivity implements CommentAdapter.OnCommentActionListener {
    private static final String TAG = VideoActivity.class.getSimpleName();

    private WebView mWebView;
    private Item mItem;
    private int widthPixels;
    private int heightPixels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        mItem = getIntent().getExtras().getParcelable("item");

        initWebView();

        VideoLinkTask mVideoLinkTask = new VideoLinkTask(mItem.getinfoBasedType());
        mVideoLinkTask.execute((Void) null);

        final Fragment itemHeaderFragment = ItemHeaderFragment.newInstance(mItem);
        final Fragment commentListFragment = CommentListFragment.newInstance(mItem.getId());

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.headerview, itemHeaderFragment)
                .replace(R.id.comments, commentListFragment)
                .commit();
    }

    private void initWebView() {
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
    public void onMemberClick(Member member) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(Member.buildUrlFromId(member.getId())));
        startActivity(i);
    }

    public class VideoLinkTask extends AsyncTask<Void, Void, String> {
        private final String mBlogId;

        VideoLinkTask(String id) {
            mBlogId = id;
            LogUtils.d(TAG, "blogid : " + mBlogId);
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                final String result = RequestHelper.getWebViewLinkById(mBlogId);
                if (result != null) {
                    return result;
                }
            } catch (ConnectionException | RemoteException e) {
                e.printStackTrace();
            }
            return "error";
        }

        @Override
        protected void onPostExecute(String result) {

            if (result.equals("error")) {
                mWebView.loadUrl("http://baidu.com");
                Toast.makeText(getApplicationContext(), "视频连接错误", Toast.LENGTH_LONG).show();
                LogUtils.d(TAG, "error link : " + result);
            } else if (result.contains("youku")) {
                mWebView.loadUrl(result);
                LogUtils.d(TAG, "youku link : " + result);
            } else if (result.contains("letv")) {
                mWebView.getSettings().setUseWideViewPort(true);
                result = result.replace("670", String.valueOf(widthPixels));
                result = result.replace("490", String.valueOf(heightPixels));
                mWebView.loadUrl(result);
                LogUtils.d(TAG, "letv link : " + result);
            } else if (result.contains("quumii")) {
                mWebView.getSettings().setUseWideViewPort(true);
                result = result.replace("670", String.valueOf(widthPixels));
                result = result.replace("490", String.valueOf(heightPixels));
                mWebView.loadUrl(result);
                LogUtils.d(TAG, "quumii link : " + result);
            } else {
                LogUtils.d(TAG, "other link : " + result);

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
