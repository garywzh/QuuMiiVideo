package org.garywzh.quumiibox.ui.loader;

import android.content.Context;

import org.garywzh.quumiibox.model.Comment;
import org.garywzh.quumiibox.network.RequestHelper;

import java.util.List;

/**
 * Created by garywzh on 2015/10/10.
 */
public class CommentListLoader extends AsyncTaskLoader<List<Comment>> {
    private String blogId;

    public CommentListLoader(Context context, String id) {
        super(context);
        blogId = id;
    }

    @Override
    public List<Comment> loadInBackgroundWithException() throws Exception {
        return RequestHelper.getComments(blogId);
    }
}
