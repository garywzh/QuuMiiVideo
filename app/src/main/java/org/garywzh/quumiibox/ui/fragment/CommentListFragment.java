package org.garywzh.quumiibox.ui.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.common.eventbus.Subscribe;

import org.garywzh.quumiibox.AppContext;
import org.garywzh.quumiibox.R;
import org.garywzh.quumiibox.common.exception.ConnectionException;
import org.garywzh.quumiibox.common.exception.FatalException;
import org.garywzh.quumiibox.common.exception.RemoteException;
import org.garywzh.quumiibox.eventbus.UserReplyResponseEvent;
import org.garywzh.quumiibox.model.Comment;
import org.garywzh.quumiibox.model.OperatInfo;
import org.garywzh.quumiibox.network.RequestHelper;
import org.garywzh.quumiibox.ui.adapter.CommentAdapter;
import org.garywzh.quumiibox.ui.loader.AsyncTaskLoader.LoaderResult;
import org.garywzh.quumiibox.ui.loader.CommentListLoader;
import org.garywzh.quumiibox.util.ExecutorUtils;
import org.garywzh.quumiibox.util.LogUtils;

import java.util.List;

public class CommentListFragment extends Fragment implements LoaderCallbacks<LoaderResult<List<Comment>>>, CommentAdapter.OnReplyActionListener {
    private static final String TAG = CommentListFragment.class.getSimpleName();
    private static final String ARG_ID = "id";

    private String blogId;
    private RecyclerView commentList;
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

        mCommentAdapter = new CommentAdapter(this);
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
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onReplyClick() {
        showReplyDialog();
    }

    private void showReplyDialog() {
        Activity activity = getActivity();
        int margin_in_dp = 24;
        final float scale = getResources().getDisplayMetrics().density;
        int margin_in_px = (int) (margin_in_dp * scale + 0.5f);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = margin_in_px;
        params.rightMargin = margin_in_px;
        final EditText input = new EditText(activity);
        input.setLines(1);
        input.setMaxLines(5);
        input.setLayoutParams(params);
        FrameLayout container = new FrameLayout(activity);
        container.addView(input);
        OnReplyDialogClickListener onDialogClickListener = new OnReplyDialogClickListener(input);
        new AlertDialog.Builder(getActivity())
                .setTitle("评论")
                .setView(container)
                .setPositiveButton("发送", onDialogClickListener)
                .setNegativeButton("取消", onDialogClickListener)
                .create()
                .show();

    }

    class OnReplyDialogClickListener implements DialogInterface.OnClickListener {
        private EditText mEditText;

        public OnReplyDialogClickListener(EditText editText) {
            super();
            mEditText = editText;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case Dialog.BUTTON_NEGATIVE:
                    break;
                case Dialog.BUTTON_POSITIVE:
                    if (mEditText.getText().toString().length() == 0) {
                        return;
                    }
                    AppContext.getEventBus().register(CommentListFragment.this);
                    ExecutorUtils.execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                RequestHelper.sentReply(blogId, mEditText.getText().toString());
                            } catch (ConnectionException | RemoteException e) {
                                throw new FatalException(e);
                            }
                        }
                    });
                    break;
            }
        }
    }

    @Subscribe
    public void onUserReplyResponseEvent(UserReplyResponseEvent e) {
        AppContext.getEventBus().unregister(this);
        if (e.response.contains(OperatInfo.MESSAGE_SUCCESS)) {
            Toast.makeText(getActivity(), getString(R.string.sueeccd), Toast.LENGTH_SHORT).show();
            getLoader().onContentChanged();
        } else {
            Toast.makeText(getActivity(), e.response, Toast.LENGTH_SHORT).show();
        }
    }
}
