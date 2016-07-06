package org.garywzh.quumiibox.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.eventbus.Subscribe;

import org.garywzh.quumiibox.AppContext;
import org.garywzh.quumiibox.R;
import org.garywzh.quumiibox.common.UserState;
import org.garywzh.quumiibox.eventbus.UserOperationResponseEvent;
import org.garywzh.quumiibox.model.Item;
import org.garywzh.quumiibox.model.OperatInfo;
import org.garywzh.quumiibox.model.UserOperation;
import org.garywzh.quumiibox.network.RequestHelper;
import org.garywzh.quumiibox.util.ExecutorUtils;

import java.io.IOException;

public class ItemHeaderFragment extends Fragment {
    private static final String TAG = ItemHeaderFragment.class.getSimpleName();
    private static final String ARG_ITEM = "item";

    private Item mItem;
    private View headerView;
    private TextView mThumbUpCountView;

    public ItemHeaderFragment() {
        // Required empty public constructor
    }

    public static ItemHeaderFragment newInstance(Item item) {
        ItemHeaderFragment fragment = new ItemHeaderFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_ITEM, item);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        if (getArguments() != null) {
            mItem = getArguments().getParcelable(ARG_ITEM);
        }
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        headerView = inflater.inflate(R.layout.fragment_item_header, container, false);
        initHeaderView();
        return headerView;
    }

    private void initHeaderView() {
        final TextView mTiTleView = (TextView) headerView.findViewById(R.id.tv_title);
        mTiTleView.setText(mItem.subject);
        final TextView mTimeView = (TextView) headerView.findViewById(R.id.tv_time);
        mTimeView.setText(mItem.dateline);
        mThumbUpCountView = (TextView) headerView.findViewById(R.id.tv_thumbupcount);
        mThumbUpCountView.setText(mItem.like);
        final TextView mReplyCountView = (TextView) headerView.findViewById(R.id.tv_replycount);
        mReplyCountView.setText(mItem.replynum);

        final View thumUpView = headerView.findViewById(R.id.thumbup_view);
        thumUpView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (UserState.getInstance().isLoggedIn()) {
                    PopupMenu popup = new PopupMenu(getActivity(), thumUpView);
                    popup.getMenuInflater().inflate(R.menu.menu_popup, popup.getMenu());

                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {
                            onOption(item);
                            return true;
                        }
                    });
                    popup.show();
                } else {
                    Toast.makeText(getActivity(), getString(R.string.toast_login_request), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void onOption(final MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.action_share) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.setType("text/plain");
            sendIntent.putExtra("Kdescription", mItem.subject);
            sendIntent.putExtra(Intent.EXTRA_TEXT, mItem.subject + " " + Item.buildUrlFromBlogid(mItem.blogid));
            startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.send_to)));
        } else {
            AppContext.getEventBus().register(this);
            ExecutorUtils.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        RequestHelper.userOperation(mItem, menuItem);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    @Subscribe
    public void onUserOptionResponseEvent(UserOperationResponseEvent e) {
        AppContext.getEventBus().unregister(this);
        if (e.message.contains(OperatInfo.MESSAGE_OK)) {
            switch (e.type) {
                case UserOperation.TYPE_LIKE:
                    mThumbUpCountView.setText(String.valueOf(Integer.parseInt((String) mThumbUpCountView.getText()) + 1));
                    break;
                case UserOperation.TYPE_UNLIKE:
                    mThumbUpCountView.setText(String.valueOf(Integer.parseInt((String) mThumbUpCountView.getText()) - 1));
                    break;
            }
            Toast.makeText(getActivity(), getString(R.string.sueeccd), Toast.LENGTH_SHORT).show();
        } else if (e.message.contains(OperatInfo.MESSAGE_ALREADY)) {
            Toast.makeText(getActivity(), getString(R.string.already), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getActivity(), e.message, Toast.LENGTH_SHORT).show();
        }
    }
}