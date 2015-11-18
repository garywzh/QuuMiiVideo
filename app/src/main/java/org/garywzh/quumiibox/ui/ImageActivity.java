package org.garywzh.quumiibox.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.TextView;

import org.garywzh.quumiibox.R;
import org.garywzh.quumiibox.model.Item;
import org.garywzh.quumiibox.model.Member;
import org.garywzh.quumiibox.ui.adapter.CommentAdapter;
import org.garywzh.quumiibox.ui.fragment.CommentListFragment;
import org.garywzh.quumiibox.ui.fragment.ItemHeaderFragment;

public class ImageActivity extends AppCompatActivity implements CommentAdapter.OnCommentActionListener {
    private static final String TAG = ImageActivity.class.getSimpleName();

    private WebView mWebView;
    private Item mItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mItem = getIntent().getExtras().getParcelable("item");

        mWebView = (WebView) findViewById(R.id.webview);
        mWebView.getSettings().setUseWideViewPort(true);
        mWebView.getSettings().setLoadWithOverviewMode(true);

        mWebView.loadUrl(mItem.getinfoBasedType());

        final Fragment itemHeaderFragment = ItemHeaderFragment.newInstance(mItem);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.headerview, itemHeaderFragment)
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_image, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_comments:
                final Intent intent = new Intent(this, CommentsActivity.class);
                intent.putExtra("id", mItem.getId());
                startActivity(intent);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMemberClick(Member member) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(Member.buildUrlFromId(member.getId())));
        startActivity(i);
    }
}
