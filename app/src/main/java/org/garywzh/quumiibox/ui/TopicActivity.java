package org.garywzh.quumiibox.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.umeng.analytics.MobclickAgent;

import org.garywzh.quumiibox.R;
import org.garywzh.quumiibox.model.Item;
import org.garywzh.quumiibox.ui.fragment.CommentListFragment;
import org.garywzh.quumiibox.ui.fragment.ItemHeaderFragment;

public class TopicActivity extends AppCompatActivity {
    private static final String TAG = TopicActivity.class.getSimpleName();

    private Item mItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mItem = getIntent().getExtras().getParcelable("item");

        final Fragment itemHeaderFragment = ItemHeaderFragment.newInstance(mItem);
        final Fragment commentListFragment = CommentListFragment.newInstance(mItem.blogid);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.headerview, itemHeaderFragment)
                .replace(R.id.comments, commentListFragment)
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }
}
