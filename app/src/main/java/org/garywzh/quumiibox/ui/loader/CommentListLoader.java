package org.garywzh.quumiibox.ui.loader;

import android.content.Context;

import org.garywzh.quumiibox.network.RequestHelper;

import java.util.List;

import org.garywzh.quumiibox.model.Comment;

/**
 * Created by garywzh on 2015/10/10.
 */
public class CommentListLoader extends AsyncTaskLoader<List<Comment>> {

    private int mId;
    public CommentListLoader(Context context, int id) {
        super(context);
        mId = id;
    }

    @Override
    public List<Comment> loadInBackgroundWithException() throws Exception {
        return RequestHelper.getComments(mId);
    }
}
