package org.garywzh.quumiibox.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.umeng.analytics.MobclickAgent;

import org.garywzh.quumiibox.R;
import org.garywzh.quumiibox.model.Item;
import org.garywzh.quumiibox.ui.fragment.ItemHeaderFragment;

public class ImageActivity extends AppCompatActivity {
    private static final String TAG = ImageActivity.class.getSimpleName();

    private ImageView mImageView;
    private Item mItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mItem = getIntent().getExtras().getParcelable("item");

        mImageView = (ImageView) findViewById(R.id.imageview);
        Glide.with(this).load(Uri.parse(mItem.link))
                .placeholder(R.drawable.coverpic_default)
                .crossFade()
                .into(mImageView);

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
                intent.putExtra("id", mItem.blogid);
                startActivity(intent);
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
