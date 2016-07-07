package org.garywzh.quumiibox.ui.player;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.google.android.exoplayer.ExoPlayer;

import org.garywzh.quumiibox.R;
import org.garywzh.quumiibox.ui.VideoActivity;
import org.garywzh.quumiibox.util.LogUtils;

import java.lang.ref.WeakReference;
import java.util.Formatter;
import java.util.Locale;

public class CustomMediaController extends FrameLayout implements DemoPlayer.Listener {
    private static String TAG = CustomMediaController.class.getSimpleName();

    private MediaController.MediaPlayerControl mPlayer;
    private Context mContext;
    private ViewGroup mAnchor;
    private View mRoot;
    private ProgressBar mProgress;
    private TextView mEndTime, mCurrentTime;
    private ProgressBar mLoadingView;
    private View mControllerView;
    private boolean mShowing = false;
    private boolean mDragging;
    private boolean isEnd = false;
    private boolean progressUpdating = false;
    private int mPlaybackState;
    private static final int TIME_OUT = 3000;
    private static final int FADE_OUT = 1;
    private static final int SHOW_PROGRESS = 2;
    StringBuilder mFormatBuilder;
    Formatter mFormatter;
    private ImageButton mPauseButton;
    private ImageButton mFullscreenButton;
    private Handler mHandler = new MessageHandler(this);

    public CustomMediaController(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    public void onFinishInflate() {
        if (mRoot != null)
            initControllerView(mRoot);
        super.onFinishInflate();
    }

    public void setMediaPlayer(MediaController.MediaPlayerControl player) {
        mPlayer = player;
        updatePausePlay();
        updateFullScreen();
    }

    public void setAnchorView(ViewGroup view) {
        mAnchor = view;
        removeAllViews();
        addControllerView();
    }

    @Override
    public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
    }

    protected void addControllerView() {
        LayoutInflater inflate = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mRoot = inflate.inflate(R.layout.view_media_controller, this);

        initControllerView(mRoot);
    }

    private void initControllerView(View v) {
        mControllerView = v.findViewById(R.id.controller_view);
        mLoadingView = (ProgressBar) v.findViewById(R.id.loading_view);

        mPauseButton = (ImageButton) v.findViewById(R.id.pause);
        mPauseButton.setOnClickListener(mPauseListener);

        mFullscreenButton = (ImageButton) v.findViewById(R.id.fullscreen);
        mFullscreenButton.setOnClickListener(mFullscreenListener);

        mProgress = (ProgressBar) v.findViewById(R.id.mediacontroller_progress);
        if (mProgress != null) {
            if (mProgress instanceof SeekBar) {
                SeekBar seeker = (SeekBar) mProgress;
                seeker.setOnSeekBarChangeListener(mSeekListener);
            }
            mProgress.setMax(1000);
        }

        mEndTime = (TextView) v.findViewById(R.id.time);
        mCurrentTime = (TextView) v.findViewById(R.id.time_current);
        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
    }

    @Override
    public void onError(Exception e) {
    }

    @Override
    public void onStateChanged(boolean playWhenReady, int playbackState) {
        mPlaybackState = playbackState;
        switch (playbackState) {
            case ExoPlayer.STATE_PREPARING:
                showProgress(true);
                show();
                LogUtils.d(TAG, "-----------preparing-----------");
                break;
            case ExoPlayer.STATE_BUFFERING:
                show();
                LogUtils.d(TAG, "-----------buffering-----------");
                break;
            case ExoPlayer.STATE_READY:
                showProgress(false);
                pendingFadeOut();
                isEnd = false;
                if (!mPlayer.isPlaying()) {
                    mHandler.removeMessages(SHOW_PROGRESS);
                    progressUpdating = false;
                } else if (!progressUpdating) {
                    Message message = mHandler.obtainMessage(SHOW_PROGRESS);
                    mHandler.sendMessage(message);
                    progressUpdating = true;
                }
                LogUtils.d(TAG, "-----------ready-----------");
                break;
            case ExoPlayer.STATE_ENDED:
                showProgress(false);
                isEnd = true;
                LogUtils.d(TAG, "-----------ended-----------");
                break;
        }
        if (!isEnd)
            updatePausePlay();
    }

    private void showProgress(boolean showProgress) {
        mLoadingView.setVisibility(showProgress ? VISIBLE : GONE);
        mControllerView.setVisibility(showProgress ? GONE : VISIBLE);
    }

    public void toggleControlsVisibility() {
        if (mShowing)
            hide();
        else
            showControls();
    }

    public void showControls() {
        show();
        if (!isEnd)
            pendingFadeOut();
    }

