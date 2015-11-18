package org.garywzh.quumiibox.ui;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.common.eventbus.Subscribe;

import org.garywzh.quumiibox.AppContext;
import org.garywzh.quumiibox.BuildConfig;
import org.garywzh.quumiibox.R;
import org.garywzh.quumiibox.common.UserState;
import org.garywzh.quumiibox.eventbus.LoginEvent;
import org.garywzh.quumiibox.model.Item;
import org.garywzh.quumiibox.model.Member;
import org.garywzh.quumiibox.ui.adapter.ItemAdapter;
import org.garywzh.quumiibox.ui.fragment.ItemListFragment;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, ItemAdapter.OnItemActionListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private ActionBarDrawerToggle mDrawerToggle;
    private Toolbar mToolbar;
    private ImageView mAvatar;
    private MenuItem favsMenuItem;
    private TextView mUsername;
    private TextView mCredit;
    @IdRes
    private int mLastMenuId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNavigationView = (NavigationView) findViewById(R.id.navigationview);

        initToolbar();
        initNavDrawer();

        switchFragment(ItemListFragment.newInstance(ItemListFragment.TYPE_HOME, null));
    }

    private void initToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        if (mToolbar == null) {
            return;
        }
        setSupportActionBar(mToolbar);
    }

    private void initNavDrawer() {
        mLastMenuId = R.id.drawer_home;
        mNavigationView.setNavigationItemSelectedListener(this);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar,
                R.string.desc_open_drawer, R.string.desc_close_drawer);
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        /*Version 23.1.0 改变了navigationview的实现，xml布局无法直接通过findviewbyid获取到headerview，
        所以需要动态添加header*/
        View headerLayout = mNavigationView.inflateHeaderView(R.layout.nav_header);
        mAvatar = (ImageView) headerLayout.findViewById(R.id.avatar_img);
        mUsername = (TextView) headerLayout.findViewById(R.id.tv_username);
        mCredit = (TextView) headerLayout.findViewById(R.id.tv_credit);

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!UserState.getInstance().isLoggedIn()) {
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                } else {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(Member.buildUrlFromId(UserState.getInstance().getId())));
                    startActivity(i);
                }
            }
        };
        mAvatar.setOnClickListener(onClickListener);
        mUsername.setOnClickListener(onClickListener);

        favsMenuItem = mNavigationView.getMenu().findItem(R.id.drawer_fav);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        mDrawerToggle.syncState();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        if (itemId == mLastMenuId) {
            return false;
        }

        switch (itemId) {
            case R.id.drawer_home:
                mLastMenuId = R.id.drawer_home;
                mDrawerLayout.closeDrawer(mNavigationView);
                switchFragment(ItemListFragment.newInstance(ItemListFragment.TYPE_HOME, null));
                return true;
            case R.id.drawer_video:
                mLastMenuId = R.id.drawer_video;
                mDrawerLayout.closeDrawer(mNavigationView);
                switchFragment(ItemListFragment.newInstance(ItemListFragment.TYPE_VIDEO, null));
                return true;
            case R.id.drawer_image:
                mLastMenuId = R.id.drawer_image;
                mDrawerLayout.closeDrawer(mNavigationView);
                switchFragment(ItemListFragment.newInstance(ItemListFragment.TYPE_IMAGE, null));
                return true;
            case R.id.drawer_fav:
                mLastMenuId = R.id.drawer_fav;
                mDrawerLayout.closeDrawer(mNavigationView);
                switchFragment(ItemListFragment.newInstance(ItemListFragment.TYPE_FAV, null));
                return true;
            case R.id.drawer_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.drawer_feedback:
                final Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"));
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"garywzh@gmail.com"});
                intent.putExtra(Intent.EXTRA_SUBJECT, String.format("QuuMiiBox(%s) feedback",
                        BuildConfig.VERSION_NAME));
                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(this, R.string.toast_email_app_not_found, Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.drawer_about:
                startActivity(new Intent(this, AboutActivity.class));
                return true;
        }

        return false;
    }

    public void setNavSelected(@IdRes int menuId) {
        mLastMenuId = menuId;
        mNavigationView.setCheckedItem(menuId);
    }

    private void switchFragment(Fragment fragment) {
        final FragmentManager fragmentManager = getSupportFragmentManager();
        final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.abc_fade_in, R.anim.abc_fade_out,
                R.anim.abc_fade_in, R.anim.abc_fade_out)
                .replace(R.id.fragment, fragment);
        fragmentTransaction.commit();
        invalidateOptionsMenu();
    }

    @Override
    protected void onStart() {
        super.onStart();
        AppContext.getEventBus().register(this);
        updateNavView();
    }

    private void updateNavView() {
        if (!UserState.getInstance().isLoggedIn()) {
            mAvatar.setVisibility(View.INVISIBLE);
            mCredit.setVisibility(View.INVISIBLE);
            mUsername.setText(R.string.action_sign_in);

            favsMenuItem.setEnabled(false);
            return;
        }

        mAvatar.setVisibility(View.VISIBLE);
        mCredit.setVisibility(View.VISIBLE);
        Glide.with(this).load(UserState.getInstance().getAvatar()).crossFade().into(mAvatar);
        mUsername.setText(UserState.getInstance().getUsername());
        mCredit.setText(String.valueOf(UserState.getInstance().getCredit()));

        favsMenuItem.setEnabled(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        AppContext.getEventBus().unregister(this);
    }

    @Subscribe
    public void onLoginEvent(LoginEvent e) {
        updateNavView();
    }

    @Override
    public boolean onItemOpen(View view, Item item) {
        final Intent intent;
        switch (item.getType()) {
            case VIDEO:
                intent = new Intent(this, VideoActivity.class);
                Bundle VideoBundle = new Bundle();
                VideoBundle.putParcelable("item", item);
                intent.putExtras(VideoBundle);
                break;
            case IMAGE:
                intent = new Intent(this, ImageActivity.class);
                Bundle ImageBundle = new Bundle();
                ImageBundle.putParcelable("item", item);
                intent.putExtras(ImageBundle);
                break;
            case TOPIC:
                intent = new Intent(this, TopicActivity.class);
                Bundle TopicBundle = new Bundle();
                TopicBundle.putParcelable("item", item);
                intent.putExtras(TopicBundle);
                break;
            case NEWS:
                intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(item.getinfoBasedType()));
                break;
            default:
                throw new RuntimeException("unknown type");
        }

        startActivity(intent);
        return false;
    }
}