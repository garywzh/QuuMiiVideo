package org.garywzh.quumiivideo.ui.loader;

import android.content.Context;

import org.garywzh.quumiivideo.network.RequestHelper;

import java.util.List;

import org.garywzh.quumiivideo.model.Comment;

/**
 * Created by WZH on 2015/10/10.
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
