package org.garywzh.quumiibox.eventbus.executor;

import android.support.annotation.NonNull;

import org.garywzh.quumiibox.util.ExecutorUtils;

import java.util.concurrent.Executor;

public class HandlerExecutor implements Executor {
    @Override
    public void execute(@NonNull Runnable command) {
        ExecutorUtils.runOnUiThread(command);
    }
}
