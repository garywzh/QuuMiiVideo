package org.garywzh.quumiibox.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import org.garywzh.quumiibox.R;

public class SearchBoxLayout extends FrameLayout implements View.OnClickListener, TextView.OnEditorActionListener {
    private EditText mQuery;
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
        mQuery.requestFocus();
    }

    public void hide() {
        final InputMethodManager manager = (InputMethodManager) getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        manager.hideSoftInputFromWindow(getWindowToken(), 0);

        setVisibility(GONE);
        mQuery.setText("");
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

    public interface Listener {
        void onQueryTextSubmit(String query);
    }
}
