package org.garywzh.quumiibox;

import android.app.Application;

import com.bumptech.glide.Glide;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.model.GlideUrl;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;

import org.garywzh.quumiibox.common.UserState;
import org.garywzh.quumiibox.eventbus.executor.HandlerExecutor;
import org.garywzh.quumiibox.network.RequestHelper;
import org.garywzh.quumiibox.util.ExecutorUtils;
import org.garywzh.quumiibox.util.LogUtils;

import java.io.InputStream;


/**
 * Created by WZH on 2015/11/11.
 */
public class AppContext extends Application {
    private static final String TAG = AppContext.class.getSimpleName();
    private static AppContext mInstance;
    private EventBus mEventBus;
    private volatile boolean mIsInited;

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.d(TAG, "AppContext onCreate");
        mInstance = this;
        init();
    }

    public boolean isInited() {
        return mIsInited;
    }

    private void init() {
        Glide.get(this)
                .register(GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory(RequestHelper.getClient()));
        mEventBus = new AsyncEventBus(new HandlerExecutor());
        ExecutorUtils.execute(new AsyncInitTask());
    }

    public static EventBus getEventBus() {
        return mInstance.mEventBus;
    }

    public static AppContext getInstance() {
        return mInstance;
    }

    private class AsyncInitTask implements Runnable {
        @Override
        public void run() {
            UserState.getInstance().init();
            mIsInited = true;
        }
    }
}