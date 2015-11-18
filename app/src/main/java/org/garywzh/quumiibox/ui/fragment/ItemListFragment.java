package org.garywzh.quumiibox.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.garywzh.quumiibox.R;
import org.garywzh.quumiibox.model.Item;
import org.garywzh.quumiibox.model.Tag;
import org.garywzh.quumiibox.network.RequestHelper;
import org.garywzh.quumiibox.ui.MainActivity;
import org.garywzh.quumiibox.ui.adapter.ItemAdapter;
import org.garywzh.quumiibox.ui.loader.AsyncTaskLoader.LoaderResult;
import org.garywzh.quumiibox.ui.loader.ItemListLoader;
import org.garywzh.quumiibox.util.ListUtils;
import org.garywzh.quumiibox.util.LogUtils;

import java.util.ArrayList;
import java.util.List;

public class ItemListFragment extends Fragment implements LoaderCallbacks<LoaderResult<List<Item>>>, SwipeRefreshLayout.OnRefreshListener {
    private static final String TAG = ItemListFragment.class.getSimpleName();
    private static final String ARG_TYPE = "type";
    private static final String ARG_TAG = "tag";
    public static final int TYPE_HOME = 0;
    public static final int TYPE_VIDEO = 1;
    public static final int TYPE_IMAGE = 2;
    public static final int TYPE_TAG = 3;
    public static final int TYPE_FAV = 4;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ItemAdapter mAdapter;
    private RecyclerView recyclerView;
    private ItemAdapter.OnItemActionListener mListener;
    private LinearLayoutManager linearLayoutManager;
    private boolean onLoading;
    private boolean noMore = false;
    private int mCount;
    private List<Item> mItems;
    private int mType;
    private Tag mTag;

    public static ItemListFragment newInstance(int type, Tag tag) {
        ItemListFragment fragment = new ItemListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TYPE, type);
        if (tag != null) {
            args.putParcelable(ARG_TAG, tag);
        }
        fragment.setArguments(args);
        return fragment;
    }

    public ItemListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Bundle arguments = getArguments();
        if (arguments != null) {
            mType = arguments.getInt(ARG_TYPE);
            mTag = arguments.getParcelable(ARG_TAG);
        }

        mItems = new ArrayList<>();
        mCount = 0;

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mSwipeRefreshLayout = (SwipeRefreshLayout) inflater.inflate(R.layout.fragment_item_list,
                container, false);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        initRecyclerView();

        onLoading = true;
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
            }
        });

        return mSwipeRefreshLayout;
    }

    private void initRecyclerView() {
        recyclerView = (RecyclerView) mSwipeRefreshLayout.findViewById(R.id.recycler_view);

        linearLayoutManager = new LinearLayoutManager(mSwipeRefreshLayout.getContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        mAdapter = new ItemAdapter(mListener);
        recyclerView.setAdapter(mAdapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (!onLoading && !noMore) {
                    int visibleItemCount = linearLayoutManager.getChildCount();
                    int totalItemCount = linearLayoutManager.getItemCount();
                    int pastItems = linearLayoutManager.findFirstVisibleItemPosition();
                    if ((pastItems + visibleItemCount) >= (totalItemCount - 6)) {

                        LogUtils.d(TAG, "scrolled to bottom, loading more");
                        onLoading = true;
                        mSwipeRefreshLayout.setRefreshing(true);

                        final ItemListLoader loader = getLoader();
                        if (loader == null) {
                            return;
                        }
                        loader.setPage(mCount + 1);
                    }
                }
            }
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final MainActivity activity = ((MainActivity) getActivity());

        switch (mType) {
            case TYPE_HOME:
                activity.setTitle(getString(R.string.drawer_home));
                break;
            case TYPE_VIDEO:
                activity.setTitle(getString(R.string.drawer_video));
                break;
            case TYPE_IMAGE:
                activity.setTitle(getString(R.string.drawer_image));
                break;
            case TYPE_FAV:
                activity.setTitle(getString(R.string.drawer_fav));
                break;
            case TYPE_TAG:
                activity.setTitle(mTag.getName());
                break;
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<LoaderResult<List<Item>>> onCreateLoader(int id, Bundle args) {
        return new ItemListLoader(getActivity(), mType, (mTag == null) ? 0 : mTag.getId());
    }

    @Override
    public void onLoadFinished(Loader<LoaderResult<List<Item>>> loader, LoaderResult<List<Item>> result) {
        if (result.hasException()) {
            Toast.makeText(getActivity(), "视频列表加载失败 - 网络错误", Toast.LENGTH_LONG).show();
            return;
        }

        if (mCount == 0) {
            mItems.clear();
            linearLayoutManager.scrollToPositionWithOffset(0, 0);
        }

        if (result.mResult.size() == 0) {
            noMore = true;
        } else {
            if (result.mResult.size() < RequestHelper.ONCE_LOAD_PAGE_COUNT * 20) {
                noMore = true;
            }
            mCount++;
            ListUtils.mergeListWithoutDuplicates(mItems, result.mResult);
            mAdapter.setDataSource(mItems);
        }

        mSwipeRefreshLayout.setRefreshing(false);
        onLoading = false;
        LogUtils.d(TAG, "onLoadFinished called");
    }

    @Override
    public void onLoaderReset(Loader<LoaderResult<List<Item>>> loader) {
        mAdapter.setDataSource(null);
        LogUtils.d(TAG, "onLoaderReset called");
    }

    private ItemListLoader getLoader() {
        return (ItemListLoader) getLoaderManager().<LoaderResult<List<Item>>>getLoader(0);
    }

    @Override
    public void onRefresh() {
        if (!onLoading) {
            final ItemListLoader loader = getLoader();
            if (loader == null) {
                return;
            }

            onLoading = true;
            mSwipeRefreshLayout.setRefreshing(true);

            mCount = 0;
            noMore = false;
            loader.setPage(mCount + 1);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (ItemAdapter.OnItemActionListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnItemActionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_topic_list, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            onRefresh();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}