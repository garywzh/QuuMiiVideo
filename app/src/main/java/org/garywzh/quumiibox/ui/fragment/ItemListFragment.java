package org.garywzh.quumiibox.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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

import org.garywzh.quumiibox.AppContext;
import org.garywzh.quumiibox.R;
import org.garywzh.quumiibox.model.Item;
import org.garywzh.quumiibox.model.ItemList;
import org.garywzh.quumiibox.network.NetworkHelper;
import org.garywzh.quumiibox.network.RequestHelper;
import org.garywzh.quumiibox.ui.ImageActivity;
import org.garywzh.quumiibox.ui.MainActivity;
import org.garywzh.quumiibox.ui.TopicActivity;
import org.garywzh.quumiibox.ui.VideoActivity;
import org.garywzh.quumiibox.ui.adapter.ItemAdapter;
import org.garywzh.quumiibox.util.ListUtils;
import org.garywzh.quumiibox.util.LogUtils;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class ItemListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, ItemAdapter.OnItemActionListener {
    private static final String TAG = ItemListFragment.class.getSimpleName();
    private static final String ARG_TYPE = "type";
    private static final String ARG_QUERY = "query";
    public static final int TYPE_ALL = 0;
    public static final int TYPE_SEARCH = 1;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ItemAdapter mAdapter;
    private Context mContext;
    private LinearLayoutManager linearLayoutManager;
    private boolean onLoading;
    private boolean noMore = false;
    private int mCount;
    private List<Item> mItems;
    private int mType;
    private String mQueryString;
    private boolean firstLoad;
    private Subscription mSubscription;

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
        firstLoad = true;
        setHasOptionsMenu(true);

        mAdapter = new ItemAdapter(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mSwipeRefreshLayout = (SwipeRefreshLayout) inflater.inflate(R.layout.fragment_item_list, container, false);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        initRecyclerView();
        return mSwipeRefreshLayout;
    }

    private void initRecyclerView() {
        RecyclerView recyclerView = (RecyclerView) mSwipeRefreshLayout.findViewById(R.id.recycler_view);

        linearLayoutManager = new LinearLayoutManager(mSwipeRefreshLayout.getContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        recyclerView.setAdapter(mAdapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE && !onLoading && !noMore) {
                    int visibleItemCount = linearLayoutManager.getChildCount();
                    int totalItemCount = linearLayoutManager.getItemCount();
                    int pastItems = linearLayoutManager.findFirstVisibleItemPosition();
                    if ((pastItems + visibleItemCount) >= (totalItemCount - 6)) {

                        LogUtils.d(TAG, "scrolled to bottom, loading more");
                        loadData();
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
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (firstLoad) {
            loadData();
        }
    }

    private void loadData() {
        Observable<ItemList> observable;
        switch (mType) {
            case ItemListFragment.TYPE_ALL:
                observable = NetworkHelper.getApiService()
                        .getItems(mCount * NetworkHelper.ONCE_LOAD_COUNT, (mCount + 1) * NetworkHelper.ONCE_LOAD_COUNT);
                break;
            case ItemListFragment.TYPE_SEARCH:
                observable = NetworkHelper.getApiService()
                        .search(mCount * NetworkHelper.ONCE_LOAD_COUNT, (mCount + 1) * NetworkHelper.ONCE_LOAD_COUNT, mQueryString);
                break;
            default:
                throw new RuntimeException("error type");
        }
        mSubscription = observable
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() {
                        onLoading = true;
                        mSwipeRefreshLayout.post(new Runnable() {
                            @Override
                            public void run() {
                                mSwipeRefreshLayout.setRefreshing(true);
                            }
                        });
                    }
                })
                .map(new Func1<ItemList, List<Item>>() {
                    @Override
                    public List<Item> call(ItemList itemList) {
                        return itemList.content;
                    }
                })
                .doOnNext(new Action1<List<Item>>() {
                    @Override
                    public void call(List<Item> items) {
                        if (mCount == 0) {
                            mItems.clear();
                        }
                        if (items == null) {
                            noMore = true;
                        } else {
                            if (items.size() < RequestHelper.ONCE_LOAD_ITEM_COUNT) {
                                noMore = true;
                                LogUtils.d(TAG, "No more items");
                            }
                            ListUtils.mergeListWithoutDuplicates(mItems, items);
                        }
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<Item>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        Toast.makeText(AppContext.getInstance(), R.string.toast_network_error, Toast.LENGTH_SHORT).show();
                        mSwipeRefreshLayout.setRefreshing(false);
                        onLoading = false;
                    }

                    @Override
                    public void onNext(List<Item> items) {
                        if (mCount == 0) {
                            linearLayoutManager.scrollToPositionWithOffset(0, 0);
                        }
                        mAdapter.setDataSource(mItems);
                        firstLoad = false;
                        mCount++;
                        mSwipeRefreshLayout.setRefreshing(false);
                        onLoading = false;
                    }
                });
    }

    @Override
    public void onStop() {
        super.onStop();
        mSwipeRefreshLayout.setRefreshing(false);
        onLoading = false;
        if (mSubscription != null && !mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
            mSubscription = null;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mContext = null;
    }

    @Override
    public void onRefresh() {
        if (!onLoading) {
            mCount = 0;
            noMore = false;
            loadData();
        }
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
    public boolean onItemOpen(Item item) {
        final Intent intent;
        switch (item.type) {
            case "video":
                intent = new Intent(mContext, VideoActivity.class);
                Bundle videoBundle = new Bundle();
                videoBundle.putParcelable("item", item);
                intent.putExtras(videoBundle);
                break;
            case "pic":
            case "longpic":
            case "gif":
                intent = new Intent(mContext, ImageActivity.class);
                Bundle imageBundle = new Bundle();
                imageBundle.putParcelable("item", item);
                intent.putExtras(imageBundle);
                break;
            case "link":
            case "tuji":
            case "duanzi":
                intent = new Intent(mContext, TopicActivity.class);
                Bundle topicBundle = new Bundle();
                topicBundle.putParcelable("item", item);
                intent.putExtras(topicBundle);
                break;
            default:
                LogUtils.w(TAG, "unknown type");
                return false;
        }

        startActivity(intent);
        return false;
    }
}