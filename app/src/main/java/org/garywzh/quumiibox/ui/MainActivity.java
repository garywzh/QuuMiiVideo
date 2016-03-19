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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.common.eventbus.Subscribe;
import com.umeng.analytics.MobclickAgent;

import org.garywzh.quumiibox.AppContext;
import org.garywzh.quumiibox.BuildConfig;
import org.garywzh.quumiibox.R;
import org.garywzh.quumiibox.common.UserState;
import org.garywzh.quumiibox.eventbus.LoginEvent;
import org.garywzh.quumiibox.model.UserInfo;
import org.garywzh.quumiibox.ui.adapter.CategoryAdapter;
import org.garywzh.quumiibox.ui.fragment.CategoryFragment;
import org.garywzh.quumiibox.ui.fragment.ItemListFragment;
import org.garywzh.quumiibox.ui.widget.SearchBoxLayout;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, CategoryAdapter.OnCateItemClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    public Toolbar mToolbar;
    private SearchBoxLayout mSearchBox;
    private ImageView mAvatar;
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
        initSearchBox();
        switchFragment(ItemListFragment.newInstance(ItemListFragment.TYPE_ALL, null), false);
    }

    private void initToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        if (mToolbar == null) {
            return;
        }
        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initNavDrawer() {
        mLastMenuId = R.id.drawer_home;
        mNavigationView.setNavigationItemSelectedListener(this);

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
                    i.setData(Uri.parse(UserInfo.buildUrlFormUid(UserState.getInstance().getId())));
                    startActivity(i);
                }
            }
        };
        mAvatar.setOnClickListener(onClickListener);
        mUsername.setOnClickListener(onClickListener);
    }

    private void initSearchBox() {
        mSearchBox = (SearchBoxLayout) findViewById(R.id.search_box);
        mSearchBox.setOnActionListener(new SearchBoxLayout.Listener() {
            @Override
            public void onQueryTextSubmit(String query) {
                mSearchBox.hide();
                if (query.trim().length() == 0) {
                    return;
                }
                switchFragment(ItemListFragment.newInstance(ItemListFragment.TYPE_SEARCH, query.trim()));
            }
        });
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
                getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                return true;
            case R.id.drawer_category:
                mLastMenuId = R.id.drawer_category;
                mDrawerLayout.closeDrawer(mNavigationView);
                switchFragment(CategoryFragment.newInstance());
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
        switchFragment(fragment, true);
    }

    private void switchFragment(Fragment fragment, boolean addToBackStack) {
        final FragmentManager fragmentManager = getSupportFragmentManager();
        final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.abc_fade_in, R.anim.abc_fade_out,
                R.anim.abc_fade_in, R.anim.abc_fade_out)
                .replace(R.id.content, fragment);
        if (addToBackStack) {
            fragmentTransaction.addToBackStack(null);
        }
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
            mUsername.setText(R.string.action_login);
            return;
        }
        mAvatar.setVisibility(View.VISIBLE);
        mCredit.setVisibility(View.VISIBLE);
        Glide.with(this).load(UserState.getInstance().getAvatar()).crossFade().into(mAvatar);
        mUsername.setText(UserState.getInstance().getUsername());
        mCredit.setText(String.valueOf(UserState.getInstance().getCredit()));
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

    @Override
    protected void onStop() {
        super.onStop();
        if (mSearchBox.getVisibility() == View.VISIBLE) {
            mSearchBox.hide();
        }
        AppContext.getEventBus().unregister(this);
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(mNavigationView)) {
            mDrawerLayout.closeDrawer(mNavigationView);
            return;
        }

        if (mSearchBox.getVisibility() == View.VISIBLE) {
            mSearchBox.hide();
            return;
        }
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            mDrawerLayout.openDrawer(mNavigationView);
            return true;
        } else if (id == R.id.action_web_search) {
            mSearchBox.show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Subscribe
    public void onLoginEvent(LoginEvent e) {
        updateNavView();
    }

    @Override
    public void onCateItemClick(String item) {
        switchFragment(ItemListFragment.newInstance(ItemListFragment.TYPE_SEARCH, item));
    }
}