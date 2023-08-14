package com.usbtv.demo.exo;

import static androidx.media3.common.util.Assertions.checkNotNull;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.media3.common.C;
import androidx.media3.common.Format;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MimeTypes;
import androidx.media3.common.Player;
import androidx.media3.common.Timeline;
import androidx.media3.common.text.Cue;
import androidx.media3.datasource.DefaultDataSource;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.exoplayer.DefaultLoadControl;
import androidx.media3.exoplayer.DefaultRenderersFactory;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.source.ConcatenatingMediaSource;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.exoplayer.source.MergingMediaSource;
import androidx.media3.exoplayer.source.SingleSampleMediaSource;
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector;
import androidx.media3.exoplayer.upstream.DefaultBandwidthMeter;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tv.danmaku.ijk.media.exo2.ExoSourceManager;
import tv.danmaku.ijk.media.exo2.IjkExo2MediaPlayer;
import tv.danmaku.ijk.media.exo2.demo.EventLogger;

/**
 * 自定义exo player，实现不同于库的exo 无缝切换效果
 */
public class MyExo2MediaPlayer extends IjkExo2MediaPlayer {

    private static final String TAG = "GSYExo2MediaPlayer";

    private static final long MAX_POSITION_FOR_SEEK_TO_PREVIOUS = 3000;

    private final Timeline.Window window = new Timeline.Window();

    public static final int POSITION_DISCONTINUITY = 899;

    private int playIndex = 0;

    private String mSubTitile;
    private Player.Listener mTextOutput;
    private List<String> urlList;

    public MyExo2MediaPlayer(Context context) {
        super(context);
        this.mHeaders = new HashMap<>();
        mHeaders.put("allowCrossProtocolRedirects", "true");

        this.mExoHelper = ExoSourceManager.newInstance(context, this.mHeaders);
    }
    @Override
    public void onCues(List<Cue> cues) {
        super.onCues(cues);
        /// 这里
    }
    @Override
    public void onPositionDiscontinuity(Player.PositionInfo oldPosition, Player.PositionInfo newPosition, @Player.DiscontinuityReason int reason) {
        super.onPositionDiscontinuity(oldPosition, newPosition, reason);
        notifyOnInfo(POSITION_DISCONTINUITY, reason);
    }

