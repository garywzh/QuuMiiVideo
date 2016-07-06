package org.garywzh.quumiibox.ui.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.common.eventbus.Subscribe;

import org.garywzh.quumiibox.AppContext;
import org.garywzh.quumiibox.R;
import org.garywzh.quumiibox.eventbus.UserReplyResponseEvent;
import org.garywzh.quumiibox.model.Comment;
import org.garywzh.quumiibox.model.OperatInfo;
import org.garywzh.quumiibox.network.NetworkHelper;
import org.garywzh.quumiibox.network.RequestHelper;
import org.garywzh.quumiibox.ui.adapter.CommentAdapter;
import org.garywzh.quumiibox.util.ExecutorUtils;

import java.io.IOException;
import java.util.List;

import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class CommentListFragment extends Fragment implements CommentAdapter.OnReplyActionListener {
    private static final String TAG = CommentListFragment.class.getSimpleName();
    private static final String ARG_ID = "id";

    private String blogId;
    private RecyclerView commentList;
    private CommentAdapter mCommentAdapter;
    private Subscription mSubscription;
    private boolean loaded = false;

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
        AppContext.getEventBus().register(this);
        if (!loaded)
            loadData();
    }

    private void loadData() {
        mSubscription = NetworkHelper.getApiService()
                .getComments(blogId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<Comment>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        Toast.makeText(AppContext.getInstance(), R.string.toast_network_error, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNext(List<Comment> comments) {
                        mCommentAdapter.setDataSource(comments);
                        loaded = true;
                    }
                });
    }

    @Override
    public void onStop() {
        super.onStop();
        AppContext.getEventBus().unregister(this);
        if (mSubscription != null && !mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
            mSubscription = null;
        }
    }

    @Override
    public void onReplyClick() {
        showReplyDialog();
    }


    private void showReplyDialog() {
        ReplyAlertDialogFragment dialogFragment = ReplyAlertDialogFragment.newInstance(blogId);
        dialogFragment.show(getChildFragmentManager(), "dialog");
    }

    public static class ReplyAlertDialogFragment extends DialogFragment {
        public static String ARG_BLOGID = "blogid";
        private EditText input;

        public static ReplyAlertDialogFragment newInstance(String blogId) {
            ReplyAlertDialogFragment frag = new ReplyAlertDialogFragment();
            Bundle args = new Bundle();
            args.putString(ARG_BLOGID, blogId);
            frag.setArguments(args);
            return frag;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            String blogId = getArguments().getString(ARG_BLOGID);
            Activity activity = getActivity();
            int margin_in_dp = 24;
            final float scale = getResources().getDisplayMetrics().density;
            int margin_in_px = (int) (margin_in_dp * scale + 0.5f);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.leftMargin = margin_in_px;
            params.rightMargin = margin_in_px;
            input = new EditText(activity);
            input.setLines(1);
            input.setMaxLines(5);
            input.setLayoutParams(params);
            FrameLayout container = new FrameLayout(activity);
            container.addView(input);
            OnReplyDialogClickListener onDialogClickListener = new OnReplyDialogClickListener(input, blogId);

            return new AlertDialog.Builder(getActivity())
                    .setTitle("评论")
                    .setView(container)
                    .setPositiveButton(R.string.alert_dialog_ok, onDialogClickListener)
                    .setNegativeButton(R.string.alert_dialog_cancel, onDialogClickListener)
                    .create();
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }

        static class OnReplyDialogClickListener implements DialogInterface.OnClickListener {
            private EditText mEditText;
            private String mBlogId;

            public OnReplyDialogClickListener(EditText editText, String blogId) {
                super();
                mEditText = editText;
                mBlogId = blogId;
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
                        ExecutorUtils.execute(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    RequestHelper.sentReply(mBlogId, mEditText.getText().toString());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    Toast.makeText(AppContext.getInstance(), R.string.toast_network_error, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        break;
                }
            }
        }
    }

    @Subscribe
    public void onUserReplyResponseEvent(UserReplyResponseEvent e) {
        if (e.response.contains(OperatInfo.MESSAGE_SUCCESS)) {
            Toast.makeText(getActivity(), getString(R.string.sueeccd), Toast.LENGTH_SHORT).show();
            loadData();
        } else {
            Toast.makeText(getActivity(), e.response, Toast.LENGTH_SHORT).show();
        }
    }
}
