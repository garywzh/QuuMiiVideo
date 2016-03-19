package org.garywzh.quumiibox.ui.loader;

import android.content.Context;

import org.garywzh.quumiibox.model.Item;
import org.garywzh.quumiibox.network.RequestHelper;

import java.util.List;

public class ItemListLoader extends AsyncTaskLoader<List<Item>> {
    private int mCount = 1;
    private int mType;
    private String mQueryString;

    public ItemListLoader(Context context, int type, String query) {
        super(context);
        mType = type;
        mQueryString = query;
    }

    public void setPage(int count) {
        mCount = count;
        onContentChanged();
    }

    @Override
    public List<Item> loadInBackgroundWithException() throws Exception {
        return RequestHelper.getItemsByTypeByPage(mType, mQueryString, mCount);
    }
}
