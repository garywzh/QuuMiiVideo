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
import org.garywzh.quumiibox.common.exception.ConnectionException;
import org.garywzh.quumiibox.common.exception.FatalException;
import org.garywzh.quumiibox.common.exception.RemoteException;
import org.garywzh.quumiibox.eventbus.UserOptionEvent;
import org.garywzh.quumiibox.model.Item;
import org.garywzh.quumiibox.network.RequestHelper;
import org.garywzh.quumiibox.util.ExecutorUtils;

/**
 * A simple {@link Fragment} subclass.
 */
public class ItemHeaderFragment extends Fragment {
    private static final String TAG = ItemHeaderFragment.class.getSimpleName();
    private static final String ARG_ITEM = "item";

    private Item mItem;
    private View headerView;
    private TextView mThumbUpCountView;
    private boolean hasUp = false;
    private boolean hasDown = false;

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
        mTiTleView.setText(mItem.getTitle());
        final TextView mTimeView = (TextView) headerView.findViewById(R.id.tv_time);
        mTimeView.setText(mItem.getTime());
        mThumbUpCountView = (TextView) headerView.findViewById(R.id.tv_thumbupcount);
        mThumbUpCountView.setText(String.valueOf(mItem.getThumbUpCount()));
        final TextView mReplyCountView = (TextView) headerView.findViewById(R.id.tv_replycount);
        mReplyCountView.setText(String.valueOf(mItem.getReplyCount()));

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
            sendIntent.putExtra("Kdescription", mItem.getTitle());
            sendIntent.putExtra(Intent.EXTRA_TEXT, mItem.getTitle() + " " + Item.buildUrlFromId(mItem.getId()));
            startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.send_to)));
        } else {
            switch (menuItem.getItemId()) {
                case R.id.action_up:
                    if (!hasUp) {
                        mThumbUpCountView.setText(String.valueOf(Integer.parseInt((String) mThumbUpCountView.getText()) + 1));
                        hasUp = true;
                        Toast.makeText(getActivity(), "+1", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case R.id.action_down:
                    if (!hasDown) {
                        mThumbUpCountView.setText(String.valueOf(Integer.parseInt((String) mThumbUpCountView.getText()) - 1));
                        hasDown = true;
                        Toast.makeText(getActivity(), "-1", Toast.LENGTH_SHORT).show();
                    }
                    break;
            }

            AppContext.getEventBus().register(this);
            ExecutorUtils.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        RequestHelper.userOperation(mItem, menuItem);
                    } catch (ConnectionException | RemoteException e) {
                        throw new FatalException(e);
                    }
                }
            });
        }
    }

    @Subscribe
    public void onUserOptionEvent(UserOptionEvent e) {
        AppContext.getEventBus().unregister(this);
        Toast.makeText(getActivity(), e.isFavSucceed ? getString(R.string.fav_add_sueeccd) : getString(R.string.fav_delete_succeed), Toast.LENGTH_SHORT).show();
    }
}