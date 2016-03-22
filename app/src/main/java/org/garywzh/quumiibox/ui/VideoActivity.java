package org.garywzh.quumiibox.ui;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.Toast;

import com.google.android.exoplayer.AspectRatioFrameLayout;
import com.google.android.exoplayer.ExoPlaybackException;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.MediaCodecTrackRenderer;
import com.google.android.exoplayer.MediaCodecUtil;
import com.google.android.exoplayer.drm.UnsupportedDrmException;
import com.google.android.exoplayer.util.Util;
import com.umeng.analytics.MobclickAgent;

import org.garywzh.quumiibox.R;
import org.garywzh.quumiibox.common.exception.ConnectionException;
import org.garywzh.quumiibox.common.exception.FatalException;
import org.garywzh.quumiibox.common.exception.RemoteException;
import org.garywzh.quumiibox.model.Item;
import org.garywzh.quumiibox.model.VideoInfo;
import org.garywzh.quumiibox.network.RequestHelper;
import org.garywzh.quumiibox.ui.fragment.CommentListFragment;
import org.garywzh.quumiibox.ui.fragment.ItemHeaderFragment;
import org.garywzh.quumiibox.ui.player.DemoPlayer;
import org.garywzh.quumiibox.ui.player.DemoPlayer.RendererBuilder;
import org.garywzh.quumiibox.ui.player.EventLogger;
import org.garywzh.quumiibox.ui.player.ExtractorRendererBuilder;
import org.garywzh.quumiibox.ui.player.HlsRendererBuilder;
import org.garywzh.quumiibox.util.LogUtils;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;

public class VideoActivity extends AppCompatActivity implements SurfaceHolder.Callback, View.OnClickListener, DemoPlayer.Listener {
    private static final String TAG = VideoActivity.class.getSimpleName();

    private Item mItem;

    private static final CookieManager defaultCookieManager;

    static {
        defaultCookieManager = new CookieManager();
        defaultCookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
    }

    private EventLogger eventLogger;
    private MediaController mediaController;
    private View shutterView;
    private AspectRatioFrameLayout videoFrame;
    private SurfaceView surfaceView;
    private Button retryButton;
    private Button fullScreenButton;
    private View controls;

    private DemoPlayer player;
    private boolean playerNeedsPrepare;

    private long playerPosition;
    private boolean isFullScreen;

    private String contentUri;
    private int contentType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        isFullScreen = false;
        mItem = getIntent().getExtras().getParcelable("item");

