package org.garywzh.quumiibox.ui.fragment;

import android.content.Context;
import android.content.Intent;
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
import org.garywzh.quumiibox.network.RequestHelper;
import org.garywzh.quumiibox.ui.ImageActivity;
import org.garywzh.quumiibox.ui.MainActivity;
import org.garywzh.quumiibox.ui.TopicActivity;
import org.garywzh.quumiibox.ui.VideoActivity;
import org.garywzh.quumiibox.ui.adapter.ItemAdapter;
import org.garywzh.quumiibox.ui.loader.AsyncTaskLoader.LoaderResult;
import org.garywzh.quumiibox.ui.loader.ItemListLoader;
import org.garywzh.quumiibox.util.ListUtils;
import org.garywzh.quumiibox.util.LogUtils;

import java.util.ArrayList;
import java.util.List;

public class ItemListFragment extends Fragment implements LoaderCallbacks<LoaderResult<List<Item>>>, SwipeRefreshLayout.OnRefreshListener, ItemAdapter.OnItemActionListener {
    private static final String TAG = ItemListFragment.class.getSimpleName();
    private static final String ARG_TYPE = "type";
    private static final String ARG_QUERY = "query";
    public static final int TYPE_ALL = 0;
    public static final int TYPE_SEARCH = 1;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ItemAdapter mAdapter;
    private RecyclerView recyclerView;
    private Context mContext;
    private LinearLayoutManager linearLayoutManager;
    private boolean onLoading;
    private boolean noMore = false;
    private int mCount;
    private List<Item> mItems;
    private int mType;
    private String mQueryString;
    private boolean neverCreateView;

    public static ItemListFragment newInstance(int type, String queryString) {
        ItemListFragment fragment = new ItemListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TYPE, type);
        if (queryString != null) {
            args.putString(ARG_QUERY, queryString);
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
            mQueryString = arguments.getString(ARG_QUERY);
        }
        mItems = new ArrayList<>();
        mCount = 0;
        neverCreateView = true;
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
        if (neverCreateView) {
            neverCreateView = false;
            mSwipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    mSwipeRefreshLayout.setRefreshing(true);
                }
            });
        }
        return mSwipeRefreshLayout;
    }

    private void initRecyclerView() {
        recyclerView = (RecyclerView) mSwipeRefreshLayout.findViewById(R.id.recycler_view);

        linearLayoutManager = new LinearLayoutManager(mSwipeRefreshLayout.getContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        mAdapter = new ItemAdapter(this);
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
        getLoaderManager().initLoader(0, null, this);
        final MainActivity activity = ((MainActivity) getActivity());
        switch (mType) {
            case TYPE_ALL:
                activity.setTitle(getString(R.string.drawer_home));
                activity.setNavSelected(R.id.drawer_home);
                break;
            case TYPE_SEARCH:
                activity.setTitle(mQueryString);
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public Loader<LoaderResult<List<Item>>> onCreateLoader(int id, Bundle args) {
        return new ItemListLoader(getActivity(), mType, mQueryString);
    }

    @Override
    public void onLoadFinished(Loader<LoaderResult<List<Item>>> loader, LoaderResult<List<Item>> result) {
        if (result.hasException()) {
            Toast.makeText(getActivity(), "视频列表加载失败 - 网络错误", Toast.LENGTH_LONG).show();
            mSwipeRefreshLayout.setRefreshing(false);
            onLoading = false;
            return;
        }
        if (mCount == 0) {
            mItems.clear();
            linearLayoutManager.scrollToPositionWithOffset(0, 0);
        }
        if (result.mResult == null) {
            noMore = true;
        } else {
            if (result.mResult.size() < RequestHelper.ONCE_LOAD_ITEM_COUNT) {
                noMore = true;
                LogUtils.d(TAG, "No more items");
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
        mContext = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mContext = null;
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

    @Override
    public boolean onItemOpen(View view, Item item) {
        final Intent intent;
        switch (item.type) {
            case "video":
                intent = new Intent(mContext, VideoActivity.class);
                Bundle VideoBundle = new Bundle();
                VideoBundle.putParcelable("item", item);
                intent.putExtras(VideoBundle);
                break;
            case "pic":
                intent = new Intent(mContext, ImageActivity.class);
                Bundle PicBundle = new Bundle();
                PicBundle.putParcelable("item", item);
                intent.putExtras(PicBundle);
                break;
            case "longpic":
                intent = new Intent(mContext, ImageActivity.class);
                Bundle LongPicBundle = new Bundle();
                LongPicBundle.putParcelable("item", item);
                intent.putExtras(LongPicBundle);
                break;
            case "gif":
                intent = new Intent(mContext, ImageActivity.class);
                Bundle GifBundle = new Bundle();
                GifBundle.putParcelable("item", item);
                intent.putExtras(GifBundle);
                break;
            case "link":
                intent = new Intent(mContext, TopicActivity.class);
                Bundle TopicBundle = new Bundle();
                TopicBundle.putParcelable("item", item);
                intent.putExtras(TopicBundle);
                break;
            case "duanzi":
                intent = new Intent(mContext, TopicActivity.class);
                Bundle DuanziBundle = new Bundle();
                DuanziBundle.putParcelable("item", item);
                intent.putExtras(DuanziBundle);
                break;
            default:
                throw new RuntimeException("unknown type");
        }
        startActivity(intent);
        return false;
    }
}