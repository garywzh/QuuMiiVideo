package org.garywzh.quumiibox.ui.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.garywzh.quumiibox.R;

public class SearchBoxLayout extends FrameLayout implements View.OnClickListener, TextView.OnEditorActionListener {
    private EditText mQuery;
    private RelativeLayout mBox;
    private Listener mListener;

    public SearchBoxLayout(Context context) {
        super(context);
        init();
    }

    public SearchBoxLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SearchBoxLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        final Context context = getContext();
        inflate(context, R.layout.view_search_box, this);

        setBackgroundResource(R.color.transparent_background);

        mBox = (RelativeLayout) findViewById(R.id.box);

        setOnClickListener(this);
        ImageButton mBtnBack = ((ImageButton) findViewById(R.id.action_back));
        ImageButton mBtnClear = ((ImageButton) findViewById(R.id.action_clear));
        mQuery = ((EditText) findViewById(R.id.query));

        mBtnBack.setOnClickListener(this);
        mBtnClear.setOnClickListener(this);
        mQuery.setOnEditorActionListener(this);
    }

    public void show() {
        setVisibility(VISIBLE);
        if (Build.VERSION.SDK_INT >= 21) {
            final int animDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);
            final Animator boxAnimator = ViewAnimationUtils.createCircularReveal(mBox,
                    mBox.getWidth(), mBox.getHeight() / 2, 0, mBox.getWidth())
                    .setDuration(animDuration);

            boxAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mQuery.requestFocus();
                    showInputMethod(mQuery);
                }
            });

            boxAnimator.start();
        } else {
            mQuery.requestFocus();
        }
    }

    public void hide() {
        hideInputMethod(this);

        if (Build.VERSION.SDK_INT >= 21) {
            final int animDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);
            final Animator boxAnimator = ViewAnimationUtils.createCircularReveal(mBox,
                    mBox.getWidth(), mBox.getHeight() / 2, mBox.getWidth(), 0)
                    .setDuration(animDuration);

            boxAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    setVisibility(GONE);
                    mQuery.setText("");
                }
            });

            boxAnimator.start();
        } else {
            setVisibility(GONE);
            mQuery.setText("");
        }
    }

    @Override
    public void onClick(View v) {
        if (v == this) {
            hide();
            return;
        }

        switch (v.getId()) {
            case R.id.action_back:
                hide();
                break;
            case R.id.action_clear:
                mQuery.setText("");
                break;
        }
    }

    public void setOnActionListener(Listener listener) {
        mListener = listener;
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId != EditorInfo.IME_ACTION_SEARCH) {
            return false;
        }

        final String query = v.getText().toString();
        mListener.onQueryTextSubmit(query);
        return true;
    }

    public void showInputMethod(View view) {
        final InputMethodManager manager = (InputMethodManager) view.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        manager.showSoftInput(view, 0);
    }

    public void hideInputMethod(View view) {
        final InputMethodManager manager = (InputMethodManager) view.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public interface Listener {
        void onQueryTextSubmit(String query);
    }
}
