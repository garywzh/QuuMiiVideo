package org.garywzh.quumiibox.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.garywzh.quumiibox.R;
import org.garywzh.quumiibox.model.Comment;
import org.garywzh.quumiibox.ui.adapter.CommentAdapter;
import org.garywzh.quumiibox.ui.loader.AsyncTaskLoader.LoaderResult;
import org.garywzh.quumiibox.ui.loader.CommentListLoader;
import org.garywzh.quumiibox.util.LogUtils;

import java.util.List;

public class CommentListFragment extends Fragment implements LoaderCallbacks<LoaderResult<List<Comment>>> {
    private static final String TAG = CommentListFragment.class.getSimpleName();
    private static final String ARG_ID = "id";

    private String blogId;
    private RecyclerView commentList;
    private CommentAdapter.OnCommentActionListener mListener;
    private CommentAdapter mCommentAdapter;

    public CommentListFragment() {
        // Required empty public constructor
    }

    public static CommentListFragment newInstance(String id) {
        CommentListFragment fragment = new CommentListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ID, id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        if (getArguments() != null) {
            blogId = getArguments().getString(ARG_ID);
        }
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        commentList = (RecyclerView) inflater.inflate(R.layout.fragment_comment_list, container, false);

        initCommentsView();

        return commentList;
    }

    private void initCommentsView() {
        commentList.setLayoutManager(new LinearLayoutManager(commentList.getContext()));

        mCommentAdapter = new CommentAdapter(mListener);
        commentList.setAdapter(mCommentAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<LoaderResult<List<Comment>>> onCreateLoader(int id, Bundle args) {
        return new CommentListLoader(getActivity(), blogId);
    }

    @Override
    public void onLoadFinished(Loader<LoaderResult<List<Comment>>> loader, LoaderResult<List<Comment>> result) {
        if (result.hasException()) {
            Toast.makeText(getActivity(), "评论加载失败 - 网络错误", Toast.LENGTH_LONG).show();
            return;
        }

        mCommentAdapter.setDataSource(result.mResult);
    }

    @Override
    public void onLoaderReset(Loader<LoaderResult<List<Comment>>> loader) {
        mCommentAdapter.setDataSource(null);
        LogUtils.d(TAG, "onLoaderReset called");
    }

    private CommentListLoader getLoader() {
        return (CommentListLoader) getLoaderManager().<LoaderResult<List<Comment>>>getLoader(0);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (CommentAdapter.OnCommentActionListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnCommentActionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}