    public void show() {
        if (mShowing || mAnchor == null)
            return;

        FrameLayout.LayoutParams tlp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                Gravity.BOTTOM
        );
        mAnchor.addView(this, tlp);
        mShowing = true;
    }

    private void pendingFadeOut() {
        mHandler.removeMessages(FADE_OUT);
        Message message = mHandler.obtainMessage(FADE_OUT);
        mHandler.sendMessageDelayed(message, TIME_OUT);
    }

    /**
     * Remove the controller from the screen.
     */
    public void hide() {
        if (!mShowing || mAnchor == null)
            return;

        if (mPlaybackState == ExoPlayer.STATE_READY
                || mPlaybackState == ExoPlayer.STATE_ENDED) {
            mAnchor.removeView(this);
            mShowing = false;
        }
    }

    private String stringForTime(int timeMs) {
        int totalSeconds = timeMs / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    private int setProgress() {
        if (mPlayer == null || mDragging) {
            return 0;
        }

        int position = mPlayer.getCurrentPosition();
        int duration = mPlayer.getDuration();
        if (mProgress != null) {
            if (duration > 0) {
                // use long to avoid overflow
                long pos = 1000L * position / duration;
                LogUtils.d(TAG, "position: " + pos);
                mProgress.setProgress((int) pos);
            }
            int percent = mPlayer.getBufferPercentage();
            mProgress.setSecondaryProgress(percent);
        }

        if (mEndTime != null)
            mEndTime.setText(stringForTime(duration));
        if (mCurrentTime != null)
            mCurrentTime.setText(stringForTime(position));

        return position;
    }

    private View.OnClickListener mPauseListener = new View.OnClickListener() {
        public void onClick(View v) {
            doPauseResume();
            pendingFadeOut();
        }
    };

    private View.OnClickListener mFullscreenListener = new View.OnClickListener() {
        public void onClick(View v) {
            doToggleFullscreen();
            pendingFadeOut();
        }
    };

    private void updatePausePlay() {
        if (mRoot == null || mPauseButton == null || mPlayer == null) {
            return;
        }

        if (isEnd) {
            mPauseButton.setImageResource(R.drawable.ic_refresh_white_24dp);
        } else {
            if (mPlayer.isPlaying()) {
                mPauseButton.setImageResource(R.drawable.ic_pause_white_24dp);
            } else {
                mPauseButton.setImageResource(R.drawable.ic_play_arrow_white_24dp);
            }
        }
    }

    public void updateFullScreen() {
        if (mRoot == null || mFullscreenButton == null || mPlayer == null) {
            return;
        }

        if (((VideoActivity) mContext).isFullScreen()) {
            mFullscreenButton.setImageResource(R.drawable.ic_fullscreen_exit_white_24dp);
        } else {
            mFullscreenButton.setImageResource(R.drawable.ic_fullscreen_white_24dp);
        }
    }

    private void doPauseResume() {
        if (mPlayer == null) {
            return;
        }

        if (isEnd) {
            mPlayer.seekTo(0);
            isEnd = false;
            if (!mPlayer.isPlaying())
                mPlayer.start();
        } else {
            if (mPlayer.isPlaying()) {
                mPlayer.pause();
            } else {
                mPlayer.start();
            }
        }
    }

    private void doToggleFullscreen() {
        if (mPlayer == null) {
            return;
        }

        ((VideoActivity) mContext).toggleFullScreen();
    }

    private OnSeekBarChangeListener mSeekListener = new OnSeekBarChangeListener() {

        private long mProgress;

        @Override
        public void onStartTrackingTouch(SeekBar bar) {
            mDragging = true;
            mHandler.removeMessages(FADE_OUT);
        }

        @Override
        public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
            if (mPlayer == null) {
                return;
            }
            if (!fromuser) {
                // We're not interested in programmatically generated changes to
                // the progress bar's position.
                return;
            }
            long duration = mPlayer.getDuration();
            mProgress = (duration * progress) / 1000L;
            if (mCurrentTime != null)
                mCurrentTime.setText(stringForTime((int) mProgress));
        }

        @Override
        public void onStopTrackingTouch(SeekBar bar) {
            mDragging = false;
            mPlayer.seekTo((int) mProgress);
            pendingFadeOut();
        }
    };

    private static class MessageHandler extends Handler {
        private final WeakReference<CustomMediaController> mView;

        MessageHandler(CustomMediaController view) {
            mView = new WeakReference<>(view);
        }

        @Override
        public void handleMessage(Message msg) {
            CustomMediaController view = mView.get();
            if (view == null || view.mPlayer == null) {
                return;
            }

            int pos;
            switch (msg.what) {
                case FADE_OUT:
                    if (!view.mDragging && !view.isEnd && view.mPlayer.isPlaying())
                        view.hide();
                    break;
                case SHOW_PROGRESS:
                    pos = view.setProgress();
                    if (!view.isEnd) {
                        msg = obtainMessage(SHOW_PROGRESS);
                        sendMessageDelayed(msg, 1000 - (pos % 1000));
                    } else {
                        view.progressUpdating = false;
                        view.updatePausePlay();
                        view.show();
                    }
                    break;
            }
        }
    }
}