    public void setDataSource(List<String> uris, Map<String, String> headers, int index, boolean cache) {
        mHeaders = headers;
        if (uris == null) {
            return;
        }
        this.urlList=uris;
        ConcatenatingMediaSource concatenatedSource = new ConcatenatingMediaSource();
        for (String uri : uris) {
            MediaSource mediaSource = mExoHelper.getMediaSource(uri, isPreview, cache, false, mCacheDir, getOverrideExtension());

            if(uri.contains("&subtitle=")){
                mSubTitile=uri.split("&subtitle=")[1];
                try {
                    mSubTitile = URLDecoder.decode(mSubTitile,"UTF-8");
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
               // mSubTitile="https://prod-video-cms-amp-microsoft-com.akamaized.net/tenant/amp/entityid/AA1e20RV?blobrefkey=closedcaptionen-us&$blob=1";
              //  mSubTitile="http://img.cdn.guoshuyu.cn/subtitle2.srt";
            }
            if (mSubTitile != null) {

                MediaSource subtitleSource = getTextSource(mSubTitile);

                mediaSource = new MergingMediaSource( subtitleSource,mediaSource);
                //mediaSource=subtitleSource;
            }
            Log.i("player",""+mSubTitile);
           if(uri!=null) Log.i("player",""+uri);

            concatenatedSource.addMediaSource(mediaSource);

        }
        playIndex = index;
        mMediaSource = concatenatedSource;
    }


    /**
     * 上一集
     */
    public void previous() {
        if (mInternalPlayer == null) {
            return;
        }
        Timeline timeline = mInternalPlayer.getCurrentTimeline();
        if (timeline.isEmpty()) {
            return;
        }
        int windowIndex = mInternalPlayer.getCurrentMediaItemIndex();
        timeline.getWindow(windowIndex, window);
        int previousWindowIndex = mInternalPlayer.getPreviousMediaItemIndex();
        if (previousWindowIndex != C.INDEX_UNSET
            && (mInternalPlayer.getCurrentPosition() <= MAX_POSITION_FOR_SEEK_TO_PREVIOUS
            || (window.isDynamic && !window.isSeekable))) {
            mInternalPlayer.seekTo(previousWindowIndex, C.TIME_UNSET);
        } else {
            mInternalPlayer.seekTo(0);
        }
    }

    @Override
    protected void prepareAsyncInternal() {
        new Handler(Looper.getMainLooper()).post(
            new Runnable() {
                @Override
                public void run() {
                    if (mTrackSelector == null) {
                        mTrackSelector = new DefaultTrackSelector(mAppContext);
                    }
                    mEventLogger = new EventLogger(mTrackSelector);
                    boolean preferExtensionDecoders = true;
                    boolean useExtensionRenderers = true;//是否开启扩展
                    @DefaultRenderersFactory.ExtensionRendererMode int extensionRendererMode = useExtensionRenderers
                        ? (preferExtensionDecoders ? DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER
                        : DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON)
                        : DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF;
                    if (mRendererFactory == null) {
                        mRendererFactory = new DefaultRenderersFactory(mAppContext);
                        mRendererFactory.setExtensionRendererMode(extensionRendererMode);
                    }
                    if (mLoadControl == null) {
                        mLoadControl = new DefaultLoadControl();
                    }
                    mInternalPlayer = new ExoPlayer.Builder(mAppContext, mRendererFactory)
                        .setLooper(Looper.getMainLooper())
                        .setTrackSelector(mTrackSelector)
                        .setLoadControl(mLoadControl).build();

                    mInternalPlayer.addListener(MyExo2MediaPlayer.this);
                    mInternalPlayer.addAnalyticsListener(MyExo2MediaPlayer.this);
                    mInternalPlayer.addListener(mEventLogger);
                    if (mSpeedPlaybackParameters != null) {
                        mInternalPlayer.setPlaybackParameters(mSpeedPlaybackParameters);
                    }
                    if (mSurface != null)
                        mInternalPlayer.setVideoSurface(mSurface);
                    ///fix start index
                    if (playIndex > 0) {
                        mInternalPlayer.seekTo(playIndex, C.INDEX_UNSET);
                    }

                    mInternalPlayer.setMediaSource(mMediaSource, false);
                    mInternalPlayer.prepare();
                    mInternalPlayer.setPlayWhenReady(false);
                }
            }
        );
    }

    public MediaSource getTextSource(String subTitle) {
        //todo C.SELECTION_FLAG_AUTOSELECT language MimeTypes

        Format.Builder builder = new Format.Builder();
                /// 其他的比如 text/x-ssa ，text/vtt，application/ttml+xml 等等
                //.setSampleMimeType(MimeTypes.BASE_TYPE_APPLICATION)
        if(subTitle.lastIndexOf("vtt")>-1){
            builder.setSampleMimeType(MimeTypes.APPLICATION_MP4VTT);
        }else if(subTitle.lastIndexOf("srt")>-1){
            builder.setSampleMimeType(MimeTypes.APPLICATION_SUBRIP);
        }else{
            builder.setSampleMimeType(MimeTypes.APPLICATION_TTML);

        }
        Format textFormat=
                builder.setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
                /// 如果出现字幕不显示，可以通过修改这个语音去对应，
                //  这个问题在内部的 selectTextTrack 时，TextTrackScore 通过 getFormatLanguageScore 方法判断语言获取匹配不上
                //  就会不出现字幕
                .setLanguage("en")
                .build();

        MediaItem.SubtitleConfiguration  subtitle = new MediaItem.SubtitleConfiguration.Builder(Uri.parse(subTitle))
                .setMimeType(checkNotNull(textFormat.sampleMimeType))
                .setLanguage( textFormat.language)
                .setSelectionFlags(textFormat.selectionFlags).build();

        DefaultHttpDataSource.Factory  factory = new DefaultHttpDataSource.Factory()
                .setAllowCrossProtocolRedirects(true)
                .setConnectTimeoutMs(50000)
                .setReadTimeoutMs(50000)
                .setTransferListener( new DefaultBandwidthMeter.Builder(mAppContext).build());

        MediaSource textMediaSource = new SingleSampleMediaSource.Factory(new DefaultDataSource.Factory(mAppContext,
                factory))
                .createMediaSource(subtitle, C.TIME_UNSET);
        return textMediaSource;

    }

    @Override
    public void stop() throws IllegalStateException {
        if (mInternalPlayer == null)
            return;
        mInternalPlayer.setPlayWhenReady(false);
        mInternalPlayer.release();
    }


    /**
     * 下一集
     */
    public boolean next() {
        if (mInternalPlayer == null) {
            return false;
        }
        Timeline timeline = mInternalPlayer.getCurrentTimeline();
        if (timeline.isEmpty()) {
            return false;
        }
        int windowIndex = mInternalPlayer.getCurrentMediaItemIndex();
        int nextWindowIndex = mInternalPlayer.getNextMediaItemIndex();
        if (nextWindowIndex != C.INDEX_UNSET) {
            mInternalPlayer.seekTo(nextWindowIndex, C.TIME_UNSET);
            return true;
        } else if (timeline.getWindow(windowIndex, window).isDynamic) {
            mInternalPlayer.seekTo(windowIndex, C.TIME_UNSET);
            return true;
        }
        return false;
    }

    public int getCurrentWindowIndex() {
        if (mInternalPlayer == null) {
            return 0;
        }
        return mInternalPlayer.getCurrentMediaItemIndex();
    }

    @Override
    public void onMediaItemTransition(@Nullable MediaItem mediaItem, int reason) {
        super.onMediaItemTransition(mediaItem, reason);

        MyExo2VideoManager.instance().listener().onAutoCompletion();

    }

    public void addCutesListener(Player.Listener lis) {
        this.mInternalPlayer.addListener(lis);
    }
}
