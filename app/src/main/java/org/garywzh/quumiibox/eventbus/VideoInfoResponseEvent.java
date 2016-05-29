package org.garywzh.quumiibox.eventbus;

import org.garywzh.quumiibox.model.VideoInfo;

/**
 * Created by WZH on 2016/5/29.
 */
public class VideoInfoResponseEvent {
    public final VideoInfo videoInfo;
    public final Exception exception;

    public VideoInfoResponseEvent(VideoInfo videoInfo, Exception e) {
        this.videoInfo = videoInfo;
        this.exception = e;
    }
}