        View videoRoot = findViewById(R.id.video_root);
        videoRoot.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    toggleControlsVisibility();
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    view.performClick();
                }
                return true;
            }
        });
        videoRoot.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_ESCAPE
                        || keyCode == KeyEvent.KEYCODE_MENU) {
                    return false;
                }
                return mediaController.dispatchKeyEvent(event);
            }
        });

        shutterView = findViewById(R.id.shutter);
        controls = findViewById(R.id.controls_root);

        videoFrame = (AspectRatioFrameLayout) findViewById(R.id.video_frame);
        final int width = getResources().getDisplayMetrics().widthPixels;
        ViewGroup.LayoutParams lp = videoFrame.getLayoutParams();
        lp.height = width;
        videoFrame.setLayoutParams(lp);

        surfaceView = (SurfaceView) findViewById(R.id.surface_view);
        surfaceView.getHolder().addCallback(this);
        mediaController = new KeyCompatibleMediaController(this);
        mediaController.setAnchorView(videoRoot);
        retryButton = (Button) findViewById(R.id.retry_button);
        retryButton.setOnClickListener(this);
        fullScreenButton = (Button) findViewById(R.id.full_screen_button);
        fullScreenButton.setOnClickListener(this);

        CookieHandler currentHandler = CookieHandler.getDefault();
        if (currentHandler != defaultCookieManager) {
            CookieHandler.setDefault(defaultCookieManager);
        }
        final Fragment itemHeaderFragment = ItemHeaderFragment.newInstance(mItem);
        final Fragment commentListFragment = CommentListFragment.newInstance(mItem.blogid);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.headerview, itemHeaderFragment)
                .replace(R.id.comments, commentListFragment)
                .commit();
        new PlayListFetcherTask(mItem.vid).execute((Void) null);
    }

    @Override
    public void onNewIntent(Intent intent) {
        releasePlayer();
        playerPosition = 0;
        setIntent(intent);
    }

    public void toggleOrientation() {
        if (!isFullScreen) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            isFullScreen = true;
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            isFullScreen = false;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);

        if (contentUri != null & player == null) {
            preparePlayer(true);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);

        releasePlayer();
        shutterView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releasePlayer();
    }

    // OnClickListener methods

    @Override
    public void onClick(View view) {
        if (view == retryButton) {
            preparePlayer(true);
        } else if (view == fullScreenButton) {
            toggleOrientation();
        }
    }

    // Internal methods

    private RendererBuilder getRendererBuilder() {
        String userAgent = Util.getUserAgent(this, "ExoPlayer");
        switch (contentType) {
            case Util.TYPE_HLS:
                return new HlsRendererBuilder(this, userAgent, contentUri);
            case Util.TYPE_OTHER:
                return new ExtractorRendererBuilder(this, userAgent, Uri.parse(contentUri));
            default:
                throw new IllegalStateException("Unsupported type: " + contentType);
        }
    }

    private void preparePlayer(boolean playWhenReady) {
        if (player == null) {
            player = new DemoPlayer(getRendererBuilder());
            player.addListener(this);
            player.seekTo(playerPosition);
            playerNeedsPrepare = true;
            mediaController.setMediaPlayer(player.getPlayerControl());
            mediaController.setEnabled(true);
            eventLogger = new EventLogger();
            eventLogger.startSession();
            player.addListener(eventLogger);
            player.setInfoListener(eventLogger);
            player.setInternalErrorListener(eventLogger);
        }
        if (playerNeedsPrepare) {
            player.prepare();
            playerNeedsPrepare = false;
            updateButtonVisibilities();
        }
        player.setSurface(surfaceView.getHolder().getSurface());
        player.setPlayWhenReady(playWhenReady);
    }

    private void releasePlayer() {
        if (player != null) {
            playerPosition = player.getCurrentPosition();
            player.release();
            player = null;
            eventLogger.endSession();
            eventLogger = null;
        }
    }

    // DemoPlayer.Listener implementation

    @Override
    public void onStateChanged(boolean playWhenReady, int playbackState) {
        if (playbackState == ExoPlayer.STATE_ENDED) {
            showControls();
        }
        updateButtonVisibilities();
    }

    @Override
    public void onError(Exception e) {
        String errorString = null;
        if (e instanceof UnsupportedDrmException) {
            // Special case DRM failures.
            UnsupportedDrmException unsupportedDrmException = (UnsupportedDrmException) e;
            errorString = Util.SDK_INT < 18 ? "error_drm_not_supported"
                    : unsupportedDrmException.reason == UnsupportedDrmException.REASON_UNSUPPORTED_SCHEME
                    ? "error_drm_unsupported_scheme" : "R.string.error_drm_unknown";
        } else if (e instanceof ExoPlaybackException
                && e.getCause() instanceof MediaCodecTrackRenderer.DecoderInitializationException) {
            // Special case for decoder initialization failures.
            MediaCodecTrackRenderer.DecoderInitializationException decoderInitializationException =
                    (MediaCodecTrackRenderer.DecoderInitializationException) e.getCause();
            if (decoderInitializationException.decoderName == null) {
                if (decoderInitializationException.getCause() instanceof MediaCodecUtil.DecoderQueryException) {
                    errorString = "error_querying_decoders";
                } else if (decoderInitializationException.secureDecoderRequired) {
                    errorString = "error_no_secure_decoder";
                } else {
                    errorString = "error_no_decoder";
                }
            } else {
                errorString = "error_instantiating_decoder";
            }
        }
        if (errorString != null) {
            Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_LONG).show();
        }
        playerNeedsPrepare = true;
        updateButtonVisibilities();
        showControls();
    }

    @Override
    public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees,
                                   float pixelWidthAspectRatio) {
        shutterView.setVisibility(View.GONE);
        videoFrame.setAspectRatio(
                height == 0 ? 1 : (width * pixelWidthAspectRatio) / height);
    }

    // User controls

    private void updateButtonVisibilities() {
        retryButton.setVisibility(playerNeedsPrepare ? View.VISIBLE : View.GONE);
        fullScreenButton.setVisibility(playerNeedsPrepare ? View.GONE : View.VISIBLE);
    }

    private void toggleControlsVisibility() {
        if (mediaController.isShowing()) {
            mediaController.hide();
            controls.setVisibility(View.GONE);
        } else {
            showControls();
        }
    }

    private void showControls() {
        if (mediaController != null) {
            mediaController.show(0);
        }
        controls.setVisibility(View.VISIBLE);
    }

    // SurfaceHolder.Callback implementation

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (player != null) {
            player.setSurface(holder.getSurface());
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // Do nothing.
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (player != null) {
            player.blockingClearSurface();
        }
    }

    // Permission request listener method

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            preparePlayer(true);
        } else {
            Toast.makeText(getApplicationContext(), "storage_permission_denied",
                    Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private static final class KeyCompatibleMediaController extends MediaController {

        private MediaController.MediaPlayerControl playerControl;

        public KeyCompatibleMediaController(Context context) {
            super(context);
        }

        @Override
        public void setMediaPlayer(MediaController.MediaPlayerControl playerControl) {
            super.setMediaPlayer(playerControl);
            this.playerControl = playerControl;
        }

        @Override
        public boolean dispatchKeyEvent(KeyEvent event) {
            int keyCode = event.getKeyCode();
            if (playerControl.canSeekForward() && keyCode == KeyEvent.KEYCODE_MEDIA_FAST_FORWARD) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    playerControl.seekTo(playerControl.getCurrentPosition() + 15000); // milliseconds
                    show();
                }
                return true;
            } else if (playerControl.canSeekBackward() && keyCode == KeyEvent.KEYCODE_MEDIA_REWIND) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    playerControl.seekTo(playerControl.getCurrentPosition() - 5000); // milliseconds
                    show();
                }
                return true;
            }
            return super.dispatchKeyEvent(event);
        }
    }

    private class PlayListFetcherTask extends AsyncTask<Void, Void, VideoInfo> {
        private final String vid;
        private Exception mException;

        PlayListFetcherTask(String vid) {
            this.vid = vid;
        }

        @Override
        protected VideoInfo doInBackground(Void... params) {
            VideoInfo videoInfo;
            try {
                videoInfo = RequestHelper.getVideoInfo(vid);
                return videoInfo;
            } catch (ConnectionException | RemoteException e) {
                mException = e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(final VideoInfo videoInfo) {
            if (mException == null) {
                contentUri = videoInfo.url;
                if (contentUri == null) {
                    Toast.makeText(VideoActivity.this, "cannot get video link", Toast.LENGTH_LONG).show();
                    return;
                } else {
                    if (contentUri.contains("youku")) {
                        contentType = Util.TYPE_HLS;
                    } else {
                        contentType = Util.TYPE_OTHER;
                    }
                    if (player == null) {
                        preparePlayer(true);

                    } else {
                        player.setBackgrounded(false);
                    }
                    return;
                }
            }
            LogUtils.w(TAG, "Video load failed", mException);
            int resId;
            if (mException instanceof ConnectionException) {
                resId = R.string.toast_connection_exception;
            } else if (mException instanceof RemoteException) {
                resId = R.string.toast_remote_exception;
            } else {
                throw new FatalException(mException);
            }
            Toast.makeText(VideoActivity.this, resId, Toast.LENGTH_LONG).show();
        }
    }
}
