package org.garywzh.quumiivideo.ui.loader;

import android.content.Context;

import org.garywzh.quumiivideo.model.Item;
import org.garywzh.quumiivideo.network.RequestHelper;

import java.util.List;

public class ItemListLoader extends AsyncTaskLoader<List<Item>> {

    public ItemListLoader(Context context) {
        super(context);
    }

    @Override
    public List<Item> loadInBackgroundWithException() throws Exception {
        return RequestHelper.getItems();
    }
}
