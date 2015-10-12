package org.garywzh.quumiivideo.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import org.garywzh.quumiivideo.model.Item;
import org.garywzh.quumiivideo.ui.adapter.ItemAdapter;
import org.garywzh.quumiivideo.ui.loader.AsyncTaskLoader;
import org.garywzh.quumiivideo.util.LogUtils;

import java.util.List;

import org.garywzh.quumiivideo.R;

import org.garywzh.quumiivideo.ui.loader.ItemListLoader;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<AsyncTaskLoader.LoaderResult<List<Item>>>, SwipeRefreshLayout.OnRefreshListener, ItemAdapter.OnItemActionListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private SwipeRefreshLayout swipeRefreshLayout;
    private ItemAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefreshlayout);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycle_view);

        swipeRefreshLayout.setOnRefreshListener(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        mAdapter = new ItemAdapter(this);
        recyclerView.setAdapter(mAdapter);

        LogUtils.d(TAG, "swipeRefreshLayout done");

        getSupportLoaderManager().initLoader(0, null, this);

        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
            }
        });
    }

    @Override
    public Loader<AsyncTaskLoader.LoaderResult<List<Item>>> onCreateLoader(int id, Bundle args) {
        return new ItemListLoader(this);
    }

    @Override
    public void onLoadFinished(Loader<AsyncTaskLoader.LoaderResult<List<Item>>> loader, AsyncTaskLoader.LoaderResult<List<Item>> result) {
        if (result.hasException()) {
            return;
        }

        mAdapter.setDataSource(result.mResult);
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onLoaderReset(Loader<AsyncTaskLoader.LoaderResult<List<Item>>> loader) {
        mAdapter.setDataSource(null);
    }

    @Override
    public void onRefresh() {
        final Loader<?> loader = getSupportLoaderManager().getLoader(0);
        if (loader == null) {
            return;
        }
        loader.forceLoad();
    }

    @Override
    public boolean onItemOpen(View view, Item item) {
        final Intent intent = new Intent(this, VideoActivity.class);

        intent.putExtra("id", String.valueOf(item.getId()));
        intent.putExtra("title", item.getTitle());

        startActivity(intent);

        return false;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            swipeRefreshLayout.setRefreshing(true);
            onRefresh();
            return true;
        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
